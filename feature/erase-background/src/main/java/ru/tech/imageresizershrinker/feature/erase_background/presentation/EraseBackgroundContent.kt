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

package ru.tech.imageresizershrinker.feature.erase_background.presentation

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ZoomIn
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.tech.imageresizershrinker.core.domain.image.model.ImageFormatGroup
import ru.tech.imageresizershrinker.core.domain.model.pt
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.settings.presentation.provider.LocalSettingsState
import ru.tech.imageresizershrinker.core.settings.presentation.provider.LocalSimpleSettingsInteractor
import ru.tech.imageresizershrinker.core.ui.theme.outlineVariant
import ru.tech.imageresizershrinker.core.ui.utils.content_pickers.Picker
import ru.tech.imageresizershrinker.core.ui.utils.content_pickers.rememberImagePicker
import ru.tech.imageresizershrinker.core.ui.utils.helper.asClip
import ru.tech.imageresizershrinker.core.ui.utils.helper.isPortraitOrientationAsState
import ru.tech.imageresizershrinker.core.ui.utils.provider.LocalComponentActivity
import ru.tech.imageresizershrinker.core.ui.utils.provider.LocalScreenSize
import ru.tech.imageresizershrinker.core.ui.utils.provider.rememberLocalEssentials
import ru.tech.imageresizershrinker.core.ui.widget.AdaptiveBottomScaffoldLayoutScreen
import ru.tech.imageresizershrinker.core.ui.widget.buttons.BottomButtonsBlock
import ru.tech.imageresizershrinker.core.ui.widget.buttons.PanModeButton
import ru.tech.imageresizershrinker.core.ui.widget.buttons.ShareButton
import ru.tech.imageresizershrinker.core.ui.widget.controls.SaveExifWidget
import ru.tech.imageresizershrinker.core.ui.widget.controls.selection.HelperGridParamsSelector
import ru.tech.imageresizershrinker.core.ui.widget.controls.selection.ImageFormatSelector
import ru.tech.imageresizershrinker.core.ui.widget.dialogs.ExitWithoutSavingDialog
import ru.tech.imageresizershrinker.core.ui.widget.dialogs.LoadingDialog
import ru.tech.imageresizershrinker.core.ui.widget.dialogs.OneTimeImagePickingDialog
import ru.tech.imageresizershrinker.core.ui.widget.dialogs.OneTimeSaveLocationSelectionDialog
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedIconButton
import ru.tech.imageresizershrinker.core.ui.widget.image.AutoFilePicker
import ru.tech.imageresizershrinker.core.ui.widget.image.ImageNotPickedWidget
import ru.tech.imageresizershrinker.core.ui.widget.modifier.container
import ru.tech.imageresizershrinker.core.ui.widget.other.BoxAnimatedVisibility
import ru.tech.imageresizershrinker.core.ui.widget.other.DrawLockScreenOrientation
import ru.tech.imageresizershrinker.core.ui.widget.other.TopAppBarEmoji
import ru.tech.imageresizershrinker.core.ui.widget.preferences.PreferenceRowSwitch
import ru.tech.imageresizershrinker.core.ui.widget.saver.PtSaver
import ru.tech.imageresizershrinker.core.ui.widget.sheets.ProcessImagesPreferenceSheet
import ru.tech.imageresizershrinker.core.ui.widget.text.TopAppBarTitle
import ru.tech.imageresizershrinker.core.ui.widget.utils.AutoContentBasedColors
import ru.tech.imageresizershrinker.feature.draw.domain.DrawPathMode
import ru.tech.imageresizershrinker.feature.draw.presentation.components.BrushSoftnessSelector
import ru.tech.imageresizershrinker.feature.draw.presentation.components.DrawPathModeSelector
import ru.tech.imageresizershrinker.feature.draw.presentation.components.LineWidthSelector
import ru.tech.imageresizershrinker.feature.erase_background.presentation.components.AutoEraseBackgroundCard
import ru.tech.imageresizershrinker.feature.erase_background.presentation.components.BitmapEraser
import ru.tech.imageresizershrinker.feature.erase_background.presentation.components.OriginalImagePreviewAlphaSelector
import ru.tech.imageresizershrinker.feature.erase_background.presentation.components.RecoverModeButton
import ru.tech.imageresizershrinker.feature.erase_background.presentation.components.RecoverModeCard
import ru.tech.imageresizershrinker.feature.erase_background.presentation.components.TrimImageToggle
import ru.tech.imageresizershrinker.feature.erase_background.presentation.screenLogic.EraseBackgroundComponent

