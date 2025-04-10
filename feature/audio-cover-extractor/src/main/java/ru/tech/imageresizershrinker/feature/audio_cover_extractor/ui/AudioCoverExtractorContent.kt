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

package ru.tech.imageresizershrinker.feature.audio_cover_extractor.ui

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.twotone.Album
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.resources.icons.MusicAdd
import ru.tech.imageresizershrinker.core.ui.utils.content_pickers.rememberFilePicker
import ru.tech.imageresizershrinker.core.ui.utils.helper.isPortraitOrientationAsState
import ru.tech.imageresizershrinker.core.ui.utils.provider.rememberLocalEssentials
import ru.tech.imageresizershrinker.core.ui.widget.AdaptiveLayoutScreen
import ru.tech.imageresizershrinker.core.ui.widget.buttons.BottomButtonsBlock
import ru.tech.imageresizershrinker.core.ui.widget.buttons.ShareButton
import ru.tech.imageresizershrinker.core.ui.widget.controls.selection.ImageFormatSelector
import ru.tech.imageresizershrinker.core.ui.widget.controls.selection.QualitySelector
import ru.tech.imageresizershrinker.core.ui.widget.dialogs.ExitWithoutSavingDialog
import ru.tech.imageresizershrinker.core.ui.widget.dialogs.LoadingDialog
import ru.tech.imageresizershrinker.core.ui.widget.dialogs.OneTimeSaveLocationSelectionDialog
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedCircularProgressIndicator
import ru.tech.imageresizershrinker.core.ui.widget.image.AutoFilePicker
import ru.tech.imageresizershrinker.core.ui.widget.image.FileNotPickedWidget
import ru.tech.imageresizershrinker.core.ui.widget.image.UrisPreview
import ru.tech.imageresizershrinker.core.ui.widget.other.TopAppBarEmoji
import ru.tech.imageresizershrinker.core.ui.widget.sheets.ProcessImagesPreferenceSheet
import ru.tech.imageresizershrinker.core.ui.widget.text.marquee
import ru.tech.imageresizershrinker.feature.audio_cover_extractor.ui.screenLogic.AudioCoverExtractorComponent

@Composable
fun AudioCoverExtractorContent(
    component: AudioCoverExtractorComponent
) {
    val essentials = rememberLocalEssentials()
    val showConfetti: () -> Unit = essentials::showConfetti

    val onFailure: (Throwable) -> Unit = essentials::showFailureToast

    LaunchedEffect(component.initialUris, component.covers, onFailure) {
        delay(500)
        if (component.initialUris != null && component.covers.isEmpty()) {
            onFailure(Throwable(essentials.context.getString(R.string.no_covers_found)))
        }
    }

    var showExitDialog by rememberSaveable { mutableStateOf(false) }

    val onBack = {
        if (component.haveChanges) showExitDialog = true
        else component.onGoBack()
    }

    val audioPicker = rememberFilePicker(
        mimeTypes = listOf("audio/*"),
        onSuccess = component::updateCovers
    )

    AutoFilePicker(
        onAutoPick = audioPicker::pickFile,
        isPickedAlready = !component.initialUris.isNullOrEmpty()
    )

    val addAudioPicker = rememberFilePicker(
        mimeTypes = listOf("audio/*"),
        onSuccess = component::addCovers
    )


    val isPortrait by isPortraitOrientationAsState()

    val covers = component.covers

    AdaptiveLayoutScreen(
        shouldDisableBackHandler = !component.haveChanges,
        title = {
            Text(
                text = stringResource(R.string.audio_cover_extractor),
                modifier = Modifier.marquee()
            )
        },
        topAppBarPersistentActions = {
            if (isPortrait) {
                TopAppBarEmoji()
            }
        },
        onGoBack = onBack,
        actions = {
            var editSheetData by remember {
                mutableStateOf(listOf<Uri>())
            }

            ShareButton(
                onShare = {
                    component.performSharing(
                        onComplete = showConfetti
                    )
                },
                onEdit = {
                    component.cacheImages {
                        editSheetData = it
                    }
                },
                enabled = !component.isSaving && covers.isNotEmpty()
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
            val uris by remember(covers) {
                derivedStateOf {
                    covers.map { it.imageCoverUri ?: it.audioUri } + Uri.EMPTY
                }
            }

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
                uris = uris,
                isPortrait = true,
                onRemoveUri = component::removeCover,
                onAddUris = addAudioPicker::pickFile,
                errorContent = { index, width ->
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.scrim.copy(0.5f))
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        val cover = covers[index]

                        val size = (width + 16.dp) / 3f

                        Icon(
                            imageVector = Icons.Rounded.Album,
                            contentDescription = null,
                            modifier = Modifier
                                .size(size)
                                .padding(8.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        AnimatedVisibility(
                            visible = cover.isLoading,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            EnhancedCircularProgressIndicator(
                                modifier = Modifier.size(size)
                            )
                        }
                    }
                },
                showScrimForNonSuccess = false,
                showTransparencyChecker = false,
                filenameSource = { index ->
                    covers[index].audioUri
                }
            )
        },
        showImagePreviewAsStickyHeader = false,
        noDataControls = {
            FileNotPickedWidget(
                onPickFile = audioPicker::pickFile,
                icon = Icons.TwoTone.Album,
                text = stringResource(R.string.pick_audio_to_start)
            )
        },
        controls = {
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
        buttons = {
            val save: (oneTimeSaveLocationUri: String?) -> Unit = {
                component.save(
                    oneTimeSaveLocationUri = it,
                    onResult = essentials::parseSaveResults
                )
            }
            var showFolderSelectionDialog by rememberSaveable {
                mutableStateOf(false)
            }

            val canSave by remember(covers) {
                derivedStateOf {
                    covers.none { it.isLoading }
                }
            }

            BottomButtonsBlock(
                isNoData = covers.isEmpty(),
                isPrimaryButtonEnabled = canSave,
                onSecondaryButtonClick = audioPicker::pickFile,
                isPrimaryButtonVisible = covers.isNotEmpty(),
                secondaryButtonIcon = Icons.Rounded.MusicAdd,
                secondaryButtonText = stringResource(R.string.pick_audio),
                onPrimaryButtonClick = {
                    save(null)
                },
                onPrimaryButtonLongClick = {
                    showFolderSelectionDialog = true
                },
                actions = {
                    if (isPortrait) it()
                },
            )
            OneTimeSaveLocationSelectionDialog(
                visible = showFolderSelectionDialog,
                onDismiss = { showFolderSelectionDialog = false },
                onSaveRequest = save
            )
        },
        canShowScreenData = covers.isNotEmpty()
    )

    ExitWithoutSavingDialog(
        onExit = component.onGoBack,
        onDismiss = { showExitDialog = false },
        visible = showExitDialog
    )

    LoadingDialog(
        visible = component.isSaving,
        done = component.done,
        left = component.left,
        onCancelLoading = component::cancelSaving
    )
}