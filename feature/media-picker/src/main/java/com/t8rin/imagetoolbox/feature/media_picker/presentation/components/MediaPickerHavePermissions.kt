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

package com.t8rin.imagetoolbox.feature.media_picker.presentation.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedChip
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedIconButton
import com.t8rin.imagetoolbox.core.ui.widget.image.Picture
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.t8rin.imagetoolbox.core.ui.widget.modifier.animateContentSizeNoClip
import com.t8rin.imagetoolbox.core.ui.widget.modifier.drawHorizontalStroke
import com.t8rin.imagetoolbox.core.ui.widget.modifier.fadingEdges
import com.t8rin.imagetoolbox.core.ui.widget.other.BoxAnimatedVisibility
import com.t8rin.imagetoolbox.core.ui.widget.text.AutoSizeText
import com.t8rin.imagetoolbox.feature.media_picker.domain.model.Album
import com.t8rin.imagetoolbox.feature.media_picker.domain.model.AllowedMedia
import com.t8rin.imagetoolbox.feature.media_picker.presentation.screenLogic.MediaPickerComponent

@Composable
internal fun MediaPickerHavePermissions(
    component: MediaPickerComponent,
    allowedMedia: AllowedMedia,
    allowMultiple: Boolean,
    onRequestManagePermission: () -> Unit,
    isManagePermissionAllowed: Boolean
) {
    var selectedAlbumIndex by rememberSaveable { mutableLongStateOf(-1) }

    val albumsState by component.albumsState.collectAsState()
    var isSearching by rememberSaveable {
        mutableStateOf(false)
    }

    Column {
        AnimatedVisibility(
            visible = albumsState.albums.size > 1
        ) {
            val layoutDirection = LocalLayoutDirection.current
            var albumsExpanded by rememberSaveable {
                mutableStateOf(false)
            }
            val listState = rememberLazyListState()
            Column(
                modifier = Modifier
                    .drawHorizontalStroke()
                    .background(MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BoxAnimatedVisibility(
                        visible = !albumsExpanded,
                        modifier = Modifier.weight(1f)
                    ) {
                        LazyRow(
                            modifier = Modifier
                                .fadingEdges(listState)
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(
                                space = 8.dp,
                                alignment = Alignment.CenterHorizontally
                            ),
                            contentPadding = PaddingValues(
                                start = WindowInsets.displayCutout
                                    .asPaddingValues()
                                    .calculateStartPadding(layoutDirection) + 8.dp,
                                end = WindowInsets.displayCutout
                                    .asPaddingValues()
                                    .calculateEndPadding(layoutDirection) + 8.dp
                            ),
                            state = listState
                        ) {
                            items(
                                items = albumsState.albums,
                                key = { "album_${it.id}_collapsed" }
                            ) {
                                AlbumChip(
                                    album = it,
                                    selected = selectedAlbumIndex == it.id,
                                    isImageVisible = false,
                                    onClick = {
                                        selectedAlbumIndex = it.id
                                        component.getAlbum(selectedAlbumIndex)
                                    }
                                )
                            }
                        }
                    }
                    BoxAnimatedVisibility(
                        visible = albumsExpanded,
                        modifier = Modifier.weight(1f)
                    ) {
                        val configuration = LocalConfiguration.current
                        val isLandscape =
                            configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

                        val gridCells = if (isLandscape) 9 else 4
                        val maxRows = if (isLandscape) 3 else 5
                        val itemHeight = 144.dp
                        val verticalSpacing = 8.dp

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(gridCells),
                            modifier = Modifier
                                .heightIn(
                                    max = (itemHeight + verticalSpacing) * maxRows - verticalSpacing
                                )
                                .fadingEdges(listState)
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(
                                space = 8.dp,
                                alignment = Alignment.CenterHorizontally
                            ),
                            verticalArrangement = Arrangement.spacedBy(verticalSpacing),
                            contentPadding = PaddingValues(
                                start = WindowInsets.displayCutout
                                    .asPaddingValues()
                                    .calculateStartPadding(layoutDirection) + 8.dp,
                                end = WindowInsets.displayCutout
                                    .asPaddingValues()
                                    .calculateEndPadding(layoutDirection) + 8.dp
                            )
                        ) {
                            items(
                                items = albumsState.albums,
                                key = { "album_${it.id}_expanded" }
                            ) {
                                AlbumChip(
                                    album = it,
                                    selected = selectedAlbumIndex == it.id,
                                    isImageVisible = true,
                                    onClick = {
                                        selectedAlbumIndex = it.id
                                        component.getAlbum(selectedAlbumIndex)
                                    },
                                    modifier = Modifier.height(itemHeight)
                                )
                            }
                        }
                    }
                    EnhancedIconButton(
                        onClick = { albumsExpanded = !albumsExpanded }
                    ) {
                        val rotation by animateFloatAsState(if (albumsExpanded) 180f else 0f)
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "Expand",
                            modifier = Modifier.rotate(rotation)
                        )
                    }
                }
            }
        }
        MediaPickerGridWithOverlays(
            component = component,
            isSearching = isSearching,
            allowedMedia = allowedMedia,
            allowMultiple = allowMultiple,
            onRequestManagePermission = onRequestManagePermission,
            isManagePermissionAllowed = isManagePermissionAllowed,
            selectedAlbumIndex = selectedAlbumIndex,
            onSearchingChange = { isSearching = it }
        )
    }
    BackHandler(selectedAlbumIndex != -1L) {
        selectedAlbumIndex = -1L
        component.getAlbum(selectedAlbumIndex)
    }

    BackHandler(isSearching) {
        isSearching = false
    }
}

@Composable
private fun AlbumChip(
    album: Album,
    selected: Boolean,
    isImageVisible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isImageActuallyVisible = isImageVisible && album.uri.isNotEmpty()
    EnhancedChip(
        modifier = modifier,
        selected = selected,
        selectedColor = MaterialTheme.colorScheme.secondaryContainer,
        unselectedColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        onClick = onClick,
        contentPadding = PaddingValues(
            horizontal = animateDpAsState(
                if (isImageActuallyVisible) 8.dp
                else 12.dp
            ).value,
            vertical = animateDpAsState(
                if (isImageActuallyVisible) 8.dp
                else 0.dp
            ).value
        ),
        label = {
            val title =
                if (album.id == -1L) stringResource(R.string.all) else album.label
            Column(
                modifier = Modifier.animateContentSizeNoClip(
                    alignment = Alignment.Center
                ),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                var width by remember {
                    mutableStateOf(1.dp)
                }
                val density = LocalDensity.current
                Text(
                    text = title,
                    modifier = Modifier.onSizeChanged {
                        width = with(density) {
                            it.width.toDp().coerceAtLeast(100.dp)
                        }
                    }
                )
                BoxAnimatedVisibility(
                    visible = isImageActuallyVisible,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Box {
                        BoxAnimatedVisibility(
                            visible = width > 1.dp,
                            enter = fadeIn() + scaleIn(),
                            exit = fadeOut() + scaleOut()
                        ) {
                            Picture(
                                model = album.uri,
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .height(100.dp)
                                    .width(width),
                                shape = ShapeDefaults.small
                            )
                        }
                        Box(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .height(100.dp)
                                .width(width)
                                .clip(ShapeDefaults.small)
                                .background(
                                    MaterialTheme
                                        .colorScheme
                                        .surfaceContainer
                                        .copy(0.6f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            AutoSizeText(
                                text = album.count.toString(),
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontSize = 20.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        },
        defaultMinSize = 32.dp,
        shape = ShapeDefaults.default
    )
}