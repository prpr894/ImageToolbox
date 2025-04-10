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

package ru.tech.imageresizershrinker.feature.filters.presentation

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Badge
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.tech.imageresizershrinker.core.filters.presentation.model.UiFilter
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.ui.utils.content_pickers.rememberImagePicker
import ru.tech.imageresizershrinker.core.ui.utils.helper.asClip
import ru.tech.imageresizershrinker.core.ui.utils.helper.isPortraitOrientationAsState
import ru.tech.imageresizershrinker.core.ui.utils.provider.LocalComponentActivity
import ru.tech.imageresizershrinker.core.ui.utils.provider.rememberLocalEssentials
import ru.tech.imageresizershrinker.core.ui.widget.AdaptiveLayoutScreen
import ru.tech.imageresizershrinker.core.ui.widget.buttons.CompareButton
import ru.tech.imageresizershrinker.core.ui.widget.buttons.ShareButton
import ru.tech.imageresizershrinker.core.ui.widget.buttons.ShowOriginalButton
import ru.tech.imageresizershrinker.core.ui.widget.dialogs.ExitWithoutSavingDialog
import ru.tech.imageresizershrinker.core.ui.widget.dialogs.LoadingDialog
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedIconButton
import ru.tech.imageresizershrinker.core.ui.widget.image.AutoFilePicker
import ru.tech.imageresizershrinker.core.ui.widget.image.ImageContainer
import ru.tech.imageresizershrinker.core.ui.widget.modifier.detectSwipes
import ru.tech.imageresizershrinker.core.ui.widget.modifier.scaleOnTap
import ru.tech.imageresizershrinker.core.ui.widget.sheets.ProcessImagesPreferenceSheet
import ru.tech.imageresizershrinker.core.ui.widget.text.TopAppBarTitle
import ru.tech.imageresizershrinker.core.ui.widget.text.marquee
import ru.tech.imageresizershrinker.core.ui.widget.utils.AutoContentBasedColors
import ru.tech.imageresizershrinker.feature.compare.presentation.components.CompareSheet
import ru.tech.imageresizershrinker.feature.filters.presentation.components.FiltersContentActionButtons
import ru.tech.imageresizershrinker.feature.filters.presentation.components.FiltersContentControls
import ru.tech.imageresizershrinker.feature.filters.presentation.components.FiltersContentNoData
import ru.tech.imageresizershrinker.feature.filters.presentation.components.FiltersContentSheets
import ru.tech.imageresizershrinker.feature.filters.presentation.components.FiltersContentTopAppBarActions
import ru.tech.imageresizershrinker.feature.filters.presentation.screenLogic.FiltersComponent

