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

package com.t8rin.imagetoolbox.core.filters.presentation.widget.addFilters

import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ImageSearch
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.filters.presentation.model.UiCubeLutFilter
import com.t8rin.imagetoolbox.core.filters.presentation.model.UiFilter
import com.t8rin.imagetoolbox.core.filters.presentation.utils.collectAsUiState
import com.t8rin.imagetoolbox.core.filters.presentation.widget.FilterSelectionItem
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.ui.theme.takeColorFromScheme
import com.t8rin.imagetoolbox.core.ui.utils.helper.LocalFilterPreviewModelProvider
import com.t8rin.imagetoolbox.core.ui.utils.provider.rememberLocalEssentials
import com.t8rin.imagetoolbox.core.ui.widget.buttons.ShareButton
import com.t8rin.imagetoolbox.core.ui.widget.controls.selection.ImageSelector
import com.t8rin.imagetoolbox.core.ui.widget.dialogs.OneTimeSaveLocationSelectionDialog
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedIconButton
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.hapticsClickable
import com.t8rin.imagetoolbox.core.ui.widget.image.Picture
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.t8rin.imagetoolbox.core.ui.widget.modifier.container
import com.t8rin.imagetoolbox.core.ui.widget.preferences.PreferenceItemOverload
import com.t8rin.imagetoolbox.core.ui.widget.text.AutoSizeText
import com.t8rin.imagetoolbox.core.ui.widget.utils.rememberForeverLazyListState

@Composable
internal fun OtherContent(
    component: AddFiltersSheetComponent,
    tabs: List<Pair<ImageVector, String>>,
    page: Int,
    filters: List<UiFilter<*>>,
    onVisibleChange: (Boolean) -> Unit,
    onFilterPickedWithParams: (UiFilter<*>) -> Unit,
    onFilterPicked: (UiFilter<*>) -> Unit,
    previewBitmap: Bitmap?,
) {
    val previewModel = LocalFilterPreviewModelProvider.current.preview
    val essentials = rememberLocalEssentials()
    val showConfetti: () -> Unit = essentials::showConfetti
    val favoriteFilters by component.favoritesFlow.collectAsUiState()
    val onRequestFilterMapping = component::filterToTransformation

    LazyColumn(
        state = rememberForeverLazyListState("sheet$page"),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        if (tabs[page].first == Icons.Rounded.Speed) {
            item {
                val previewProvider = LocalFilterPreviewModelProvider.current
                val canSetDynamicFilterPreview = previewProvider.canSetDynamicFilterPreview

                Row(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .height(intrinsicSize = IntrinsicSize.Max)
                ) {
                    ImageSelector(
                        value = previewModel.data,
                        onValueChange = {
                            component.setFilterPreviewModel(it.toString())
                        },
                        title = stringResource(R.string.filter_preview_image),
                        subtitle = stringResource(R.string.filter_preview_image_sub),
                        contentScale = ContentScale.Crop,
                        color = Color.Unspecified,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        shape = ShapeDefaults.start
                    )
                    val containerColor by animateColorAsState(
                        if (canSetDynamicFilterPreview) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        }
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .clip(ShapeDefaults.center)
                            .hapticsClickable {
                                component.setCanSetDynamicFilterPreview(true)
                            }
                            .container(
                                color = containerColor,
                                shape = ShapeDefaults.center,
                                resultPadding = 0.dp
                            )
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ImageSearch,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.contentColorFor(containerColor)
                        )
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(start = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(2) { index ->
                            val shape = if (index == 0) {
                                ShapeDefaults.topEnd
                            } else {
                                ShapeDefaults.bottomEnd
                            }
                            val containerColor = takeColorFromScheme {
                                when {
                                    canSetDynamicFilterPreview -> secondaryContainer
                                    previewModel.data == R.drawable.filter_preview_source && index == 0 -> secondary
                                    previewModel.data == R.drawable.filter_preview_source_3 && index == 1 -> secondary
                                    else -> secondaryContainer
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(shape)
                                    .hapticsClickable {
                                        component.setFilterPreviewModel(
                                            index.toString()
                                        )
                                    }
                                    .container(
                                        color = containerColor,
                                        shape = shape,
                                        resultPadding = 0.dp
                                    )
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                AutoSizeText(
                                    text = (index + 1).toString(),
                                    color = contentColorFor(
                                        containerColor
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
        if (tabs[page].first == Icons.Rounded.TableChart) {
            item {
                PreferenceItemOverload(
                    title = stringResource(R.string.save_empty_lut),
                    subtitle = stringResource(R.string.save_empty_lut_sub),
                    shape = ShapeDefaults.default,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    endIcon = {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Picture(
                                model = R.drawable.lookup,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(48.dp)
                                    .scale(1.1f)
                                    .clip(MaterialTheme.shapes.extraSmall),
                                shape = MaterialTheme.shapes.extraSmall
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            var showFolderSelection by rememberSaveable {
                                mutableStateOf(false)
                            }
                            val saveNeutralLut: (String?) -> Unit = {
                                component.saveNeutralLut(
                                    oneTimeSaveLocationUri = it,
                                    onComplete = essentials::parseSaveResult
                                )
                            }
                            Row {
                                ShareButton(
                                    onShare = {
                                        component.shareNeutralLut(showConfetti)
                                    },
                                    onCopy = {
                                        component.cacheNeutralLut(essentials::copyToClipboard)
                                    }
                                )
                                EnhancedIconButton(
                                    onClick = {
                                        saveNeutralLut(null)
                                    },
                                    onLongClick = {
                                        showFolderSelection = true
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Save,
                                        contentDescription = stringResource(R.string.save)
                                    )
                                }

                                OneTimeSaveLocationSelectionDialog(
                                    visible = showFolderSelection,
                                    onDismiss = {
                                        showFolderSelection = false
                                    },
                                    onSaveRequest = saveNeutralLut
                                )
                            }
                        }
                    }
                )
            }
        }
        itemsIndexed(
            items = filters,
            key = { _, f -> f.hashCode() }
        ) { index, filter ->
            FilterSelectionItem(
                filter = filter,
                canOpenPreview = previewBitmap != null,
                favoriteFilters = favoriteFilters,
                onLongClick = {
                    component.setPreviewData(filter)
                },
                onOpenPreview = {
                    component.setPreviewData(filter)
                },
                onClick = { custom ->
                    onVisibleChange(false)
                    if (custom != null) {
                        onFilterPickedWithParams(custom)
                    } else {
                        onFilterPicked(filter)
                    }
                },
                onRequestFilterMapping = onRequestFilterMapping,
                shape = ShapeDefaults.byIndex(
                    index = index,
                    size = filters.size
                ),
                onToggleFavorite = {
                    component.toggleFavorite(filter)
                },
                isFavoritePage = false,
                modifier = Modifier.animateItem(),
                cubeLutRemoteResources = if (filter is UiCubeLutFilter) {
                    component.cubeLutRemoteResources
                } else null,
                cubeLutDownloadProgress = if (filter is UiCubeLutFilter) {
                    component.cubeLutDownloadProgress
                } else null,
                onCubeLutDownloadRequest = { forceUpdate, downloadOnlyNewData ->
                    component.updateCubeLuts(
                        startDownloadIfNeeded = true,
                        forceUpdate = forceUpdate,
                        onFailure = essentials::showFailureToast,
                        downloadOnlyNewData = downloadOnlyNewData
                    )
                }
            )
        }
    }
}