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

package com.t8rin.imagetoolbox.feature.apng_tools.presentation

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material.icons.rounded.Close
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
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormatGroup
import com.t8rin.imagetoolbox.core.domain.model.MimeType
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.Apng
import com.t8rin.imagetoolbox.core.resources.icons.Jxl
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.Picker
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberFileCreator
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberFilePicker
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberImagePicker
import com.t8rin.imagetoolbox.core.ui.utils.helper.isApng
import com.t8rin.imagetoolbox.core.ui.utils.helper.isPortraitOrientationAsState
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.utils.provider.rememberLocalEssentials
import com.t8rin.imagetoolbox.core.ui.widget.AdaptiveLayoutScreen
import com.t8rin.imagetoolbox.core.ui.widget.buttons.BottomButtonsBlock
import com.t8rin.imagetoolbox.core.ui.widget.buttons.ShareButton
import com.t8rin.imagetoolbox.core.ui.widget.controls.ImageReorderCarousel
import com.t8rin.imagetoolbox.core.ui.widget.controls.selection.ImageFormatSelector
import com.t8rin.imagetoolbox.core.ui.widget.controls.selection.QualitySelector
import com.t8rin.imagetoolbox.core.ui.widget.dialogs.ExitWithoutSavingDialog
import com.t8rin.imagetoolbox.core.ui.widget.dialogs.LoadingDialog
import com.t8rin.imagetoolbox.core.ui.widget.dialogs.OneTimeImagePickingDialog
import com.t8rin.imagetoolbox.core.ui.widget.dialogs.OneTimeSaveLocationSelectionDialog
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedIconButton
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedLoadingIndicator
import com.t8rin.imagetoolbox.core.ui.widget.image.ImagesPreviewWithSelection
import com.t8rin.imagetoolbox.core.ui.widget.image.UrisPreview
import com.t8rin.imagetoolbox.core.ui.widget.modifier.container
import com.t8rin.imagetoolbox.core.ui.widget.modifier.withModifier
import com.t8rin.imagetoolbox.core.ui.widget.other.TopAppBarEmoji
import com.t8rin.imagetoolbox.core.ui.widget.preferences.PreferenceItem
import com.t8rin.imagetoolbox.core.ui.widget.sheets.ProcessImagesPreferenceSheet
import com.t8rin.imagetoolbox.core.ui.widget.text.TopAppBarTitle
import com.t8rin.imagetoolbox.feature.apng_tools.presentation.components.ApngParamsSelector
import com.t8rin.imagetoolbox.feature.apng_tools.presentation.screenLogic.ApngToolsComponent

