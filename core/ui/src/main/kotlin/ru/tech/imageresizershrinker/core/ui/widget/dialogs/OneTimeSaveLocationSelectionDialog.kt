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

package ru.tech.imageresizershrinker.core.ui.widget.dialogs

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material.icons.outlined.SaveAs
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import kotlinx.coroutines.launch
import ru.tech.imageresizershrinker.core.domain.image.model.ImageFormat
import ru.tech.imageresizershrinker.core.domain.utils.timestamp
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.settings.domain.model.OneTimeSaveLocation
import ru.tech.imageresizershrinker.core.settings.presentation.provider.LocalSettingsState
import ru.tech.imageresizershrinker.core.settings.presentation.provider.LocalSimpleSettingsInteractor
import ru.tech.imageresizershrinker.core.ui.theme.takeColorFromScheme
import ru.tech.imageresizershrinker.core.ui.utils.helper.toUiPath
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedAlertDialog
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedButton
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.hapticsClickable
import ru.tech.imageresizershrinker.core.ui.widget.modifier.ContainerShapeDefaults
import ru.tech.imageresizershrinker.core.ui.widget.modifier.container
import ru.tech.imageresizershrinker.core.ui.widget.modifier.fadingEdges
import ru.tech.imageresizershrinker.core.ui.widget.other.RevealDirection
import ru.tech.imageresizershrinker.core.ui.widget.other.RevealValue
import ru.tech.imageresizershrinker.core.ui.widget.other.SwipeToReveal
import ru.tech.imageresizershrinker.core.ui.widget.other.rememberRevealState
import ru.tech.imageresizershrinker.core.ui.widget.preferences.PreferenceItem
import ru.tech.imageresizershrinker.core.ui.widget.preferences.PreferenceItemDefaults


