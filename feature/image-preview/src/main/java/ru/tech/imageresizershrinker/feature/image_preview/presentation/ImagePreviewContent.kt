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

package ru.tech.imageresizershrinker.feature.image_preview.presentation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.ImageSearch
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.tech.imageresizershrinker.core.domain.image.model.ImageFrames
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.resources.icons.FolderOpened
import ru.tech.imageresizershrinker.core.resources.icons.ImageEdit
import ru.tech.imageresizershrinker.core.settings.presentation.provider.LocalSettingsState
import ru.tech.imageresizershrinker.core.ui.utils.content_pickers.Picker
import ru.tech.imageresizershrinker.core.ui.utils.content_pickers.rememberImagePicker
import ru.tech.imageresizershrinker.core.ui.utils.provider.rememberLocalEssentials
import ru.tech.imageresizershrinker.core.ui.widget.dialogs.ExitBackHandler
import ru.tech.imageresizershrinker.core.ui.widget.dialogs.ExitWithoutSavingDialog
import ru.tech.imageresizershrinker.core.ui.widget.dialogs.LoadingDialog
import ru.tech.imageresizershrinker.core.ui.widget.dialogs.OneTimeImagePickingDialog
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedFloatingActionButton
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedIconButton
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedTopAppBar
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedTopAppBarType
import ru.tech.imageresizershrinker.core.ui.widget.image.ImageNotPickedWidget
import ru.tech.imageresizershrinker.core.ui.widget.image.ImagePreviewGrid
import ru.tech.imageresizershrinker.core.ui.widget.modifier.container
import ru.tech.imageresizershrinker.core.ui.widget.other.TopAppBarEmoji
import ru.tech.imageresizershrinker.core.ui.widget.sheets.ProcessImagesPreferenceSheet
import ru.tech.imageresizershrinker.core.ui.widget.text.marquee
import ru.tech.imageresizershrinker.feature.image_preview.presentation.screenLogic.ImagePreviewComponent

