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

package ru.tech.imageresizershrinker.feature.image_stacking.presentation

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.ui.theme.mixedContainer
import ru.tech.imageresizershrinker.core.ui.utils.content_pickers.Picker
import ru.tech.imageresizershrinker.core.ui.utils.content_pickers.rememberImagePicker
import ru.tech.imageresizershrinker.core.ui.utils.helper.asClip
import ru.tech.imageresizershrinker.core.ui.utils.helper.isPortraitOrientationAsState
import ru.tech.imageresizershrinker.core.ui.utils.provider.LocalComponentActivity
import ru.tech.imageresizershrinker.core.ui.utils.provider.rememberLocalEssentials
import ru.tech.imageresizershrinker.core.ui.widget.AdaptiveLayoutScreen
import ru.tech.imageresizershrinker.core.ui.widget.buttons.BottomButtonsBlock
import ru.tech.imageresizershrinker.core.ui.widget.buttons.ShareButton
import ru.tech.imageresizershrinker.core.ui.widget.buttons.ZoomButton
import ru.tech.imageresizershrinker.core.ui.widget.controls.ImageReorderCarousel
import ru.tech.imageresizershrinker.core.ui.widget.controls.selection.ImageFormatSelector
import ru.tech.imageresizershrinker.core.ui.widget.controls.selection.QualitySelector
import ru.tech.imageresizershrinker.core.ui.widget.dialogs.ExitWithoutSavingDialog
import ru.tech.imageresizershrinker.core.ui.widget.dialogs.LoadingDialog
import ru.tech.imageresizershrinker.core.ui.widget.dialogs.OneTimeImagePickingDialog
import ru.tech.imageresizershrinker.core.ui.widget.dialogs.OneTimeSaveLocationSelectionDialog
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedButton
import ru.tech.imageresizershrinker.core.ui.widget.image.AutoFilePicker
import ru.tech.imageresizershrinker.core.ui.widget.image.ImageContainer
import ru.tech.imageresizershrinker.core.ui.widget.image.ImageNotPickedWidget
import ru.tech.imageresizershrinker.core.ui.widget.modifier.container
import ru.tech.imageresizershrinker.core.ui.widget.other.TopAppBarEmoji
import ru.tech.imageresizershrinker.core.ui.widget.sheets.ProcessImagesPreferenceSheet
import ru.tech.imageresizershrinker.core.ui.widget.sheets.ZoomModalSheet
import ru.tech.imageresizershrinker.core.ui.widget.text.TitleItem
import ru.tech.imageresizershrinker.core.ui.widget.text.TopAppBarTitle
import ru.tech.imageresizershrinker.core.ui.widget.utils.AutoContentBasedColors
import ru.tech.imageresizershrinker.feature.image_stacking.domain.StackImage
import ru.tech.imageresizershrinker.feature.image_stacking.presentation.components.StackImageItem
import ru.tech.imageresizershrinker.feature.image_stacking.presentation.components.StackingParamsSelector
import ru.tech.imageresizershrinker.feature.image_stacking.presentation.screenLogic.ImageStackingComponent

