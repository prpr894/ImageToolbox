/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2025 T8RIN (Malik Mukhametzyanov)
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

package ru.tech.imageresizershrinker.feature.recognize.text.presentation.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.resources.icons.CropSmall
import ru.tech.imageresizershrinker.core.ui.theme.mixedContainer
import ru.tech.imageresizershrinker.core.ui.theme.onMixedContainer
import ru.tech.imageresizershrinker.core.ui.utils.content_pickers.ImagePickerMode
import ru.tech.imageresizershrinker.core.ui.utils.content_pickers.Picker
import ru.tech.imageresizershrinker.core.ui.utils.content_pickers.localImagePickerMode
import ru.tech.imageresizershrinker.core.ui.utils.content_pickers.rememberFilePicker
import ru.tech.imageresizershrinker.core.ui.utils.content_pickers.rememberImagePicker
import ru.tech.imageresizershrinker.core.ui.utils.helper.isPortraitOrientationAsState
import ru.tech.imageresizershrinker.core.ui.utils.navigation.Screen
import ru.tech.imageresizershrinker.core.ui.utils.provider.rememberLocalEssentials
import ru.tech.imageresizershrinker.core.ui.widget.controls.ImageTransformBar
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedIconButton
import ru.tech.imageresizershrinker.core.ui.widget.other.LinkPreviewList
import ru.tech.imageresizershrinker.feature.recognize.text.presentation.screenLogic.RecognizeTextComponent
import ru.tech.imageresizershrinker.feature.single_edit.presentation.components.CropEditOption

@Composable
internal fun RecognizeTextControls(
    component: RecognizeTextComponent
) {
    val type = component.type
    val isExtraction = type is Screen.RecognizeText.Type.Extraction
    val imagePickerMode = localImagePickerMode(Picker.Single)
    val isPortrait by isPortraitOrientationAsState()

    val context = LocalContext.current
    val essentials = rememberLocalEssentials()
    val showConfetti = { essentials.showConfetti() }

    val startRecognition = {
        component.startRecognition(
            onFailure = essentials::showFailureToast
        )
    }

    val editedText = component.editedText

    val captureImageLauncher = rememberImagePicker(ImagePickerMode.CameraCapture) { list ->
        component.updateType(
            type = Screen.RecognizeText.Type.Extraction(list.firstOrNull()),
            onImageSet = startRecognition
        )
    }

    val captureImage = captureImageLauncher::pickImage

    val exportLanguagesPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip"),
        onResult = {
            it?.let { uri ->
                component.exportLanguagesTo(
                    uri = uri,
                    onResult = essentials::parseFileSaveResult
                )
            }
        }
    )

    val importLanguagesPicker = rememberFilePicker(
        mimeTypes = listOf("application/zip")
    ) { uri: Uri ->
        component.importLanguagesFrom(
            uri = uri,
            onSuccess = {
                showConfetti()
                essentials.showToast(
                    message = context.getString(R.string.languages_imported),
                    icon = Icons.Outlined.Language
                )
                startRecognition()
            },
            onFailure = essentials::showFailureToast
        )
    }

    val onExportLanguages: () -> Unit = {
        exportLanguagesPicker.launch(component.generateExportFilename())
    }

    val onImportLanguages: () -> Unit = importLanguagesPicker::pickFile

    if (isExtraction) {
        ImageTransformBar(
            onRotateLeft = component::rotateBitmapLeft,
            onFlip = component::flipImage,
            onRotateRight = component::rotateBitmapRight
        ) {
            if (imagePickerMode != ImagePickerMode.CameraCapture) {
                EnhancedIconButton(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    onClick = captureImage
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CameraAlt,
                        contentDescription = stringResource(R.string.camera)
                    )
                }
                Spacer(Modifier.weight(1f))
            }
            var showCropper by rememberSaveable { mutableStateOf(false) }
            EnhancedIconButton(
                containerColor = MaterialTheme.colorScheme.mixedContainer,
                contentColor = MaterialTheme.colorScheme.onMixedContainer,
                onClick = {
                    showCropper = true
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.CropSmall,
                    contentDescription = stringResource(R.string.crop)
                )
            }
            CropEditOption(
                visible = showCropper,
                onDismiss = { showCropper = false },
                useScaffold = isPortrait,
                bitmap = component.previewBitmap,
                onGetBitmap = component::updateBitmap,
                cropProperties = component.cropProperties,
                setCropAspectRatio = component::setCropAspectRatio,
                setCropMask = component::setCropMask,
                selectedAspectRatio = component.selectedAspectRatio,
                loadImage = component::loadImage
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
    FilterSelectionBar(
        addedFilters = component.filtersAdded,
        onContrastClick = component::toggleContrastFilter,
        onThresholdClick = component::toggleThresholdFilter,
        onSharpnessClick = component::toggleSharpnessFilter
    )
    Spacer(modifier = Modifier.height(16.dp))
    RecognizeLanguageSelector(
        currentRecognitionType = component.recognitionType,
        value = component.selectedLanguages,
        availableLanguages = component.languages,
        onValueChange = { codeList, recognitionType ->
            component.onLanguagesSelected(codeList)
            component.setRecognitionType(recognitionType)
            startRecognition()
        },
        onDeleteLanguage = { language, types ->
            component.deleteLanguage(
                language = language,
                types = types,
                onSuccess = startRecognition
            )
        },
        onImportLanguages = onImportLanguages,
        onExportLanguages = onExportLanguages
    )
    if (isExtraction) {
        LinkPreviewList(
            text = editedText ?: "",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        OCRTextPreviewItem(
            text = editedText,
            onTextEdit = { newText ->
                if (editedText != null) {
                    component.updateEditedText(newText)
                }
            },
            isLoading = component.isTextLoading,
            loadingProgress = component.textLoadingProgress,
            accuracy = component.recognitionData?.accuracy ?: 0
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
    RecognitionTypeSelector(
        value = component.recognitionType,
        onValueChange = { recognitionType ->
            component.setRecognitionType(recognitionType)
            startRecognition()
        }
    )
    Spacer(modifier = Modifier.height(8.dp))
    ModelTypeSelector(
        value = component.segmentationMode,
        onValueChange = {
            component.setSegmentationMode(it)
            startRecognition()
        }
    )
    Spacer(modifier = Modifier.height(8.dp))
    OcrEngineModeSelector(
        value = component.ocrEngineMode,
        onValueChange = {
            component.setOcrEngineMode(it)
            startRecognition()
        }
    )
    Spacer(modifier = Modifier.height(8.dp))
    TessParamsSelector(
        value = component.params,
        onValueChange = {
            component.updateParams(it)
            startRecognition()
        },
        modifier = Modifier.fillMaxWidth()
    )
}