@Composable
fun ApngToolsContent(
    component: ApngToolsComponent
) {
    val context = LocalContext.current

    val essentials = rememberLocalEssentials()
    val showConfetti: () -> Unit = essentials::showConfetti

    val imagePicker = rememberImagePicker(onSuccess = component::setImageUris)

    val pickSingleApngLauncher = rememberFilePicker(
        mimeType = MimeType.Png,
        onSuccess = { uri: Uri ->
            if (uri.isApng(context)) {
                component.setApngUri(uri)
            } else {
                essentials.showToast(
                    message = context.getString(R.string.select_apng_image_to_start),
                    icon = Icons.Rounded.Apng
                )
            }
        }
    )

    val pickMultipleApngLauncher = rememberFilePicker(
        mimeType = MimeType.Png,
        onSuccess = { list: List<Uri> ->
            list.filter {
                it.isApng(context)
            }.let { uris ->
                if (uris.isEmpty()) {
                    essentials.showToast(
                        message = context.getString(R.string.select_apng_image_to_start),
                        icon = Icons.Rounded.Apng
                    )
                } else {
                    component.setType(
                        Screen.ApngTools.Type.ApngToJxl(uris)
                    )
                }
            }
        }
    )

    val addApngLauncher = rememberFilePicker(
        mimeType = MimeType.Png,
        onSuccess = { list: List<Uri> ->
            list.filter {
                it.isApng(context)
            }.let { uris ->
                if (uris.isEmpty()) {
                    essentials.showToast(
                        message = context.getString(R.string.select_gif_image_to_start),
                        icon = Icons.Filled.Jxl
                    )
                } else {
                    component.setType(
                        Screen.ApngTools.Type.ApngToJxl(
                            (component.type as? Screen.ApngTools.Type.ApngToJxl)?.apngUris?.plus(
                                uris
                            )?.distinct()
                        )
                    )
                }
            }
        }
    )

    val saveApngLauncher = rememberFileCreator(
        mimeType = MimeType.Apng,
        onSuccess = { uri ->
            component.saveApngTo(
                uri = uri,
                onResult = essentials::parseFileSaveResult
            )
        }
    )

    var showExitDialog by rememberSaveable { mutableStateOf(false) }

    val onBack = {
        if (component.haveChanges) showExitDialog = true
        else component.onGoBack()
    }

    val isPortrait by isPortraitOrientationAsState()

    AdaptiveLayoutScreen(
        shouldDisableBackHandler = !component.haveChanges,
        title = {
            TopAppBarTitle(
                title = when (val type = component.type) {
                    null -> stringResource(R.string.apng_tools)
                    else -> stringResource(type.title)
                },
                input = component.type,
                isLoading = component.isLoading,
                size = null
            )
        },
        onGoBack = onBack,
        topAppBarPersistentActions = {
            if (component.type == null) TopAppBarEmoji()
            val pagesSize by remember(component.imageFrames, component.convertedImageUris) {
                derivedStateOf {
                    component.imageFrames.getFramePositions(component.convertedImageUris.size).size
                }
            }
            val isApngToImage = component.type is Screen.ApngTools.Type.ApngToImage
            AnimatedVisibility(
                visible = isApngToImage && pagesSize != component.convertedImageUris.size,
                enter = fadeIn() + scaleIn() + expandHorizontally(),
                exit = fadeOut() + scaleOut() + shrinkHorizontally()
            ) {
                EnhancedIconButton(
                    onClick = component::selectAllConvertedImages
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SelectAll,
                        contentDescription = "Select All"
                    )
                }
            }
            AnimatedVisibility(
                modifier = Modifier
                    .padding(8.dp)
                    .container(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        resultPadding = 0.dp
                    ),
                visible = isApngToImage && pagesSize != 0
            ) {
                Row(
                    modifier = Modifier.padding(start = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    pagesSize.takeIf { it != 0 }?.let {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = it.toString(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    EnhancedIconButton(
                        onClick = component::clearConvertedImagesSelection
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.close)
                        )
                    }
                }
            }
        },
        actions = {
            var editSheetData by remember {
                mutableStateOf(listOf<Uri>())
            }
            ShareButton(
                enabled = !component.isLoading && component.type != null,
                onShare = {
                    component.performSharing(showConfetti)
                },
                onEdit = {
                    component.cacheImages {
                        editSheetData = it
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
            AnimatedContent(
                targetState = component.isLoading to component.type
            ) { (loading, type) ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = if (loading) {
                        Modifier.padding(32.dp)
                    } else Modifier
                ) {
                    if (loading || type == null) {
                        EnhancedLoadingIndicator()
                    } else {
                        when (type) {
                            is Screen.ApngTools.Type.ApngToImage -> {
                                ImagesPreviewWithSelection(
                                    imageUris = component.convertedImageUris,
                                    imageFrames = component.imageFrames,
                                    onFrameSelectionChange = component::updateApngFrames,
                                    isPortrait = isPortrait,
                                    isLoadingImages = component.isLoadingApngImages
                                )
                            }

                            is Screen.ApngTools.Type.ApngToJxl -> {
                                UrisPreview(
                                    modifier = Modifier
                                        .then(
                                            if (!isPortrait) {
                                                Modifier
                                                    .layout { measurable, constraints ->
                                                        val placeable = measurable.measure(
                                                            constraints = constraints.copy(
                                                                maxHeight = constraints.maxHeight + 48.dp.roundToPx()
                                                            )
                                                        )
                                                        layout(placeable.width, placeable.height) {
                                                            placeable.place(0, 0)
                                                        }
                                                    }
                                                    .verticalScroll(rememberScrollState())
                                            } else Modifier
                                        )
                                        .padding(vertical = 24.dp),
                                    uris = type.apngUris ?: emptyList(),
                                    isPortrait = true,
                                    onRemoveUri = {
                                        component.setType(
                                            Screen.ApngTools.Type.ApngToJxl(type.apngUris?.minus(it))
                                        )
                                    },
                                    onAddUris = addApngLauncher::pickFile
                                )
                            }

                            is Screen.ApngTools.Type.ImageToApng -> Unit
                        }
                    }
                }
            }
        },
        placeImagePreview = component.type !is Screen.ApngTools.Type.ImageToApng,
        showImagePreviewAsStickyHeader = false,
        autoClearFocus = false,
        controls = {
            when (val type = component.type) {
                is Screen.ApngTools.Type.ApngToImage -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    ImageFormatSelector(
                        value = component.imageFormat,
                        onValueChange = component::setImageFormat,
                        entries = ImageFormatGroup.alphaContainedEntries
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    QualitySelector(
                        imageFormat = component.imageFormat,
                        enabled = true,
                        quality = component.params.quality,
                        onQualityChange = component::setQuality
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                is Screen.ApngTools.Type.ImageToApng -> {
                    val addImagesToPdfPicker =
                        rememberImagePicker(onSuccess = component::addImageToUris)

                    Spacer(modifier = Modifier.height(16.dp))
                    ImageReorderCarousel(
                        images = type.imageUris,
                        onReorder = component::reorderImageUris,
                        onNeedToAddImage = addImagesToPdfPicker::pickImage,
                        onNeedToRemoveImageAt = component::removeImageAt,
                        onNavigate = component.onNavigate
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ApngParamsSelector(
                        value = component.params,
                        onValueChange = component::updateParams
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                is Screen.ApngTools.Type.ApngToJxl -> {
                    QualitySelector(
                        imageFormat = ImageFormat.Jxl.Lossy,
                        enabled = true,
                        quality = component.jxlQuality,
                        onQualityChange = component::setJxlQuality
                    )
                }

                null -> Unit
            }
        },
        contentPadding = animateDpAsState(
            if (component.type == null) 12.dp
            else 20.dp
        ).value,
        buttons = {
            val saveBitmaps: (oneTimeSaveLocationUri: String?) -> Unit = {
                component.saveBitmaps(
                    oneTimeSaveLocationUri = it,
                    onApngSaveResult = saveApngLauncher::make,
                    onResult = essentials::parseSaveResults
                )
            }
            var showFolderSelectionDialog by rememberSaveable {
                mutableStateOf(false)
            }
            var showOneTimeImagePickingDialog by rememberSaveable {
                mutableStateOf(false)
            }
            BottomButtonsBlock(
                isNoData = component.type == null,
                onSecondaryButtonClick = {
                    when (component.type) {
                        is Screen.ApngTools.Type.ApngToImage -> pickSingleApngLauncher.pickFile()
                        is Screen.ApngTools.Type.ApngToJxl -> pickMultipleApngLauncher.pickFile()
                        else -> imagePicker.pickImage()
                    }
                },
                isPrimaryButtonVisible = component.canSave,
                onPrimaryButtonClick = {
                    saveBitmaps(null)
                },
                onPrimaryButtonLongClick = {
                    if (component.type is Screen.ApngTools.Type.ImageToApng) {
                        saveBitmaps(null)
                    } else showFolderSelectionDialog = true
                },
                actions = {
                    if (isPortrait) it()
                },
                showNullDataButtonAsContainer = true,
                onSecondaryButtonLongClick = if (component.type is Screen.ApngTools.Type.ImageToApng || component.type == null) {
                    {
                        showOneTimeImagePickingDialog = true
                    }
                } else null
            )
            OneTimeSaveLocationSelectionDialog(
                visible = showFolderSelectionDialog,
                onDismiss = { showFolderSelectionDialog = false },
                onSaveRequest = saveBitmaps
            )
            OneTimeImagePickingDialog(
                onDismiss = { showOneTimeImagePickingDialog = false },
                picker = Picker.Multiple,
                imagePicker = imagePicker,
                visible = showOneTimeImagePickingDialog
            )
        },
        insetsForNoData = WindowInsets(0),
        noDataControls = {
            val types = remember {
                Screen.ApngTools.Type.entries
            }
            val preference1 = @Composable {
                PreferenceItem(
                    title = stringResource(types[0].title),
                    subtitle = stringResource(types[0].subtitle),
                    startIcon = types[0].icon,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = imagePicker::pickImage
                )
            }
            val preference2 = @Composable {
                PreferenceItem(
                    title = stringResource(types[1].title),
                    subtitle = stringResource(types[1].subtitle),
                    startIcon = types[1].icon,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = pickSingleApngLauncher::pickFile
                )
            }
            val preference3 = @Composable {
                PreferenceItem(
                    title = stringResource(types[2].title),
                    subtitle = stringResource(types[2].subtitle),
                    startIcon = types[2].icon,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = pickMultipleApngLauncher::pickFile
                )
            }

            if (isPortrait) {
                Column {
                    preference1()
                    Spacer(modifier = Modifier.height(8.dp))
                    preference2()
                    Spacer(modifier = Modifier.height(8.dp))
                    preference3()
                }
            } else {
                val direction = LocalLayoutDirection.current
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.padding(
                            WindowInsets.displayCutout.asPaddingValues().let {
                                PaddingValues(
                                    start = it.calculateStartPadding(direction),
                                    end = it.calculateEndPadding(direction)
                                )
                            }
                        )
                    ) {
                        preference1.withModifier(modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        preference2.withModifier(modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    preference3.withModifier(modifier = Modifier.fillMaxWidth(0.5f))
                }
            }
        },
        canShowScreenData = component.type != null
    )

    LoadingDialog(
        visible = component.isSaving,
        done = component.done,
        left = component.left,
        onCancelLoading = component::cancelSaving
    )

    ExitWithoutSavingDialog(
        onExit = component::clearAll,
        onDismiss = { showExitDialog = false },
        visible = showExitDialog
    )
}