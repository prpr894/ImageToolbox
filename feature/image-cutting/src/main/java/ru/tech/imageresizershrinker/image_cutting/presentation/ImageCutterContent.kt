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

package ru.tech.imageresizershrinker.image_cutting.presentation

import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.toBitmap
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.ui.utils.content_pickers.Picker
import ru.tech.imageresizershrinker.core.ui.utils.content_pickers.rememberImagePicker
import ru.tech.imageresizershrinker.core.ui.utils.helper.ImageUtils.safeAspectRatio
import ru.tech.imageresizershrinker.core.ui.utils.helper.asClip
import ru.tech.imageresizershrinker.core.ui.utils.helper.isPortraitOrientationAsState
import ru.tech.imageresizershrinker.core.ui.utils.provider.rememberLocalEssentials
import ru.tech.imageresizershrinker.core.ui.widget.AdaptiveLayoutScreen
import ru.tech.imageresizershrinker.core.ui.widget.buttons.BottomButtonsBlock
import ru.tech.imageresizershrinker.core.ui.widget.buttons.CompareButton
import ru.tech.imageresizershrinker.core.ui.widget.buttons.ShareButton
import ru.tech.imageresizershrinker.core.ui.widget.buttons.ZoomButton
import ru.tech.imageresizershrinker.core.ui.widget.controls.selection.ImageFormatSelector
import ru.tech.imageresizershrinker.core.ui.widget.controls.selection.QualitySelector
import ru.tech.imageresizershrinker.core.ui.widget.dialogs.ExitWithoutSavingDialog
import ru.tech.imageresizershrinker.core.ui.widget.dialogs.LoadingDialog
import ru.tech.imageresizershrinker.core.ui.widget.dialogs.OneTimeImagePickingDialog
import ru.tech.imageresizershrinker.core.ui.widget.dialogs.OneTimeSaveLocationSelectionDialog
import ru.tech.imageresizershrinker.core.ui.widget.image.AutoFilePicker
import ru.tech.imageresizershrinker.core.ui.widget.image.ImageCounter
import ru.tech.imageresizershrinker.core.ui.widget.image.ImageNotPickedWidget
import ru.tech.imageresizershrinker.core.ui.widget.image.Picture
import ru.tech.imageresizershrinker.core.ui.widget.modifier.container
import ru.tech.imageresizershrinker.core.ui.widget.modifier.detectSwipes
import ru.tech.imageresizershrinker.core.ui.widget.other.TopAppBarEmoji
import ru.tech.imageresizershrinker.core.ui.widget.sheets.PickImageFromUrisSheet
import ru.tech.imageresizershrinker.core.ui.widget.sheets.ProcessImagesPreferenceSheet
import ru.tech.imageresizershrinker.core.ui.widget.sheets.ZoomModalSheet
import ru.tech.imageresizershrinker.core.ui.widget.text.TopAppBarTitle
import ru.tech.imageresizershrinker.feature.compare.presentation.components.CompareSheet
import ru.tech.imageresizershrinker.image_cutting.presentation.components.CutParamsSelector
import ru.tech.imageresizershrinker.image_cutting.presentation.components.rememberCutTransformations
import ru.tech.imageresizershrinker.image_cutting.presentation.screenLogic.ImageCutterComponent