@Composable
fun EraseBackgroundContent(
    component: EraseBackgroundComponent,
) {
    val settingsState = LocalSettingsState.current
    val context = LocalComponentActivity.current

    val essentials = rememberLocalEssentials()
    val scope = essentials.coroutineScope
    val showConfetti: () -> Unit = essentials::showConfetti

    AutoContentBasedColors(component.bitmap)

    var showExitDialog by rememberSaveable { mutableStateOf(false) }


    val imagePicker = rememberImagePicker { uri: Uri ->
        component.setUri(
            uri = uri,
            onFailure = essentials::showFailureToast
        )
    }

    val pickImage = imagePicker::pickImage

    AutoFilePicker(
        onAutoPick = pickImage,
        isPickedAlready = component.initialUri != null
    )

    val onBack = {
        if (component.haveChanges) showExitDialog = true
        else component.onGoBack()
    }

    val saveBitmap: (oneTimeSaveLocationUri: String?) -> Unit = {
        component.saveBitmap(
            oneTimeSaveLocationUri = it,
            onComplete = essentials::parseSaveResult
        )
    }

    var strokeWidth by rememberSaveable(stateSaver = PtSaver) {
        mutableStateOf(
            settingsState.defaultDrawLineWidth.pt
        )
    }
    var brushSoftness by rememberSaveable(stateSaver = PtSaver) {
        mutableStateOf(
            0.pt
        )
    }

    val drawPathMode = component.drawPathMode

    var originalImagePreviewAlpha by rememberSaveable {
        mutableFloatStateOf(0.2f)
    }

    val screenSize = LocalScreenSize.current
    val isPortrait by isPortraitOrientationAsState()

    var panEnabled by rememberSaveable { mutableStateOf(false) }

    val secondaryControls = @Composable {
        PanModeButton(
            selected = panEnabled,
            onClick = {
                panEnabled = !panEnabled
            }
        )
        EnhancedIconButton(
            containerColor = Color.Transparent,
            borderColor = MaterialTheme.colorScheme.outlineVariant(
                luminance = 0.1f
            ),
            onClick = { component.undo() },
            enabled = component.lastPaths.isNotEmpty() || component.paths.isNotEmpty()
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Undo,
                contentDescription = "Undo"
            )
        }
        EnhancedIconButton(
            containerColor = Color.Transparent,
            borderColor = MaterialTheme.colorScheme.outlineVariant(
                luminance = 0.1f
            ),
            onClick = { component.redo() },
            enabled = component.undonePaths.isNotEmpty()
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Redo,
                contentDescription = "Redo"
            )
        }
    }

    AdaptiveBottomScaffoldLayoutScreen(
        title = {
            TopAppBarTitle(
                title = stringResource(R.string.background_remover),
                input = component.bitmap,
                isLoading = component.isImageLoading,
                size = null,
                originalSize = null
            )
        },
        onGoBack = onBack,
        shouldDisableBackHandler = !component.haveChanges,
        actions = {
            secondaryControls()
            RecoverModeButton(
                selected = component.isRecoveryOn,
                enabled = !panEnabled,
                onClick = component::toggleEraser
            )
        },
        topAppBarPersistentActions = { scaffoldState ->
            if (component.bitmap == null) TopAppBarEmoji()
            else {
                if (isPortrait) {
                    EnhancedIconButton(
                        onClick = {
                            scope.launch {
                                if (scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
                                    scaffoldState.bottomSheetState.partialExpand()
                                } else {
                                    scaffoldState.bottomSheetState.expand()
                                }
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = stringResource(R.string.properties)
                        )
                    }
                }
                var editSheetData by remember {
                    mutableStateOf(listOf<Uri>())
                }
                ShareButton(
                    enabled = component.bitmap != null,
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
                        component.cacheCurrentImage { uri ->
                            editSheetData = listOf(uri)
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
                EnhancedIconButton(
                    onClick = { component.clearDrawing() },
                    enabled = component.paths.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.delete)
                    )
                }
            }
        },
        mainContent = {
            AnimatedContent(
                targetState = remember(component.bitmap) {
                    derivedStateOf {
                        component.bitmap?.copy(
                            Bitmap.Config.ARGB_8888,
                            true
                        )?.asImageBitmap() ?: ImageBitmap(
                            screenSize.widthPx,
                            screenSize.heightPx
                        )
                    }
                }.value,
                transitionSpec = { fadeIn() togetherWith fadeOut() }
            ) { imageBitmap ->
                val direction = LocalLayoutDirection.current
                val aspectRatio = imageBitmap.width / imageBitmap.height.toFloat()
                BitmapEraser(
                    imageBitmapForShader = component.internalBitmap?.asImageBitmap(),
                    imageBitmap = imageBitmap,
                    paths = component.paths,
                    strokeWidth = strokeWidth,
                    brushSoftness = brushSoftness,
                    onAddPath = component::addPath,
                    isRecoveryOn = component.isRecoveryOn,
                    modifier = Modifier
                        .padding(
                            start = WindowInsets
                                .displayCutout
                                .asPaddingValues()
                                .calculateStartPadding(direction)
                        )
                        .padding(16.dp)
                        .aspectRatio(
                            ratio = aspectRatio,
                            matchHeightConstraintsFirst = isPortrait
                        )
                        .fillMaxSize(),
                    panEnabled = panEnabled,
                    originalImagePreviewAlpha = originalImagePreviewAlpha,
                    drawPathMode = drawPathMode,
                    helperGridParams = component.helperGridParams
                )
            }
        },
        controls = { scaffoldState ->
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!isPortrait) {
                    Row(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .container(shape = CircleShape)
                    ) {
                        secondaryControls()
                    }
                }
                RecoverModeCard(
                    modifier = Modifier.fillMaxWidth(),
                    selected = component.isRecoveryOn,
                    enabled = !panEnabled,
                    onClick = component::toggleEraser
                )
                AutoEraseBackgroundCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        scope.launch {
                            scaffoldState.bottomSheetState.partialExpand()
                            component.autoEraseBackground(
                                onSuccess = showConfetti,
                                onFailure = essentials::showFailureToast
                            )
                        }
                    },
                    onReset = component::resetImage
                )
                OriginalImagePreviewAlphaSelector(
                    value = originalImagePreviewAlpha,
                    onValueChange = {
                        originalImagePreviewAlpha = it
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                DrawPathModeSelector(
                    modifier = Modifier.fillMaxWidth(),
                    value = drawPathMode,
                    onValueChange = component::updateDrawPathMode,
                    values = remember {
                        listOf(
                            DrawPathMode.Free,
                            DrawPathMode.Line,
                            DrawPathMode.Lasso,
                            DrawPathMode.Rect(),
                            DrawPathMode.Oval
                        )
                    }
                )
                BoxAnimatedVisibility(drawPathMode.isStroke) {
                    LineWidthSelector(
                        modifier = Modifier.fillMaxWidth(),
                        value = strokeWidth.value,
                        onValueChange = { strokeWidth = it.pt }
                    )
                }
                BrushSoftnessSelector(
                    modifier = Modifier.fillMaxWidth(),
                    value = brushSoftness.value,
                    onValueChange = { brushSoftness = it.pt }
                )
                TrimImageToggle(
                    checked = component.trimImage,
                    onCheckedChange = { component.setTrimImage(it) },
                    modifier = Modifier.fillMaxWidth()
                )
                HelperGridParamsSelector(
                    value = component.helperGridParams,
                    onValueChange = component::updateHelperGridParams,
                    modifier = Modifier.fillMaxWidth()
                )
                val settingsInteractor = LocalSimpleSettingsInteractor.current
                PreferenceRowSwitch(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    title = stringResource(R.string.magnifier),
                    subtitle = stringResource(R.string.magnifier_sub),
                    checked = settingsState.magnifierEnabled,
                    onClick = {
                        scope.launch {
                            settingsInteractor.toggleMagnifierEnabled()
                        }
                    },
                    startIcon = Icons.Outlined.ZoomIn
                )
                SaveExifWidget(
                    imageFormat = component.imageFormat,
                    checked = component.saveExif,
                    onCheckedChange = component::setSaveExif,
                    modifier = Modifier.fillMaxWidth()
                )
                ImageFormatSelector(
                    modifier = Modifier.navigationBarsPadding(),
                    entries = ImageFormatGroup.alphaContainedEntries,
                    value = component.imageFormat,
                    onValueChange = {
                        component.setImageFormat(it)
                    }
                )
            }
        },
        buttons = {
            var showOneTimeImagePickingDialog by rememberSaveable {
                mutableStateOf(false)
            }
            var showFolderSelectionDialog by rememberSaveable {
                mutableStateOf(false)
            }
            BottomButtonsBlock(
                isNoData = component.bitmap == null,
                onSecondaryButtonClick = pickImage,
                onSecondaryButtonLongClick = {
                    showOneTimeImagePickingDialog = true
                },
                onPrimaryButtonClick = {
                    saveBitmap(null)
                },
                onPrimaryButtonLongClick = {
                    showFolderSelectionDialog = true
                },
                actions = {
                    if (isPortrait) it()
                }
            )
            OneTimeSaveLocationSelectionDialog(
                visible = showFolderSelectionDialog,
                onDismiss = { showFolderSelectionDialog = false },
                onSaveRequest = saveBitmap,
                formatForFilenameSelection = component.getFormatForFilenameSelection()
            )
            OneTimeImagePickingDialog(
                onDismiss = { showOneTimeImagePickingDialog = false },
                picker = Picker.Single,
                imagePicker = imagePicker,
                visible = showOneTimeImagePickingDialog
            )
        },
        noDataControls = {
            ImageNotPickedWidget(
                onPickImage = pickImage
            )
        },
        canShowScreenData = component.bitmap != null,
        showActionsInTopAppBar = false,
        mainContentWeight = 0.65f
    )

    LoadingDialog(
        visible = component.isSaving || component.isImageLoading || component.isErasingBG,
        onCancelLoading = component::cancelSaving,
        canCancel = component.isSaving
    )

    ExitWithoutSavingDialog(
        onExit = component.onGoBack,
        onDismiss = { showExitDialog = false },
        visible = showExitDialog
    )

    DrawLockScreenOrientation()
}