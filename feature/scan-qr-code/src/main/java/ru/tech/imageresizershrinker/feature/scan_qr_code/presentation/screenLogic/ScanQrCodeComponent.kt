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

package ru.tech.imageresizershrinker.feature.scan_qr_code.presentation.screenLogic


import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import com.arkivanov.decompose.ComponentContext
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.tech.imageresizershrinker.core.domain.dispatchers.DispatchersHolder
import ru.tech.imageresizershrinker.core.domain.image.ImageCompressor
import ru.tech.imageresizershrinker.core.domain.image.ShareProvider
import ru.tech.imageresizershrinker.core.domain.image.model.ImageFormat
import ru.tech.imageresizershrinker.core.domain.image.model.ImageInfo
import ru.tech.imageresizershrinker.core.domain.image.model.Quality
import ru.tech.imageresizershrinker.core.domain.saving.FileController
import ru.tech.imageresizershrinker.core.domain.saving.model.ImageSaveTarget
import ru.tech.imageresizershrinker.core.domain.saving.model.SaveResult
import ru.tech.imageresizershrinker.core.domain.utils.smartJob
import ru.tech.imageresizershrinker.core.filters.domain.FavoriteFiltersInteractor
import ru.tech.imageresizershrinker.core.settings.domain.SettingsProvider
import ru.tech.imageresizershrinker.core.settings.domain.model.SettingsState
import ru.tech.imageresizershrinker.core.settings.presentation.model.toUiFont
import ru.tech.imageresizershrinker.core.ui.utils.BaseComponent
import ru.tech.imageresizershrinker.core.ui.utils.state.update
import ru.tech.imageresizershrinker.feature.scan_qr_code.domain.ImageBarcodeReader
import ru.tech.imageresizershrinker.feature.scan_qr_code.presentation.components.QrPreviewParams

class ScanQrCodeComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted initialQrCodeContent: String?,
    @Assisted uriToAnalyze: Uri?,
    @Assisted val onGoBack: () -> Unit,
    private val fileController: FileController,
    private val shareProvider: ShareProvider<Bitmap>,
    private val imageCompressor: ImageCompressor<Bitmap>,
    private val favoriteFiltersInteractor: FavoriteFiltersInteractor,
    private val imageBarcodeReader: ImageBarcodeReader,
    settingsProvider: SettingsProvider,
    dispatchersHolder: DispatchersHolder
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _params: MutableState<QrPreviewParams> = mutableStateOf(
        QrPreviewParams.Default.copy(
            content = initialQrCodeContent ?: ""
        )
    )
    val params by _params

    private val _isSaving: MutableState<Boolean> = mutableStateOf(false)
    val isSaving by _isSaving

    private var savingJob: Job? by smartJob {
        _isSaving.update { false }
    }

    private var settingsState: SettingsState = SettingsState.Default

    init {
        settingsProvider.getSettingsStateFlow().onEach { state ->
            settingsState = state
            _params.update {
                it.copy(
                    descriptionFont = settingsState.font.toUiFont()
                )
            }
        }.launchIn(componentScope)

        uriToAnalyze?.let(::readBarcodeFromImage)
    }

    fun saveBitmap(
        bitmap: Bitmap,
        oneTimeSaveLocationUri: String?,
        onComplete: (saveResult: SaveResult) -> Unit
    ) {
        savingJob = componentScope.launch {
            _isSaving.update { true }
            onComplete(
                fileController.save(
                    saveTarget = ImageSaveTarget(
                        imageInfo = ImageInfo(
                            width = bitmap.width,
                            height = bitmap.height
                        ),
                        originalUri = "_",
                        sequenceNumber = null,
                        data = imageCompressor.compress(
                            image = bitmap,
                            imageFormat = ImageFormat.Png.Lossless,
                            quality = Quality.Base(100)
                        )
                    ),
                    keepOriginalMetadata = false,
                    oneTimeSaveLocationUri = oneTimeSaveLocationUri
                )
            )
            _isSaving.update { false }
        }
    }

    fun shareImage(
        bitmap: Bitmap,
        onComplete: () -> Unit
    ) {
        _isSaving.value = false
        savingJob?.cancel()
        savingJob = componentScope.launch {
            _isSaving.value = true
            bitmap.let { image ->
                shareProvider.shareImage(
                    imageInfo = ImageInfo(
                        width = image.width,
                        height = image.height,
                        imageFormat = ImageFormat.Png.Lossless
                    ),
                    image = image,
                    onComplete = {
                        _isSaving.value = false
                        onComplete()
                    }
                )
            }
        }
    }

    fun cancelSaving() {
        savingJob?.cancel()
        savingJob = null
        _isSaving.value = false
    }

    fun cacheImage(
        bitmap: Bitmap,
        onComplete: (Uri) -> Unit
    ) {
        _isSaving.value = false
        savingJob?.cancel()
        savingJob = componentScope.launch {
            _isSaving.value = true
            bitmap.let { image ->
                shareProvider.cacheImage(
                    image = image,
                    imageInfo = ImageInfo(
                        width = image.width,
                        height = image.height,
                        imageFormat = ImageFormat.Png.Lossless
                    )
                )?.let { uri ->
                    onComplete(uri.toUri())
                }
            }
            _isSaving.value = false
        }
    }

    fun processFilterTemplateFromQrContent(
        onSuccess: (filterName: String, filtersCount: Int) -> Unit
    ) {
        componentScope.launch {
            if (favoriteFiltersInteractor.isValidTemplateFilter(params.content)) {
                favoriteFiltersInteractor.addTemplateFilterFromString(
                    string = params.content,
                    onSuccess = onSuccess,
                    onFailure = {}
                )
            }
        }
    }

    fun getFormatForFilenameSelection(): ImageFormat = ImageFormat.Png.Lossless

    fun updateParams(params: QrPreviewParams) {
        _params.update { params }
    }

    fun readBarcodeFromImage(
        imageUri: Uri,
        onFailure: (Throwable) -> Unit = {}
    ) {
        componentScope.launch {
            imageBarcodeReader
                .readBarcode(imageUri)
                .onSuccess {
                    updateParams(
                        params.copy(
                            content = it
                        )
                    )
                }
                .onFailure(onFailure)
        }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            initialQrCodeContent: String?,
            uriToAnalyze: Uri?,
            onGoBack: () -> Unit,
        ): ScanQrCodeComponent
    }

}