@Composable
fun ImageCutterContent(
    component: ImageCutterComponent
) {
    val essentials = rememberLocalEssentials()
    val showConfetti: () -> Unit = essentials::showConfetti

    val context = LocalContext.current

    val imagePicker = rememberImagePicker(onSuccess = component::updateUris)

    val pickImage = imagePicker::pickImage

    AutoFilePicker(
        onAutoPick = pickImage,
        isPickedAlready = !component.initialUris.isNullOrEmpty()
    )

    val saveBitmaps: (oneTimeSaveLocationUri: String?) -> Unit = {
        component.saveBitmaps(
            oneTimeSaveLocationUri = it,
            onComplete = essentials::parseSaveResults
        )
    }

    val isPortrait by isPortraitOrientationAsState()

    var showExitDialog by rememberSaveable { mutableStateOf(false) }

    val onBack = {
        if (component.haveChanges) showExitDialog = true
        else component.onGoBack()
    }

    var showPickImageFromUrisSheet by rememberSaveable { mutableStateOf(false) }

    AdaptiveLayoutScreen(
        shouldDisableBackHandler = !component.haveChanges,
        title = {
            TopAppBarTitle(
                title = stringResource(R.string.image_cutting),
                input = component.uris,
                isLoading = component.isImageLoading,
                size = null
            )
        },
        onGoBack = onBack,
        actions = {
            var editSheetData by remember {
                mutableStateOf(listOf<Uri>())
            }
            ShareButton(
                enabled = component.uris != null,
                onShare = {
                    component.shareBitmaps(showConfetti)
                },
                onEdit = {
                    component.cacheImages {
                        editSheetData = it
                    }
                },
                onCopy = { manager ->
                    component.cacheCurrentImage {
                        manager.copyToClipboard(it.asClip(context))
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
        },
        imagePreview = {
            Box(
                modifier = Modifier
                    .container()
                    .padding(4.dp)
                    .animateContentSize(
                        alignment = Alignment.Center
                    )
                    .detectSwipes(
                        onSwipeRight = component::selectLeftUri,
                        onSwipeLeft = component::selectRightUri
                    ),
                contentAlignment = Alignment.Center
            ) {
                var aspectRatio by remember {
                    mutableFloatStateOf(1f)
                }
                Picture(
                    model = component.selectedUri,
                    size = 1500,
                    contentScale = ContentScale.FillBounds,
                    transformations = component.rememberCutTransformations(component.selectedUri),
                    modifier = Modifier.aspectRatio(aspectRatio),
                    onSuccess = {
                        aspectRatio = it.result.image.toBitmap().safeAspectRatio
                    },
                    shape = MaterialTheme.shapes.medium,
                    isLoadingFromDifferentPlace = component.isImageLoading
                )
            }
        },
        controls = {
            ImageCounter(
                imageCount = component.uris?.size?.takeIf { it > 1 },
                onRepick = {
                    showPickImageFromUrisSheet = true
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            val params = component.params
            CutParamsSelector(
                value = params,
                onValueChange = component::updateParams
            )
            Spacer(Modifier.height(8.dp))
            ImageFormatSelector(
                value = component.imageFormat,
                onValueChange = component::setImageFormat
            )
            if (component.imageFormat.canChangeCompressionValue) {
                Spacer(Modifier.height(8.dp))
            }
            QualitySelector(
                imageFormat = component.imageFormat,
                quality = component.quality,
                onQualityChange = component::setQuality
            )
        },
        buttons = { actions ->
            var showFolderSelectionDialog by rememberSaveable {
                mutableStateOf(false)
            }
            var showOneTimeImagePickingDialog by rememberSaveable {
                mutableStateOf(false)
            }
            BottomButtonsBlock(
                isNoData = component.uris.isNullOrEmpty(),
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
                picker = Picker.Single,
                imagePicker = imagePicker,
                visible = showOneTimeImagePickingDialog
            )
        },
        topAppBarPersistentActions = {
            if (component.uris.isNullOrEmpty()) {
                TopAppBarEmoji()
            }

            var showZoomSheet by rememberSaveable {
                mutableStateOf(false)
            }
            var showCompareSheet by rememberSaveable {
                mutableStateOf(false)
            }
            CompareButton(
                visible = !component.uris.isNullOrEmpty(),
                onClick = { showCompareSheet = true }
            )
            ZoomButton(
                visible = !component.uris.isNullOrEmpty(),
                onClick = { showZoomSheet = true }
            )

            CompareSheet(
                beforeContent = {
                    var aspectRatio by remember {
                        mutableFloatStateOf(1f)
                    }
                    Picture(
                        model = component.selectedUri,
                        modifier = Modifier.aspectRatio(aspectRatio),
                        onSuccess = {
                            aspectRatio = it.result.image.toBitmap().safeAspectRatio
                        }
                    )
                },
                afterContent = {
                    var aspectRatio by remember {
                        mutableFloatStateOf(1f)
                    }
                    Picture(
                        model = component.selectedUri,
                        transformations = component.rememberCutTransformations(component.selectedUri),
                        modifier = Modifier.aspectRatio(aspectRatio),
                        onSuccess = {
                            aspectRatio = it.result.image.toBitmap().safeAspectRatio
                        }
                    )
                },
                visible = showCompareSheet,
                onDismiss = {
                    showCompareSheet = false
                }
            )

            ZoomModalSheet(
                data = component.selectedUri,
                transformations = component.rememberCutTransformations(component.selectedUri),
                visible = showZoomSheet,
                onDismiss = { showZoomSheet = false }
            )
        },
        canShowScreenData = !component.uris.isNullOrEmpty(),
        noDataControls = {
            if (!component.isImageLoading) {
                ImageNotPickedWidget(onPickImage = pickImage)
            }
        }
    )

    ExitWithoutSavingDialog(
        onExit = component.onGoBack,
        onDismiss = { showExitDialog = false },
        visible = showExitDialog
    )

    PickImageFromUrisSheet(
        visible = showPickImageFromUrisSheet,
        onDismiss = {
            showPickImageFromUrisSheet = false
        },
        transformations = component.rememberCutTransformations(component.uris),
        uris = component.uris,
        selectedUri = component.selectedUri,
        onUriPicked = { uri ->
            component.updateSelectedUri(uri)
        },
        onUriRemoved = { uri ->
            component.updateUrisSilently(removedUri = uri)
        },
        columns = if (isPortrait) 2 else 4,
    )

    LoadingDialog(
        visible = component.isSaving,
        done = component.done,
        left = component.uris?.size ?: -1,
        onCancelLoading = component::cancelSaving
    )
}