@Composable
fun ImagePreviewContent(
    component: ImagePreviewComponent
) {
    var showExitDialog by rememberSaveable {
        mutableStateOf(false)
    }
    val onBack = {
        if (component.uris.isNullOrEmpty()) component.onGoBack()
        else showExitDialog = true
    }

    val initialShowImagePreviewDialog = !component.initialUris.isNullOrEmpty()

    val settingsState = LocalSettingsState.current

    val essentials = rememberLocalEssentials()
    val showConfetti: () -> Unit = essentials::showConfetti

    val imagePicker = rememberImagePicker(onSuccess = component::updateUris)

    val isLoadingImages = component.isLoadingImages

    var previousFolder by rememberSaveable {
        mutableStateOf<Uri?>(null)
    }
    val openDirectoryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { treeUri ->
            treeUri?.let { uri ->
                previousFolder = uri
                component.updateUrisFromTree(uri)
            }
        }
    )

    val pickDirectory: () -> Unit = {
        runCatching {
            openDirectoryLauncher.launch(previousFolder)
        }.onFailure {
            essentials.showActivateFilesToast()
        }
    }

    val pickImage = imagePicker::pickImage

    val selectedUris by remember(component.uris, component.imageFrames) {
        derivedStateOf {
            component.getSelectedUris() ?: emptyList()
        }
    }
    var wantToEdit by rememberSaveable(selectedUris.isNotEmpty()) {
        mutableStateOf(false)
    }

    Surface(
        color = MaterialTheme.colorScheme.background
    ) {
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        Box(
            Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            var showOneTimeImagePickingDialog by rememberSaveable {
                mutableStateOf(false)
            }

            Column(Modifier.fillMaxSize()) {
                EnhancedTopAppBar(
                    type = EnhancedTopAppBarType.Large,
                    scrollBehavior = scrollBehavior,
                    title = {
                        Text(
                            text = stringResource(R.string.image_preview),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.marquee()
                        )
                    },
                    navigationIcon = {
                        EnhancedIconButton(
                            onClick = component.onGoBack
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = stringResource(R.string.exit)
                            )
                        }
                    },
                    actions = {
                        val isCanClear = selectedUris.isNotEmpty()
                        val isCanSelectAll =
                            component.uris?.size != selectedUris.size && component.uris != null

                        AnimatedVisibility(
                            visible = !isCanSelectAll && !isCanClear || selectedUris.isEmpty()
                        ) {
                            TopAppBarEmoji()
                        }

                        AnimatedVisibility(
                            visible = isCanSelectAll && isCanClear,
                            enter = fadeIn() + scaleIn() + expandHorizontally(),
                            exit = fadeOut() + scaleOut() + shrinkHorizontally()
                        ) {
                            EnhancedIconButton(
                                onClick = {
                                    component.updateImageFrames(ImageFrames.All)
                                }
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
                            visible = isCanClear
                        ) {
                            Row(
                                modifier = Modifier.padding(start = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = selectedUris.size.toString(),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                EnhancedIconButton(
                                    onClick = {
                                        component.updateImageFrames(
                                            ImageFrames.ManualSelection(emptyList())
                                        )
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = stringResource(R.string.close)
                                    )
                                }
                            }
                        }
                    }
                )
                AnimatedVisibility(
                    visible = !isLoadingImages,
                    modifier = Modifier.weight(1f)
                ) {
                    if (component.uris.isNullOrEmpty()) {
                        Column(
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ImageNotPickedWidget(
                                onPickImage = pickImage,
                                modifier = Modifier.padding(
                                    PaddingValues(
                                        bottom = 88.dp + WindowInsets
                                            .navigationBars
                                            .asPaddingValues()
                                            .calculateBottomPadding(),
                                        top = 12.dp,
                                        end = 12.dp,
                                        start = 12.dp
                                    )
                                )
                            )
                        }
                    } else {
                        ImagePreviewGrid(
                            data = component.uris,
                            onAddImages = component::updateUris,
                            onShareImage = {
                                component.shareImages(
                                    uriList = listOf(element = it),
                                    onComplete = showConfetti
                                )
                            },
                            onRemove = component::removeUri,
                            initialShowImagePreviewDialog = initialShowImagePreviewDialog,
                            onNavigate = component.onNavigate,
                            imageFrames = component.imageFrames,
                            onFrameSelectionChange = component::updateImageFrames
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(16.dp)
                    .align(settingsState.fabAlignment)
            ) {
                AnimatedContent(targetState = selectedUris.isNotEmpty()) { isFramesSelected ->
                    if (isFramesSelected) {
                        EnhancedFloatingActionButton(
                            onClick = {
                                wantToEdit = true
                            },
                            content = {
                                Spacer(Modifier.width(16.dp))
                                Icon(
                                    imageVector = Icons.Outlined.ImageEdit,
                                    contentDescription = stringResource(R.string.edit)
                                )
                                Spacer(Modifier.width(16.dp))
                                Text(stringResource(R.string.edit))
                                Spacer(Modifier.width(16.dp))
                            }
                        )
                    } else {
                        EnhancedFloatingActionButton(
                            onClick = pickImage,
                            onLongClick = {
                                showOneTimeImagePickingDialog = true
                            },
                            content = {
                                Spacer(Modifier.width(16.dp))
                                Icon(
                                    imageVector = Icons.Rounded.AddPhotoAlternate,
                                    contentDescription = stringResource(R.string.pick_image_alt)
                                )
                                Spacer(Modifier.width(16.dp))
                                Text(stringResource(R.string.pick_image_alt))
                                Spacer(Modifier.width(16.dp))
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                AnimatedContent(targetState = selectedUris.isNotEmpty()) { isFramesSelected ->
                    if (isFramesSelected) {
                        EnhancedFloatingActionButton(
                            onClick = {
                                component.shareImages(
                                    uriList = null,
                                    onComplete = showConfetti
                                )
                            },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            content = {
                                Icon(
                                    imageVector = Icons.Rounded.Share,
                                    contentDescription = stringResource(R.string.share)
                                )
                            }
                        )
                    } else {
                        EnhancedFloatingActionButton(
                            onClick = pickDirectory,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            content = {
                                Icon(
                                    imageVector = Icons.Rounded.FolderOpened,
                                    contentDescription = stringResource(R.string.folder)
                                )
                            }
                        )
                    }
                }
            }

            OneTimeImagePickingDialog(
                onDismiss = { showOneTimeImagePickingDialog = false },
                picker = Picker.Multiple,
                imagePicker = imagePicker,
                visible = showOneTimeImagePickingDialog
            )

            ProcessImagesPreferenceSheet(
                uris = selectedUris,
                visible = wantToEdit,
                onDismiss = {
                    wantToEdit = false
                },
                onNavigate = component.onNavigate
            )

            ExitBackHandler(
                enabled = !component.uris.isNullOrEmpty(),
                onBack = onBack
            )
        }
    }

    LoadingDialog(
        visible = isLoadingImages,
        canCancel = false
    )

    ExitWithoutSavingDialog(
        onExit = component.onGoBack,
        onDismiss = { showExitDialog = false },
        visible = showExitDialog,
        title = stringResource(id = R.string.image_preview),
        text = stringResource(id = R.string.preview_closing),
        icon = Icons.Outlined.ImageSearch
    )
}