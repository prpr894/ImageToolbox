/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package ru.tech.imageresizershrinker.feature.draw.presentation.screenLogic

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.toArgb
import androidx.core.net.toUri
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import ru.tech.imageresizershrinker.core.domain.dispatchers.DispatchersHolder
import ru.tech.imageresizershrinker.core.domain.image.ImageCompressor
import ru.tech.imageresizershrinker.core.domain.image.ImageGetter
import ru.tech.imageresizershrinker.core.domain.image.ImageScaler
import ru.tech.imageresizershrinker.core.domain.image.ImageTransformer
import ru.tech.imageresizershrinker.core.domain.image.ShareProvider
import ru.tech.imageresizershrinker.core.domain.image.model.ImageFormat
import ru.tech.imageresizershrinker.core.domain.image.model.ImageInfo
import ru.tech.imageresizershrinker.core.domain.saving.FileController
import ru.tech.imageresizershrinker.core.domain.saving.model.ImageSaveTarget
import ru.tech.imageresizershrinker.core.domain.saving.model.SaveResult
import ru.tech.imageresizershrinker.core.domain.utils.smartJob
import ru.tech.imageresizershrinker.core.filters.domain.FilterProvider
import ru.tech.imageresizershrinker.core.filters.domain.model.Filter
import ru.tech.imageresizershrinker.core.filters.presentation.widget.FilterTemplateCreationSheetComponent
import ru.tech.imageresizershrinker.core.filters.presentation.widget.addFilters.AddFiltersSheetComponent
import ru.tech.imageresizershrinker.core.settings.domain.SettingsProvider
import ru.tech.imageresizershrinker.core.ui.utils.BaseComponent
import ru.tech.imageresizershrinker.core.ui.utils.navigation.Screen
import ru.tech.imageresizershrinker.core.ui.utils.state.update
import ru.tech.imageresizershrinker.core.ui.widget.modifier.HelperGridParams
import ru.tech.imageresizershrinker.feature.draw.domain.DrawBehavior
import ru.tech.imageresizershrinker.feature.draw.domain.DrawLineStyle
import ru.tech.imageresizershrinker.feature.draw.domain.DrawMode
import ru.tech.imageresizershrinker.feature.draw.domain.DrawOnBackgroundParams
import ru.tech.imageresizershrinker.feature.draw.domain.DrawPathMode
import ru.tech.imageresizershrinker.feature.draw.domain.ImageDrawApplier
import ru.tech.imageresizershrinker.feature.draw.presentation.components.UiPathPaint

class DrawComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val initialUri: Uri?,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    private val fileController: FileController,
    private val imageTransformer: ImageTransformer<Bitmap>,
    private val imageCompressor: ImageCompressor<Bitmap>,
    private val imageDrawApplier: ImageDrawApplier<Bitmap, Path, Color>,
    private val imageGetter: ImageGetter<Bitmap>,
    private val imageScaler: ImageScaler<Bitmap>,
    private val shareProvider: ShareProvider<Bitmap>,
    private val filterProvider: FilterProvider<Bitmap>,
    private val settingsProvider: SettingsProvider,
    dispatchersHolder: DispatchersHolder,
    addFiltersSheetComponentFactory: AddFiltersSheetComponent.Factory,
    filterTemplateCreationSheetComponentFactory: FilterTemplateCreationSheetComponent.Factory
) : BaseComponent(dispatchersHolder, componentContext) {

    init {
        debounce {
            initialUri?.let {
                setUri(
                    uri = it,
                    onFailure = {}
                )
            }
        }
    }

    val addFiltersSheetComponent: AddFiltersSheetComponent = addFiltersSheetComponentFactory(
        componentContext = componentContext.childContext(
            key = "addFilters",

            )
    )

    val filterTemplateCreationSheetComponent: FilterTemplateCreationSheetComponent =
        filterTemplateCreationSheetComponentFactory(
            componentContext = componentContext.childContext(
                key = "filterTemplateCreationSheetComponentDraw"
            )
        )

    private val _drawOnBackgroundParams: MutableState<DrawOnBackgroundParams?> =
        mutableStateOf(null)
    val drawOnBackgroundParams: DrawOnBackgroundParams? by _drawOnBackgroundParams

    private val _bitmap: MutableState<Bitmap?> = mutableStateOf(null)
    val bitmap: Bitmap? by _bitmap

    private val _backgroundColor: MutableState<Color> = mutableStateOf(Color.Transparent)
    val backgroundColor by _backgroundColor

    private val _colorPickerBitmap: MutableState<Bitmap?> = mutableStateOf(null)
    val colorPickerBitmap by _colorPickerBitmap

    private val _drawBehavior: MutableState<DrawBehavior> = mutableStateOf(DrawBehavior.None)
    val drawBehavior: DrawBehavior by _drawBehavior

    private val _drawMode: MutableState<DrawMode> = mutableStateOf(DrawMode.Pen)
    val drawMode: DrawMode by _drawMode

    private val _drawPathMode: MutableState<DrawPathMode> = mutableStateOf(DrawPathMode.Free)
    val drawPathMode: DrawPathMode by _drawPathMode

    private val _drawLineStyle: MutableState<DrawLineStyle> = mutableStateOf(DrawLineStyle.None)
    val drawLineStyle: DrawLineStyle by _drawLineStyle

    private val _uri = mutableStateOf(Uri.EMPTY)
    val uri: Uri by _uri

    private val _paths = mutableStateOf(listOf<UiPathPaint>())
    val paths: List<UiPathPaint> by _paths

    private val _lastPaths = mutableStateOf(listOf<UiPathPaint>())
    val lastPaths: List<UiPathPaint> by _lastPaths

    private val _undonePaths = mutableStateOf(listOf<UiPathPaint>())
    val undonePaths: List<UiPathPaint> by _undonePaths

    val havePaths: Boolean
        get() = paths.isNotEmpty() || lastPaths.isNotEmpty() || undonePaths.isNotEmpty()

    private val _imageFormat = mutableStateOf(ImageFormat.Default)
    val imageFormat by _imageFormat

    private val _isSaving: MutableState<Boolean> = mutableStateOf(false)
    val isSaving: Boolean by _isSaving

    private val _saveExif: MutableState<Boolean> = mutableStateOf(false)
    val saveExif: Boolean by _saveExif

    private val _helperGridParams: MutableState<HelperGridParams> =
        mutableStateOf(HelperGridParams())
    val helperGridParams: HelperGridParams by _helperGridParams

    init {
        componentScope.launch {
            val settingsState = settingsProvider.getSettingsState()
            _drawPathMode.update { DrawPathMode.fromOrdinal(settingsState.defaultDrawPathMode) }
        }
        componentScope.launch {
            val params = fileController.restoreObject(
                "drawOnBackgroundParams",
                DrawOnBackgroundParams::class
            )
            _drawOnBackgroundParams.update { params }
        }
        componentScope.launch {
            val params = fileController.restoreObject(
                "helperGridParams",
                HelperGridParams::class
            ) ?: HelperGridParams()
            _helperGridParams.update { params }
        }
    }

    private var savingJob: Job? by smartJob {
        _isSaving.update { false }
    }

    fun saveBitmap(
        oneTimeSaveLocationUri: String?,
        onComplete: (saveResult: SaveResult) -> Unit,
    ) {
        savingJob = componentScope.launch {
            _isSaving.value = true
            getDrawingBitmap()?.let { localBitmap ->
                onComplete(
                    fileController.save(
                        saveTarget = ImageSaveTarget(
                            imageInfo = ImageInfo(
                                imageFormat = imageFormat,
                                width = localBitmap.width,
                                height = localBitmap.height
                            ),
                            originalUri = _uri.value.toString(),
                            sequenceNumber = null,
                            data = imageCompressor.compressAndTransform(
                                image = localBitmap,
                                imageInfo = ImageInfo(
                                    imageFormat = imageFormat,
                                    width = localBitmap.width,
                                    height = localBitmap.height
                                )
                            )
                        ),
                        keepOriginalMetadata = _saveExif.value,
                        oneTimeSaveLocationUri = oneTimeSaveLocationUri
                    ).onSuccess(::registerSave)
                )
            }
            _isSaving.value = false
        }
    }

    private suspend fun calculateScreenOrientationBasedOnUri(uri: Uri): Int {
        val bmp = imageGetter.getImage(uri = uri.toString(), originalSize = false)?.image
        val imageRatio = (bmp?.width ?: 0) / (bmp?.height?.toFloat() ?: 1f)
        return if (imageRatio <= 1.05f) {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
    }

    fun setImageFormat(imageFormat: ImageFormat) {
        _imageFormat.value = imageFormat
        registerChanges()
    }

    fun setSaveExif(bool: Boolean) {
        _saveExif.value = bool
        registerChanges()
    }

    private fun updateBitmap(bitmap: Bitmap?) {
        componentScope.launch {
            _isImageLoading.value = true
            _bitmap.value = imageScaler.scaleUntilCanShow(bitmap)
            _isImageLoading.value = false
        }
    }

    fun setUri(
        uri: Uri,
        onFailure: (Throwable) -> Unit,
    ) {
        componentScope.launch {
            _paths.value = listOf()
            _lastPaths.value = listOf()
            _undonePaths.value = listOf()
            _isImageLoading.value = true

            _uri.value = uri
            if (drawBehavior !is DrawBehavior.Image) {
                _drawBehavior.update {
                    DrawBehavior.Image(calculateScreenOrientationBasedOnUri(uri))
                }
            }
            imageGetter.getImageAsync(
                uri = uri.toString(),
                originalSize = true,
                onGetImage = { data ->
                    updateBitmap(data.image)
                    _imageFormat.update { data.imageInfo.imageFormat }
                },
                onFailure = onFailure
            )
        }
    }

    private suspend fun getDrawingBitmap(): Bitmap? = withContext(defaultDispatcher) {
        imageDrawApplier.applyDrawToImage(
            drawBehavior = drawBehavior.let {
                if (it is DrawBehavior.Background) it.copy(color = backgroundColor.toArgb())
                else it
            },
            pathPaints = paths,
            imageUri = _uri.value.toString()
        )
    }

    fun openColorPicker() {
        componentScope.launch {
            _colorPickerBitmap.value = getDrawingBitmap()
        }
    }

    fun resetDrawBehavior() {
        _paths.value = listOf()
        _lastPaths.value = listOf()
        _undonePaths.value = listOf()
        _bitmap.value = null
        _drawBehavior.update {
            DrawBehavior.None
        }
        _drawPathMode.update { DrawPathMode.Free }
        _uri.value = Uri.EMPTY
        _backgroundColor.value = Color.Transparent
        registerChangesCleared()
    }

    fun startDrawOnBackground(
        reqWidth: Int,
        reqHeight: Int,
        color: Color,
    ) {
        val width = reqWidth.takeIf { it > 0 } ?: 1
        val height = reqHeight.takeIf { it > 0 } ?: 1
        val imageRatio = width / height.toFloat()
        _drawBehavior.update {
            DrawBehavior.Background(
                orientation = if (imageRatio <= 1f) {
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                },
                width = width,
                height = height,
                color = color.toArgb()
            )
        }
        _backgroundColor.value = color

        componentScope.launch {
            val newValue = DrawOnBackgroundParams(
                width = width,
                height = height,
                color = color.toArgb()
            )

            _drawOnBackgroundParams.update { newValue }
            fileController.saveObject(
                key = "drawOnBackgroundParams",
                value = newValue
            )
        }
    }

    fun shareBitmap(onComplete: () -> Unit) {
        savingJob = componentScope.launch {
            _isSaving.value = true
            getDrawingBitmap()?.let {
                shareProvider.shareImage(
                    image = it,
                    imageInfo = ImageInfo(
                        imageFormat = imageFormat,
                        width = it.width,
                        height = it.height
                    ),
                    onComplete = onComplete
                )
            }
            _isSaving.value = false
        }
    }

    fun updateBackgroundColor(color: Color) {
        _backgroundColor.value = color
        registerChanges()
    }

    fun clearDrawing() {
        if (paths.isNotEmpty()) {
            _lastPaths.value = paths
            _paths.value = listOf()
            _undonePaths.value = listOf()
            registerChanges()
        }
    }

    fun undo() {
        if (paths.isEmpty() && lastPaths.isNotEmpty()) {
            _paths.value = lastPaths
            _lastPaths.value = listOf()
            return
        }
        if (paths.isEmpty()) return

        val lastPath = paths.last()

        _paths.update { it - lastPath }
        _undonePaths.update { it + lastPath }
        registerChanges()
    }

    fun redo() {
        if (undonePaths.isEmpty()) return

        val lastPath = undonePaths.last()
        _paths.update { it + lastPath }
        _undonePaths.update { it - lastPath }
        registerChanges()
    }

    fun addPath(pathPaint: UiPathPaint) {
        _paths.update { it + pathPaint }
        _undonePaths.value = listOf()
        registerChanges()
    }

    fun cancelSaving() {
        savingJob?.cancel()
        savingJob = null
        _isSaving.value = false
    }

    suspend fun filter(
        bitmap: Bitmap,
        filters: List<Filter<*>>,
    ): Bitmap? = imageTransformer.transform(
        image = bitmap,
        transformations = filters.map { filterProvider.filterToTransformation(it) }
    )

    fun cacheCurrentImage(onComplete: (Uri) -> Unit) {
        savingJob = componentScope.launch {
            _isSaving.value = true
            getDrawingBitmap()?.let { image ->
                shareProvider.cacheImage(
                    image = image,
                    imageInfo = ImageInfo(
                        imageFormat = imageFormat,
                        width = image.width,
                        height = image.height
                    )
                )?.let { uri ->
                    onComplete(uri.toUri())
                }
            }
            _isSaving.value = false
        }
    }

    fun updateDrawMode(drawMode: DrawMode) {
        _drawMode.update { drawMode }
    }

    fun updateDrawPathMode(drawPathMode: DrawPathMode) {
        _drawPathMode.update { drawPathMode }
    }

    fun getFormatForFilenameSelection(): ImageFormat = imageFormat

    fun updateDrawLineStyle(style: DrawLineStyle) {
        _drawLineStyle.update { style }
    }

    private var smartSavingJob: Job? by smartJob()

    fun updateHelperGridParams(params: HelperGridParams) {
        _helperGridParams.update { params }

        smartSavingJob = componentScope.launch {
            delay(200)
            fileController.saveObject(
                key = "helperGridParams",
                value = params
            )
        }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            initialUri: Uri?,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): DrawComponent
    }
}