@Composable
fun ImageStackingContent(
    component: ImageStackingComponent
) {
    val context = LocalComponentActivity.current

    val essentials = rememberLocalEssentials()
    val showConfetti: () -> Unit = essentials::showConfetti

    AutoContentBasedColors(component.previewBitmap)

    val imagePicker = rememberImagePicker(onSuccess = component::updateUris)

    val addImagesImagePicker = rememberImagePicker(onSuccess = component::addUrisToEnd)

    val addImages = addImagesImagePicker::pickImage

    val pickImage = imagePicker::pickImage

    AutoFilePicker(
        onAutoPick = pickImage,
        isPickedAlready = !component.initialUris.isNullOrEmpty()
    )

    var showExitDialog by rememberSaveable { mutableStateOf(false) }

    val onBack = {
        if (component.haveChanges) showExitDialog = true
        else component.onGoBack()
    }

    val saveBitmaps: (oneTimeSaveLocationUri: String?) -> Unit = {
        component.saveBitmaps(
            oneTimeSaveLocationUri = it,
            onComplete = essentials::parseSaveResult
        )
    }

    val isPortrait by isPortraitOrientationAsState()

    var showZoomSheet by rememberSaveable { mutableStateOf(false) }

    ZoomModalSheet(
        data = component.previewBitmap,
        visible = showZoomSheet,
        onDismiss = {
            showZoomSheet = false
        }
    )

    AdaptiveLayoutScreen(
        shouldDisableBackHandler = !component.haveChanges,
        title = {
            TopAppBarTitle(
                title = stringResource(R.string.image_stacking),
                input = component.stackImages,
                isLoading = component.isImageLoading,
                size = component.imageByteSize?.toLong(),
                updateOnSizeChange = false
            )
        },
        onGoBack = onBack,
        actions = {
            var editSheetData by remember {
                mutableStateOf(listOf<Uri>())
            }
            ShareButton(
                enabled = component.previewBitmap != null,
                onShare = {
                    component.shareBitmap(showConfetti)
                },
                onCopy = { manager ->
                    component.cacheCurrentImage { uri ->
                        manager.copyToClipboard(uri.asClip(context))
                        showConfetti()
                    }
                },
                onEdit = {
                    component.cacheCurrentImage {
                        editSheetData = listOf(it)
                    }
                }
            )
            ProcessImagesPreferenceSheet(
                uris = editSheetData,
                visible = editSheetData.isNotEmpty(),
                onDismiss = {
                    editSheetData = emptyList()
                },
                onNavigate = component.onNavigate
            )
            ZoomButton(
                onClick = { showZoomSheet = true },
                visible = component.previewBitmap != null,
            )
        },
        imagePreview = {
            ImageContainer(
                imageInside = isPortrait,
                showOriginal = false,
                previewBitmap = component.previewBitmap,
                originalBitmap = null,
                isLoading = component.isImageLoading,
                shouldShowPreview = true
            )
        },
        topAppBarPersistentActions = {
            if (component.stackImages.isEmpty()) {
                TopAppBarEmoji()
            }
        },
        controls = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ImageReorderCarousel(
                    images = remember(component.stackImages) {
                        derivedStateOf {
                            component.stackImages.map { it.uri.toUri() }
                        }
                    }.value,
                    onReorder = component::reorderUris,
                    onNeedToAddImage = addImages,
                    onNeedToRemoveImageAt = component::removeImageAt
                )
                StackingParamsSelector(
                    value = component.stackingParams,
                    onValueChange = component::updateParams
                )
                Column(Modifier.container(MaterialTheme.shapes.extraLarge)) {
                    TitleItem(
                        text = stringResource(
                            R.string.images,
                            component.stackImages.size
                        )
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(
                            8.dp
                        ),
                        modifier = Modifier.padding(8.dp)
                    ) {
                        component.stackImages.forEachIndexed { index, stackImage ->
                            StackImageItem(
                                backgroundColor = MaterialTheme.colorScheme.surface,
                                stackImage = stackImage,
                                index = index,
                                onStackImageChange = { image: StackImage ->
                                    component.updateStackImage(
                                        value = image,
                                        index = index,
                                        onFailure = essentials::showFailureToast
                                    )
                                },
                                isRemoveVisible = component.stackImages.size > 2,
                                onRemove = {
                                    component.removeImageAt(index)
                                }
                            )
                        }
                        EnhancedButton(
                            containerColor = MaterialTheme.colorScheme.mixedContainer,
                            onClick = addImages,
                            modifier = Modifier.padding(
                                horizontal = 16.dp
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AddCircleOutline,
                                contentDescription = stringResource(R.string.add_image)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(id = R.string.add_image))
                        }
                    }
                }
                QualitySelector(
                    imageFormat = component.imageInfo.imageFormat,
                    enabled = component.stackImages.isNotEmpty(),
                    quality = component.imageInfo.quality,
                    onQualityChange = component::setQuality
                )
                ImageFormatSelector(
                    value = component.imageInfo.imageFormat,
                    onValueChange = component::setImageFormat
                )
            }
        },
        buttons = { actions ->
            var showFolderSelectionDialog by rememberSaveable {
                mutableStateOf(false)
            }
            var showOneTimeImagePickingDialog by rememberSaveable {
                mutableStateOf(false)
            }
            BottomButtonsBlock(
                isNoData = component.stackImages.isEmpty(),
                isPrimaryButtonVisible = component.stackImages.isNotEmpty(),
                onSecondaryButtonClick = pickImage,
                onPrimaryButtonClick = {
                    saveBitmaps(null)
                },
                onPrimaryButtonLongClick = {
                    showFolderSelectionDialog = true
                },
                actions = {
                    if (isPortrait) actions()
                },
                onSecondaryButtonLongClick = {
                    showOneTimeImagePickingDialog = true
                }
            )
            OneTimeSaveLocationSelectionDialog(
                visible = showFolderSelectionDialog,
                onDismiss = { showFolderSelectionDialog = false },
                onSaveRequest = saveBitmaps,
                formatForFilenameSelection = component.getFormatForFilenameSelection()
            )
            OneTimeImagePickingDialog(
                onDismiss = { showOneTimeImagePickingDialog = false },
                picker = Picker.Multiple,
                imagePicker = imagePicker,
                visible = showOneTimeImagePickingDialog
            )
        },
        noDataControls = {
            if (!component.isImageLoading) {
                ImageNotPickedWidget(onPickImage = pickImage)
            }
        },
        canShowScreenData = component.stackImages.isNotEmpty()
    )

    LoadingDialog(
        visible = component.isSaving,
        done = component.done,
        left = component.stackImages.size,
        onCancelLoading = component::cancelSaving
    )

    ExitWithoutSavingDialog(
        onExit = component.onGoBack,
        onDismiss = { showExitDialog = false },
        visible = showExitDialog
    )
}