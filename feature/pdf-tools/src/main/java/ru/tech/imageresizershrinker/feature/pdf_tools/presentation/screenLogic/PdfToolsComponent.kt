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

package ru.tech.imageresizershrinker.feature.pdf_tools.presentation.screenLogic

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import ru.tech.imageresizershrinker.core.domain.dispatchers.DispatchersHolder
import ru.tech.imageresizershrinker.core.domain.image.ImageCompressor
import ru.tech.imageresizershrinker.core.domain.image.ImageTransformer
import ru.tech.imageresizershrinker.core.domain.image.ShareProvider
import ru.tech.imageresizershrinker.core.domain.image.model.ImageFormat
import ru.tech.imageresizershrinker.core.domain.image.model.ImageInfo
import ru.tech.imageresizershrinker.core.domain.image.model.Preset
import ru.tech.imageresizershrinker.core.domain.image.model.Quality
import ru.tech.imageresizershrinker.core.domain.saving.FileController
import ru.tech.imageresizershrinker.core.domain.saving.model.ImageSaveTarget
import ru.tech.imageresizershrinker.core.domain.saving.model.SaveResult
import ru.tech.imageresizershrinker.core.domain.saving.model.onSuccess
import ru.tech.imageresizershrinker.core.domain.utils.runSuspendCatching
import ru.tech.imageresizershrinker.core.domain.utils.smartJob
import ru.tech.imageresizershrinker.core.ui.utils.BaseComponent
import ru.tech.imageresizershrinker.core.ui.utils.navigation.Screen
import ru.tech.imageresizershrinker.core.ui.utils.state.update
import ru.tech.imageresizershrinker.feature.pdf_tools.domain.PdfManager
import ru.tech.imageresizershrinker.feature.pdf_tools.presentation.components.PdfToImageState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class PdfToolsComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val initialType: Screen.PdfTools.Type?,
    @Assisted val onGoBack: () -> Unit,
    private val imageTransformer: ImageTransformer<Bitmap>,
    private val imageCompressor: ImageCompressor<Bitmap>,
    private val pdfManager: PdfManager<Bitmap>,
    private val shareProvider: ShareProvider<Bitmap>,
    private val fileController: FileController,
    dispatchersHolder: DispatchersHolder
) : BaseComponent(dispatchersHolder, componentContext) {

    init {
        debounce {
            initialType?.let(::setType)
        }
    }

    private val _pdfToImageState: MutableState<PdfToImageState?> = mutableStateOf(null)
    val pdfToImageState by _pdfToImageState

    private val _imagesToPdfState: MutableState<List<Uri>?> = mutableStateOf(null)
    val imagesToPdfState by _imagesToPdfState

    private val _pdfPreviewUri: MutableState<Uri?> = mutableStateOf(null)
    val pdfPreviewUri by _pdfPreviewUri

    private val _pdfType: MutableState<Screen.PdfTools.Type?> = mutableStateOf(null)
    val pdfType: Screen.PdfTools.Type? by _pdfType

    private val _byteArray = mutableStateOf<ByteArray?>(null)

    private val _imageInfo = mutableStateOf(ImageInfo())
    val imageInfo by _imageInfo

    private val _isSaving: MutableState<Boolean> = mutableStateOf(false)
    val isSaving by _isSaving

    private val _presetSelected: MutableState<Preset.Percentage> =
        mutableStateOf(Preset.Percentage(100))
    val presetSelected by _presetSelected

    private val _scaleSmallImagesToLarge: MutableState<Boolean> = mutableStateOf(false)
    val scaleSmallImagesToLarge by _scaleSmallImagesToLarge

    private val _showOOMWarning: MutableState<Boolean> = mutableStateOf(false)
    val showOOMWarning by _showOOMWarning

    private var savingJob: Job? by smartJob {
        _isSaving.update { false }
    }

    private fun resetCalculatedData() {
        _byteArray.value = null
    }

    fun savePdfTo(
        uri: Uri,
        onResult: (SaveResult) -> Unit
    ) {
        savingJob = componentScope.launch {
            _isSaving.value = true
            _byteArray.value?.let { byteArray ->
                fileController.writeBytes(
                    uri = uri.toString(),
                    block = { it.writeBytes(byteArray) }
                ).also(onResult).onSuccess(::registerSave)
            }
            _isSaving.value = false
        }
    }

    fun cancelSaving() {
        savingJob?.cancel()
        savingJob = null
        _isSaving.value = false
    }

    fun setType(type: Screen.PdfTools.Type) {
        when (type) {
            is Screen.PdfTools.Type.ImagesToPdf -> setImagesToPdf(type.imageUris)
            is Screen.PdfTools.Type.PdfToImages -> setPdfToImagesUri(type.pdfUri)
            is Screen.PdfTools.Type.Preview -> setPdfPreview(type.pdfUri)
        }
        registerChanges()
        resetCalculatedData()
    }

    fun setPdfPreview(uri: Uri?) {
        _pdfType.update {
            if (it !is Screen.PdfTools.Type.Preview) {
                Screen.PdfTools.Type.Preview(uri)
            } else it
        }
        _pdfPreviewUri.update { uri }
        _imagesToPdfState.update { null }
        _pdfToImageState.update { null }
        resetCalculatedData()
    }

    fun setImagesToPdf(uris: List<Uri>?) {
        _pdfType.update {
            if (it !is Screen.PdfTools.Type.ImagesToPdf) {
                Screen.PdfTools.Type.ImagesToPdf(uris)
            } else it
        }
        _imagesToPdfState.update { uris }
        _pdfPreviewUri.update { null }
        _pdfToImageState.update { null }
        resetCalculatedData()
    }

    fun setPdfToImagesUri(newUri: Uri?) {
        _pdfToImageState.update { null }
        _pdfType.update {
            if (it !is Screen.PdfTools.Type.PdfToImages) {
                Screen.PdfTools.Type.PdfToImages(newUri)
            } else it
        }
        componentScope.launch {
            newUri?.let {
                val pages = pdfManager.getPdfPages(newUri.toString())
                _pdfToImageState.update {
                    PdfToImageState(
                        uri = newUri,
                        pagesCount = pages.size,
                        selectedPages = pages
                    )
                }
                checkForOOM()
            }
        }

        _imagesToPdfState.update { null }
        _pdfPreviewUri.update { null }
        resetCalculatedData()
    }

    fun clearAll() {
        _pdfType.update { null }
        _pdfPreviewUri.update { null }
        _imagesToPdfState.update { null }
        _pdfToImageState.update { null }
        _presetSelected.update { Preset.Original }
        _showOOMWarning.value = false
        _imageInfo.value = ImageInfo()
        resetCalculatedData()
        registerChangesCleared()
    }

    private val _done: MutableState<Int> = mutableIntStateOf(0)
    val done by _done

    private val _left: MutableState<Int> = mutableIntStateOf(1)
    val left by _left

    fun savePdfToImages(
        oneTimeSaveLocationUri: String?,
        onComplete: (List<SaveResult>) -> Unit
    ) {
        _done.value = 0
        _left.value = 1
        val results = mutableListOf<SaveResult>()
        savingJob = componentScope.launch {
            pdfManager.convertPdfToImages(
                pdfUri = _pdfToImageState.value?.uri.toString(),
                pages = _pdfToImageState.value?.selectedPages,
                preset = presetSelected,
                onProgressChange = { _, bitmap ->
                    val imageInfo = imageTransformer.applyPresetBy(
                        image = bitmap,
                        preset = _presetSelected.value,
                        currentInfo = imageInfo
                    )

                    results.add(
                        fileController.save(
                            saveTarget = ImageSaveTarget(
                                imageInfo = imageInfo,
                                metadata = null,
                                originalUri = _pdfToImageState.value?.uri.toString(),
                                sequenceNumber = _done.value + 1,
                                data = imageCompressor.compressAndTransform(
                                    image = bitmap,
                                    imageInfo = imageInfo
                                )
                            ),
                            keepOriginalMetadata = false,
                            oneTimeSaveLocationUri = oneTimeSaveLocationUri
                        )
                    )
                    _done.value += 1
                },
                onGetPagesCount = { size ->
                    _left.update { size }
                    _isSaving.value = true
                },
                onComplete = {
                    _isSaving.value = false
                    onComplete(results.onSuccess(::registerSave))
                }
            )
        }
    }

    fun convertImagesToPdf(onComplete: () -> Unit) {
        _done.value = 0
        _left.value = 0
        savingJob = componentScope.launch {
            _isSaving.value = true
            _left.value = imagesToPdfState?.size ?: 0
            _byteArray.value = pdfManager.convertImagesToPdf(
                imageUris = imagesToPdfState?.map { it.toString() } ?: emptyList(),
                onProgressChange = {
                    _done.value = it
                },
                scaleSmallImagesToLarge = _scaleSmallImagesToLarge.value,
                preset = _presetSelected.value
            )
            registerChanges()
            onComplete()
            _isSaving.value = false
        }
    }

    fun generatePdfFilename(): String {
        val timeStamp = SimpleDateFormat(
            "yyyy-MM-dd_HH-mm-ss",
            Locale.getDefault()
        ).format(Date()) + "_${Random(Random.nextInt()).hashCode().toString().take(4)}"
        return "PDF_$timeStamp"
    }

    fun preformSharing(
        onComplete: () -> Unit
    ) {
        savingJob = componentScope.launch {
            _isSaving.value = true
            when (val type = _pdfType.value) {
                is Screen.PdfTools.Type.ImagesToPdf -> {
                    _isSaving.value = true
                    _left.value = imagesToPdfState?.size ?: 0
                    pdfManager.convertImagesToPdf(
                        imageUris = imagesToPdfState?.map { it.toString() } ?: emptyList(),
                        onProgressChange = {
                            _done.value = it
                        },
                        scaleSmallImagesToLarge = _scaleSmallImagesToLarge.value,
                        preset = _presetSelected.value
                    ).let {
                        shareProvider.shareByteArray(
                            byteArray = it,
                            filename = generatePdfFilename() + ".pdf",
                            onComplete = onComplete
                        )
                    }
                    onComplete()
                }

                is Screen.PdfTools.Type.PdfToImages -> {
                    savingJob?.cancel()
                    _done.value = 0
                    _left.value = 1
                    _isSaving.value = false
                    val uris: MutableList<String?> = mutableListOf()
                    savingJob = componentScope.launch {
                        pdfManager.convertPdfToImages(
                            pdfUri = _pdfToImageState.value?.uri.toString(),
                            pages = _pdfToImageState.value?.selectedPages,
                            onProgressChange = { _, bitmap ->
                                imageInfo.copy(
                                    originalUri = _pdfToImageState.value?.uri?.toString()
                                ).let {
                                    imageTransformer.applyPresetBy(
                                        image = bitmap,
                                        preset = _presetSelected.value,
                                        currentInfo = it
                                    )
                                }.apply {
                                    uris.add(
                                        shareProvider.cacheImage(
                                            imageInfo = this,
                                            image = bitmap
                                        )
                                    )
                                }
                                _done.value += 1
                            },
                            preset = presetSelected,
                            onGetPagesCount = { size ->
                                _left.update { size }
                                _isSaving.value = true
                            },
                            onComplete = {
                                _isSaving.value = false
                                shareProvider.shareUris(uris.filterNotNull())
                                onComplete()
                            }
                        )
                    }
                }

                is Screen.PdfTools.Type.Preview -> {
                    type.pdfUri?.toString()?.let {
                        shareProvider.shareUri(
                            uri = it,
                            onComplete = onComplete
                        )
                    }
                }

                null -> Unit
            }
            _isSaving.value = false
        }
    }

    fun addImagesToPdf(uris: List<Uri>) {
        _imagesToPdfState.update {
            it?.plus(uris)?.toSet()?.toList()
        }
        registerChanges()
    }

    fun removeImageToPdfAt(index: Int) {
        runCatching {
            _imagesToPdfState.update {
                it?.toMutableList()?.apply { removeAt(index) }
            }
            registerChanges()
        }
    }

    fun reorderImagesToPdf(uris: List<Uri>?) {
        _imagesToPdfState.update { uris }
        registerChanges()
    }

    fun toggleScaleSmallImagesToLarge() {
        _scaleSmallImagesToLarge.update { !it }
        registerChanges()
    }

    private var presetSelectionJob: Job? by smartJob()

    private fun checkForOOM() {
        val preset = _presetSelected.value
        presetSelectionJob = componentScope.launch {
            runSuspendCatching {
                _pdfToImageState.value?.let { (uri, _, selectedPages) ->
                    val pagesSize = pdfManager.getPdfPageSizes(uri.toString())
                        .filterIndexed { index, _ -> index in selectedPages }
                    _showOOMWarning.update {
                        pagesSize.maxOf { size ->
                            size.width * (preset.value / 100f) * size.height * (preset.value / 100f) * 4
                        } >= 10_000 * 10_000 * 3
                    }
                }
            }.getOrNull() ?: _showOOMWarning.update { false }
        }
        registerChanges()
    }

    fun selectPreset(preset: Preset.Percentage) {
        _presetSelected.update { preset }
        preset.value()?.takeIf { it <= 100f }?.let { quality ->
            _imageInfo.update {
                it.copy(
                    quality = when (val q = it.quality) {
                        is Quality.Base -> q.copy(qualityValue = quality)
                        is Quality.Jxl -> q.copy(qualityValue = quality)
                        else -> q
                    }
                )
            }
        }
        checkForOOM()
    }

    fun updatePdfToImageSelection(ints: List<Int>) {
        _pdfToImageState.update { state ->
            if (state != null) checkForOOM()
            state?.copy(selectedPages = ints.filter { it < state.pagesCount }.sorted())
        }
    }

    fun updateImageFormat(imageFormat: ImageFormat) {
        _imageInfo.update {
            it.copy(imageFormat = imageFormat)
        }
        registerChanges()
    }

    fun setQuality(quality: Quality) {
        _imageInfo.update {
            it.copy(quality = quality)
        }
        registerChanges()
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            initialType: Screen.PdfTools.Type?,
            onGoBack: () -> Unit,
        ): PdfToolsComponent
    }

}