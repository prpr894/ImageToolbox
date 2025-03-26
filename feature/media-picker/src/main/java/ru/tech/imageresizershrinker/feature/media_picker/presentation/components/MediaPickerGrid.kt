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
package ru.tech.imageresizershrinker.feature.media_picker.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ru.tech.imageresizershrinker.core.domain.utils.safeCast
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.ui.utils.helper.toPx
import ru.tech.imageresizershrinker.core.ui.widget.modifier.dragHandler
import ru.tech.imageresizershrinker.feature.media_picker.domain.model.Media
import ru.tech.imageresizershrinker.feature.media_picker.domain.model.MediaItem
import ru.tech.imageresizershrinker.feature.media_picker.domain.model.MediaState
import ru.tech.imageresizershrinker.feature.media_picker.domain.model.isHeaderKey

@Composable
internal fun MediaPickerGrid(
    state: MediaState,
    isSelectionOfAll: Boolean,
    selectedMedia: SnapshotStateList<Media>,
    allowMultiple: Boolean,
    isButtonVisible: Boolean,
    onRequestManagePermission: () -> Unit,
    isManagePermissionAllowed: Boolean
) {
    val scope = rememberCoroutineScope()
    val stringToday = stringResource(id = R.string.header_today)
    val stringYesterday = stringResource(id = R.string.header_yesterday)
    val gridState = rememberLazyGridState()
    val isCheckVisible = rememberSaveable { mutableStateOf(allowMultiple) }
    val hapticFeedback = LocalHapticFeedback.current

    LaunchedEffect(state.media) {
        gridState.scrollToItem(0)
    }

    var imagePreviewUri by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    val onMediaClick: (Media) -> Unit = {
        if (allowMultiple) {
            if (selectedMedia.contains(it)) selectedMedia.remove(it)
            else selectedMedia.add(it)
        } else {
            if (selectedMedia.contains(it)) selectedMedia.remove(it)
            else {
                if (selectedMedia.isNotEmpty()) selectedMedia[0] = it
                else selectedMedia.add(it)
            }
        }
    }

    val layoutDirection = LocalLayoutDirection.current
    val autoScrollSpeed: MutableState<Float> = remember { mutableFloatStateOf(0f) }
    LaunchedEffect(autoScrollSpeed.value) {
        if (autoScrollSpeed.value != 0f) {
            while (isActive) {
                gridState.scrollBy(autoScrollSpeed.value)
                delay(10)
            }
        }
    }
    val privateSelection = remember {
        mutableStateOf(emptySet<Int>())
    }

    LaunchedEffect(state.mappedMedia, isSelectionOfAll, selectedMedia.size) {
        if (isSelectionOfAll) {
            privateSelection.value = state.mappedMedia.mapIndexedNotNull { index, item ->
                if (item is MediaItem.MediaViewItem && item.media in selectedMedia) {
                    index
                } else null
            }.toSet()
        }
    }

    LaunchedEffect(selectedMedia.size) {
        if (selectedMedia.isEmpty() && isSelectionOfAll) {
            privateSelection.value = emptySet()
        }
    }

    LazyVerticalGrid(
        state = gridState,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .dragHandler(
                enabled = isSelectionOfAll && allowMultiple,
                key = state.mappedMedia,
                lazyGridState = gridState,
                isVertical = true,
                haptics = LocalHapticFeedback.current,
                selectedItems = privateSelection,
                onSelectionChange = { indices ->
                    val order: MutableList<Any> = indices.toMutableList()
                    state.mappedMedia.forEachIndexed { index, mediaItem ->
                        if (index in indices && mediaItem is MediaItem.MediaViewItem) {
                            order.indexOf(index).takeIf { it >= 0 }?.let {
                                order[it] = mediaItem.media
                            }
                        }
                    }
                    selectedMedia.clear()
                    selectedMedia.addAll(order.mapNotNull { it.safeCast() })
                },
                autoScrollSpeed = autoScrollSpeed,
                autoScrollThreshold = 40.dp.toPx(),
                onLongTap = {
                    if (selectedMedia.isEmpty()) {
                        imagePreviewUri =
                            (state.mappedMedia[it + 1] as? MediaItem.MediaViewItem)?.media?.uri
                    }
                },
                shouldHandleLongTap = selectedMedia.isNotEmpty()
            ),
        columns = GridCells.Adaptive(100.dp),
        horizontalArrangement = Arrangement.spacedBy(1.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp),
        contentPadding = PaddingValues(
            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding().plus(
                if (isButtonVisible) 80.dp
                else 0.dp
            ).plus(
                if (selectedMedia.isNotEmpty()) 52.dp
                else 0.dp
            ),
            start = WindowInsets.displayCutout.asPaddingValues()
                .calculateStartPadding(layoutDirection),
            end = WindowInsets.displayCutout.asPaddingValues().calculateEndPadding(layoutDirection)
        )
    ) {
        if (!isManagePermissionAllowed) {
            item(
                span = {
                    GridItemSpan(maxLineSpan)
                }
            ) {
                ManageExternalStorageWarning(onRequestManagePermission)
            }
        }
        items(
            items = state.mappedMedia,
            key = {
                val first = if (it is MediaItem.MediaViewItem) it.media.toString() else it.key
                "$first-${state.mappedMedia.indexOf(it)}"
            },
            contentType = { it.key.startsWith("media_") },
            span = { item ->
                GridItemSpan(if (item.key.isHeaderKey) maxLineSpan else 1)
            }
        ) { item ->
            when (item) {
                is MediaItem.Header -> {
                    val isChecked = rememberSaveable { mutableStateOf(false) }
                    if (allowMultiple) {
                        LaunchedEffect(selectedMedia.size) {
                            // Partial check of media items should not check the header
                            isChecked.value = selectedMedia.containsAll(item.data)
                        }
                    }
                    val title = item.text
                        .replace("Today", stringToday)
                        .replace("Yesterday", stringYesterday)
                    MediaStickyHeader(
                        date = title,
                        showAsBig = item.key.contains("big"),
                        isCheckVisible = isCheckVisible,
                        isChecked = isChecked,
                        onChecked = {
                            if (allowMultiple) {
                                hapticFeedback.performHapticFeedback(
                                    HapticFeedbackType.LongPress
                                )
                                scope.launch {
                                    isChecked.value = !isChecked.value
                                    if (isChecked.value) {
                                        val toAdd = item.data.toMutableList().apply {
                                            // Avoid media from being added twice to selection
                                            removeIf { selectedMedia.contains(it) }
                                        }
                                        selectedMedia.addAll(toAdd)
                                    } else selectedMedia.removeAll(item.data)
                                }
                            }
                        }
                    )
                }

                is MediaItem.MediaViewItem -> {
                    val selectionIndex by remember(selectedMedia, item.media) {
                        derivedStateOf {
                            selectedMedia.indexOf(item.media)
                        }
                    }

                    MediaImage(
                        media = item.media,
                        canClick = !isSelectionOfAll || !allowMultiple,
                        onItemClick = {
                            hapticFeedback.performHapticFeedback(
                                HapticFeedbackType.TextHandleMove
                            )
                            onMediaClick(it)
                        },
                        onItemLongClick = {
                            imagePreviewUri = it.uri
                        },
                        selectionIndex = if (selectedMedia.size > 1) selectionIndex else -1,
                        isSelected = selectionIndex >= 0
                    )
                }
            }
        }
    }

    MediaImagePager(
        imagePreviewUri = imagePreviewUri,
        onDismiss = { imagePreviewUri = null },
        media = state.media,
        selectedMedia = selectedMedia,
        onMediaClick = onMediaClick
    )
}