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

package ru.tech.imageresizershrinker.core.filters.presentation.widget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.RemoveCircleOutline
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import ru.tech.imageresizershrinker.core.filters.presentation.model.UiFilter
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.ui.theme.outlineVariant
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedButton
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedDropdownMenu
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedIconButton
import ru.tech.imageresizershrinker.core.ui.widget.modifier.container
import ru.tech.imageresizershrinker.core.ui.widget.modifier.fadingEdges
import ru.tech.imageresizershrinker.core.ui.widget.value.ValueDialog
import ru.tech.imageresizershrinker.core.ui.widget.value.ValueText

@Composable
fun <T> FilterItem(
    filter: UiFilter<T>,
    showDragHandle: Boolean,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    onLongPress: (() -> Unit)? = null,
    previewOnly: Boolean = false,
    onFilterChange: (value: Any) -> Unit,
    backgroundColor: Color = Color.Unspecified,
    shape: Shape = MaterialTheme.shapes.extraLarge,
    canHide: Boolean = true
) {
    var isControlsExpanded by rememberSaveable {
        mutableStateOf(true)
    }

    val isVisible = filter.isVisible

    if (!canHide && !isVisible) {
        SideEffect {
            filter.isVisible = true
        }
    }

    Box(
        modifier = Modifier.then(
            onLongPress?.let {
                Modifier.pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { it() }
                    )
                }
            } ?: Modifier
        )
    ) {
        Row(
            modifier = modifier
                .container(color = backgroundColor, shape = shape)
                .animateContentSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .alpha(if (previewOnly) 0.5f else 1f)
                    .then(
                        if (previewOnly) {
                            Modifier
                                .heightIn(max = 120.dp)
                                .fadingEdges(
                                    scrollableState = null,
                                    isVertical = true,
                                    length = 12.dp
                                )
                                .verticalScroll(rememberScrollState())
                        } else Modifier
                    )
            ) {
                var sliderValue by remember(filter) {
                    mutableFloatStateOf(
                        ((filter.value as? Number)?.toFloat()) ?: 0f
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!previewOnly) {
                        if (canHide) {
                            Box {
                                var showPopup by remember {
                                    mutableStateOf(false)
                                }

                                EnhancedIconButton(
                                    onClick = {
                                        showPopup = true
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.MoreVert,
                                        contentDescription = "more"
                                    )
                                }

                                EnhancedDropdownMenu(
                                    expanded = showPopup,
                                    onDismissRequest = { showPopup = false },
                                    shape = MaterialTheme.shapes.extraLarge
                                ) {
                                    EnhancedButton(
                                        modifier = Modifier
                                            .padding(horizontal = 8.dp)
                                            .width(164.dp),
                                        onClick = {
                                            onRemove()
                                            showPopup = false
                                        },
                                        containerColor = MaterialTheme.colorScheme.tertiary
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.RemoveCircleOutline,
                                            contentDescription = stringResource(R.string.remove)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(stringResource(R.string.remove))
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    EnhancedButton(
                                        modifier = Modifier
                                            .padding(horizontal = 8.dp)
                                            .width(164.dp),
                                        onClick = {
                                            filter.isVisible = !isVisible
                                            onFilterChange(filter.value as Any)
                                            showPopup = false
                                        },
                                        containerColor = MaterialTheme.colorScheme.primary
                                    ) {
                                        Icon(
                                            imageVector = if (isVisible) {
                                                Icons.Outlined.VisibilityOff
                                            } else {
                                                Icons.Rounded.Visibility
                                            },
                                            contentDescription = stringResource(R.string.remove)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            stringResource(
                                                if (isVisible) R.string.hide
                                                else R.string.show
                                            )
                                        )
                                    }
                                }
                            }
                        } else {
                            EnhancedIconButton(
                                onClick = onRemove
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.RemoveCircleOutline,
                                    contentDescription = stringResource(R.string.remove)
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(filter.title),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .alpha(
                                    animateFloatAsState(if (isVisible) 1f else 0.5f).value
                                )
                                .padding(
                                    top = 8.dp,
                                    end = 8.dp,
                                    start = 16.dp,
                                    bottom = 8.dp
                                )
                                .fillMaxWidth(),
                            textDecoration = if (!isVisible) {
                                TextDecoration.LineThrough
                            } else null
                        )
                    }
                    if (!filter.value.isSingle() && !previewOnly) {
                        EnhancedIconButton(
                            onClick = {
                                isControlsExpanded = !isControlsExpanded
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.KeyboardArrowDown,
                                contentDescription = "Expand",
                                modifier = Modifier.rotate(
                                    animateFloatAsState(
                                        if (isControlsExpanded) 180f
                                        else 0f
                                    ).value
                                )
                            )
                        }
                    }
                    if (filter.value is Number) {
                        var showValueDialog by remember { mutableStateOf(false) }
                        ValueText(
                            value = sliderValue,
                            onClick = { showValueDialog = true }
                        )
                        ValueDialog(
                            roundTo = filter.paramsInfo[0].roundTo,
                            valueRange = filter.paramsInfo[0].valueRange,
                            valueState = sliderValue.toString(),
                            expanded = showValueDialog && !previewOnly,
                            onDismiss = { showValueDialog = false },
                            onValueUpdate = {
                                sliderValue = it
                                onFilterChange(it)
                            }
                        )
                    }
                }
                AnimatedVisibility(
                    visible = isControlsExpanded || filter.value.isSingle() || previewOnly
                ) {
                    FilterItemContent(
                        filter = filter,
                        onFilterChange = onFilterChange,
                        previewOnly = previewOnly
                    )
                }
            }
            if (showDragHandle) {
                Box(
                    modifier = Modifier
                        .height(if (filter.value is Unit) 32.dp else 64.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant())
                        .padding(start = 20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Rounded.DragHandle,
                    contentDescription = stringResource(R.string.drag_handle_width),
                    modifier = Modifier
                        .size(48.dp)
                        .padding(12.dp)
                )
                Spacer(Modifier.width(8.dp))
            }
        }
        if (previewOnly) {
            Surface(
                color = Color.Transparent,
                modifier = modifier.matchParentSize()
            ) {}
        }
    }
}

private fun Any?.isSingle(): Boolean = this is Number || this is Unit