@Composable
fun OneTimeSaveLocationSelectionDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onSaveRequest: ((String?) -> Unit)?,
    formatForFilenameSelection: ImageFormat? = null
) {
    val settingsState = LocalSettingsState.current
    var tempSelectedSaveFolderUri by rememberSaveable(visible) {
        mutableStateOf(settingsState.saveFolderUri?.toString())
    }
    var selectedSaveFolderUri by rememberSaveable(visible) {
        mutableStateOf(settingsState.saveFolderUri?.toString())
    }
    EnhancedAlertDialog(
        visible = visible,
        onDismissRequest = onDismiss,
        confirmButton = {
            onSaveRequest?.let {
                EnhancedButton(
                    onClick = {
                        onDismiss()
                        onSaveRequest(selectedSaveFolderUri)
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Text(text = stringResource(id = R.string.save))
                }
            }
        },
        dismissButton = {
            EnhancedButton(
                onClick = onDismiss,
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(text = stringResource(id = R.string.close))
            }
        },
        icon = {
            Icon(
                imageVector = Icons.Outlined.SaveAs,
                contentDescription = stringResource(id = R.string.folder)
            )
        },
        title = {
            Text(text = stringResource(id = R.string.folder))
        },
        text = {
            val data by remember(settingsState.oneTimeSaveLocations, tempSelectedSaveFolderUri) {
                derivedStateOf {
                    settingsState.oneTimeSaveLocations.plus(
                        tempSelectedSaveFolderUri?.let {
                            OneTimeSaveLocation(
                                uri = it,
                                date = null,
                                count = 0
                            )
                        }
                    ).plus(
                        settingsState.saveFolderUri?.toString()?.let {
                            OneTimeSaveLocation(
                                uri = it,
                                date = null,
                                count = 0
                            )
                        }
                    ).distinctBy { it?.uri }
                }
            }

            val context = LocalContext.current
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fadingEdges(
                        scrollableState = scrollState,
                        isVertical = true
                    )
                    .verticalScroll(scrollState)
            ) {
                Spacer(Modifier.height(4.dp))
                data.forEachIndexed { index, item ->
                    val title by remember(item) {
                        derivedStateOf {
                            val default = context.getString(R.string.default_folder)
                            item?.uri?.toUri()?.toUiPath(context, default = default) ?: default
                        }
                    }
                    val subtitle by remember(item) {
                        derivedStateOf {
                            if (item?.uri == settingsState.saveFolderUri?.toString()) {
                                context.getString(R.string.default_value)
                            } else {
                                val time = item?.date?.let {
                                    timestamp(
                                        format = "dd MMMM yyyy",
                                        date = it
                                    )
                                } ?: ""

                                "$time ${
                                    item?.count?.takeIf { it > 0 }
                                        ?.let { "($it)" } ?: ""
                                }".trim()
                                    .takeIf { it.isNotEmpty() }
                            }
                        }
                    }
                    val selected = selectedSaveFolderUri == item?.uri
                    val scope = rememberCoroutineScope()
                    val state = rememberRevealState()
                    val interactionSource = remember {
                        MutableInteractionSource()
                    }
                    val isDragged by interactionSource.collectIsDraggedAsState()
                    val shape = ContainerShapeDefaults.shapeForIndex(
                        index = index,
                        size = data.size + 1,
                        forceDefault = isDragged
                    )
                    val settingsInteractor = LocalSimpleSettingsInteractor.current
                    val canDeleteItem by remember(item, settingsState) {
                        derivedStateOf {
                            item != null && item in settingsState.oneTimeSaveLocations
                        }
                    }

                    SwipeToReveal(
                        state = state,
                        revealedContentEnd = {
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .container(
                                        color = MaterialTheme.colorScheme.errorContainer,
                                        shape = shape,
                                        autoShadowElevation = 0.dp,
                                        resultPadding = 0.dp
                                    )
                                    .hapticsClickable {
                                        scope.launch {
                                            state.animateTo(RevealValue.Default)
                                        }
                                        scope.launch {
                                            settingsInteractor.setOneTimeSaveLocations((settingsState.oneTimeSaveLocations - item).filterNotNull())
                                            if (item?.uri == selectedSaveFolderUri) {
                                                selectedSaveFolderUri = null
                                                tempSelectedSaveFolderUri = null
                                            }
                                        }
                                    }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = stringResource(R.string.delete),
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .padding(end = 8.dp)
                                        .align(Alignment.CenterEnd),
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        },
                        directions = setOf(RevealDirection.EndToStart),
                        swipeableContent = {
                            PreferenceItem(
                                title = title,
                                subtitle = subtitle,
                                shape = shape,
                                titleFontStyle = PreferenceItemDefaults.TitleFontStyleSmall,
                                onClick = {
                                    if (item != null) {
                                        tempSelectedSaveFolderUri = item.uri
                                    }
                                    selectedSaveFolderUri = item?.uri
                                },
                                onLongClick = if (item != null) {
                                    {
                                        scope.launch {
                                            state.animateTo(RevealValue.FullyRevealedStart)
                                        }
                                    }
                                } else null,
                                startIconTransitionSpec = {
                                    fadeIn() togetherWith fadeOut()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                startIcon = if (selected) {
                                    Icons.Rounded.Folder
                                } else Icons.Rounded.FolderOpen,
                                endIcon = if (selected) Icons.Rounded.RadioButtonChecked
                                else Icons.Rounded.RadioButtonUnchecked,
                                color = takeColorFromScheme {
                                    if (selected) surface
                                    else surfaceContainer
                                }
                            )
                        },
                        enableSwipe = canDeleteItem,
                        interactionSource = interactionSource,
                        modifier = Modifier
                            .fadingEdges(
                                scrollableState = null,
                                length = 4.dp
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
                val currentFolderUri = selectedSaveFolderUri?.toUri() ?: settingsState.saveFolderUri
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocumentTree(),
                    onResult = { uri ->
                        uri?.let {
                            context.contentResolver.takePersistableUriPermission(
                                it,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                            )
                            tempSelectedSaveFolderUri = it.toString()
                            selectedSaveFolderUri = it.toString()
                        }
                    }
                )
                PreferenceItem(
                    title = stringResource(id = R.string.add_new_folder),
                    startIcon = Icons.Outlined.CreateNewFolder,
                    shape = ContainerShapeDefaults.bottomShape,
                    titleFontStyle = PreferenceItemDefaults.TitleFontStyleSmall,
                    onClick = {
                        launcher.launch(currentFolderUri)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer
                )

                if (formatForFilenameSelection != null) {
                    val createLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.CreateDocument(formatForFilenameSelection.mimeType),
                        onResult = { uri ->
                            uri?.let {
                                onSaveRequest?.invoke(it.toString())
                                onDismiss()
                            }
                        }
                    )
                    val imageString = stringResource(R.string.image)
                    PreferenceItem(
                        title = stringResource(id = R.string.custom_filename),
                        subtitle = stringResource(id = R.string.custom_filename_sub),
                        startIcon = Icons.Outlined.DriveFileRenameOutline,
                        shape = ContainerShapeDefaults.defaultShape,
                        titleFontStyle = PreferenceItemDefaults.TitleFontStyleSmall,
                        onClick = {
                            createLauncher.launch("$imageString.${formatForFilenameSelection.extension}")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer
                    )
                }
            }
        }
    )
}