@Composable
fun FiltersContent(
    component: FiltersComponent
) {
    val context = LocalComponentActivity.current

    val essentials = rememberLocalEssentials()
    val showConfetti: () -> Unit = essentials::showConfetti

    AutoContentBasedColors(component.previewBitmap)

    val imagePicker = rememberImagePicker(onSuccess = component::setBasicFilter)

    val pickSingleImagePicker = rememberImagePicker(onSuccess = component::setMaskFilter)

    var showExitDialog by rememberSaveable { mutableStateOf(false) }

    val onBack = {
        if (component.haveChanges) showExitDialog = true
        else if (component.filterType != null) {
            component.clearType()
        } else component.onGoBack()
    }

    val isPortrait by isPortraitOrientationAsState()

    var showOriginal by remember { mutableStateOf(false) }

    val actions: @Composable RowScope.() -> Unit = {
        Spacer(modifier = Modifier.width(8.dp))
        if (component.bitmap != null) {
            var editSheetData by remember {
                mutableStateOf(listOf<Uri>())
            }
            ShareButton(
                enabled = component.canSave,
                onShare = {
                    component.performSharing(showConfetti)
                },
                onCopy = { manager ->
                    component.cacheCurrentImage { uri ->
                        manager.copyToClipboard(uri.asClip(context))
                        showConfetti()
                    }
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
            ShowOriginalButton(
                canShow = component.canShow(),
                onStateChange = {
                    showOriginal = it
                }
            )
        }
        var showCompareSheet by rememberSaveable { mutableStateOf(false) }
        CompareButton(
            onClick = { showCompareSheet = true },
            visible = component.previewBitmap != null
        )
        CompareSheet(
            data = component.bitmap to component.previewBitmap,
            visible = showCompareSheet,
            onDismiss = {
                showCompareSheet = false
            }
        )

        if (component.bitmap != null && (component.basicFilterState.filters.size >= 2 || component.maskingFilterState.masks.size >= 2)) {
            EnhancedIconButton(
                onClick = component::showReorderSheet
            ) {
                Icon(
                    imageVector = Icons.Rounded.Tune,
                    contentDescription = stringResource(R.string.properties)
                )
            }
        }
    }

    var tempSelectionUris by rememberSaveable {
        mutableStateOf<List<Uri>?>(
            null
        )
    }

    LaunchedEffect(component.isSelectionFilterPickerVisible) {
        if (!component.isSelectionFilterPickerVisible) tempSelectionUris = null
    }

    val selectionFilterPicker = rememberImagePicker { uris: List<Uri> ->
        tempSelectionUris = uris
        if (uris.size > 1) {
            component.setBasicFilter(tempSelectionUris)
        } else {
            component.showSelectionFilterPicker()
        }
    }

    AutoFilePicker(
        onAutoPick = selectionFilterPicker::pickImage,
        isPickedAlready = component.initialType != null
    )

    AdaptiveLayoutScreen(
        shouldDisableBackHandler = !(component.haveChanges || component.filterType != null),
        onGoBack = onBack,
        title = {
            AnimatedContent(
                targetState = component.filterType?.let {
                    stringResource(it.title)
                }
            ) { title ->
                if (title == null) {
                    val text by remember {
                        derivedStateOf {
                            UiFilter.groupedEntries.flatten().size.toString()
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.marquee()
                    ) {
                        Text(
                            text = stringResource(R.string.filter)
                        )
                        Badge(
                            content = {
                                Text(
                                    text = text
                                )
                            },
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary,
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .padding(bottom = 12.dp)
                                .scaleOnTap {
                                    showConfetti()
                                }
                        )
                    }
                } else {
                    TopAppBarTitle(
                        title = title,
                        input = component.bitmap,
                        isLoading = component.isImageLoading,
                        size = component.imageInfo.sizeInBytes.toLong()
                    )
                }
            }
        },
        topAppBarPersistentActions = {
            FiltersContentTopAppBarActions(
                component = component,
                actions = actions
            )
        },
        actions = actions,
        showActionsInTopAppBar = false,
        canShowScreenData = component.filterType != null,
        imagePreview = {
            ImageContainer(
                modifier = Modifier
                    .detectSwipes(
                        onSwipeRight = component::selectLeftUri,
                        onSwipeLeft = component::selectRightUri
                    ),
                imageInside = isPortrait,
                showOriginal = showOriginal,
                previewBitmap = component.previewBitmap,
                originalBitmap = component.bitmap,
                isLoading = component.isImageLoading,
                shouldShowPreview = true,
                animatePreviewChange = false
            )
        },
        forceImagePreviewToMax = showOriginal,
        controls = {
            FiltersContentControls(component)
        },
        buttons = { bottomActions ->
            FiltersContentActionButtons(
                component = component,
                actions = bottomActions,
                imagePicker = imagePicker,
                pickSingleImagePicker = pickSingleImagePicker,
                selectionFilterPicker = selectionFilterPicker
            )
        },
        insetsForNoData = WindowInsets(0),
        noDataControls = {
            FiltersContentNoData(
                component = component,
                imagePicker = imagePicker,
                pickSingleImagePicker = pickSingleImagePicker,
                tempSelectionUris = tempSelectionUris
            )
        },
        contentPadding = animateDpAsState(
            if (component.filterType == null) 12.dp
            else 20.dp
        ).value,
    )

    FiltersContentSheets(component)

    LoadingDialog(
        visible = component.isSaving,
        done = component.done,
        left = component.left,
        onCancelLoading = component::cancelSaving
    )

    ExitWithoutSavingDialog(
        onExit = {
            if (component.filterType != null) {
                component.clearType()
            } else {
                component.onGoBack()
            }
        },
        onDismiss = { showExitDialog = false },
        visible = showExitDialog
    )
}