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

package ru.tech.imageresizershrinker.feature.filters.presentation.components.addEditMaskSheet

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Preview
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t8rin.histogram.ImageHistogram
import kotlinx.coroutines.launch
import ru.tech.imageresizershrinker.core.domain.model.Pt
import ru.tech.imageresizershrinker.core.domain.model.pt
import ru.tech.imageresizershrinker.core.filters.presentation.widget.AddFilterButton
import ru.tech.imageresizershrinker.core.filters.presentation.widget.FilterItem
import ru.tech.imageresizershrinker.core.filters.presentation.widget.FilterReorderSheet
import ru.tech.imageresizershrinker.core.filters.presentation.widget.addFilters.AddFiltersSheet
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.ui.theme.outlineVariant
import ru.tech.imageresizershrinker.core.ui.utils.helper.isPortraitOrientationAsState
import ru.tech.imageresizershrinker.core.ui.utils.provider.LocalComponentActivity
import ru.tech.imageresizershrinker.core.ui.widget.buttons.EraseModeButton
import ru.tech.imageresizershrinker.core.ui.widget.buttons.PanModeButton
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedIconButton
import ru.tech.imageresizershrinker.core.ui.widget.image.ImageHeaderState
import ru.tech.imageresizershrinker.core.ui.widget.modifier.container
import ru.tech.imageresizershrinker.core.ui.widget.other.BoxAnimatedVisibility
import ru.tech.imageresizershrinker.core.ui.widget.other.LoadingIndicator
import ru.tech.imageresizershrinker.core.ui.widget.other.LocalToastHostState
import ru.tech.imageresizershrinker.core.ui.widget.other.showFailureToast
import ru.tech.imageresizershrinker.core.ui.widget.preferences.PreferenceItemOverload
import ru.tech.imageresizershrinker.core.ui.widget.preferences.PreferenceRowSwitch
import ru.tech.imageresizershrinker.core.ui.widget.text.TitleItem
import ru.tech.imageresizershrinker.feature.draw.domain.DrawPathMode
import ru.tech.imageresizershrinker.feature.draw.presentation.components.BrushSoftnessSelector
import ru.tech.imageresizershrinker.feature.draw.presentation.components.DrawColorSelector
import ru.tech.imageresizershrinker.feature.draw.presentation.components.DrawPathModeSelector
import ru.tech.imageresizershrinker.feature.draw.presentation.components.LineWidthSelector
import ru.tech.imageresizershrinker.feature.draw.presentation.components.model.UiDrawPathMode
import ru.tech.imageresizershrinker.feature.draw.presentation.components.model.toUi


@Composable
internal fun LazyItemScope.AddEditMaskSheetControls(
    component: AddMaskSheetComponent,
    imageState: ImageHeaderState,
    domainDrawPathMode: DrawPathMode,
    onDrawPathModeChange: (UiDrawPathMode) -> Unit,
    strokeWidth: Pt,
    onStrokeWidthChange: (Pt) -> Unit,
    brushSoftness: Pt,
    onBrushSoftnessChange: (Pt) -> Unit,
    panEnabled: Boolean,
    onTogglePanEnabled: () -> Unit,
    isEraserOn: Boolean,
    onToggleIsEraserOn: () -> Unit
) {
    var showAddFilterSheet by rememberSaveable { mutableStateOf(false) }

    val context = LocalComponentActivity.current
    val toastHostState = LocalToastHostState.current
    val scope = rememberCoroutineScope()

    var showReorderSheet by rememberSaveable { mutableStateOf(false) }

    val isPortrait by isPortraitOrientationAsState()
    val canSave = component.paths.isNotEmpty() && component.filterList.isNotEmpty()

    Row(
        Modifier
            .then(
                if (imageState.isBlocked && isPortrait) {
                    Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    )
                } else Modifier.padding(16.dp)
            )
            .container(shape = CircleShape)
    ) {
        PanModeButton(
            selected = panEnabled,
            onClick = onTogglePanEnabled
        )
        Spacer(Modifier.width(4.dp))
        EnhancedIconButton(
            containerColor = Color.Transparent,
            borderColor = MaterialTheme.colorScheme.outlineVariant(
                luminance = 0.1f
            ),
            onClick = component::undo,
            enabled = (component.lastPaths.isNotEmpty() || component.paths.isNotEmpty())
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
            onClick = component::redo,
            enabled = component.undonePaths.isNotEmpty()
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Redo,
                contentDescription = "Redo"
            )
        }
        EraseModeButton(
            selected = isEraserOn,
            enabled = !panEnabled,
            onClick = onToggleIsEraserOn
        )
    }

    AnimatedVisibility(visible = canSave) {
        Column {
            BoxAnimatedVisibility(component.maskPreviewModeEnabled) {
                PreferenceItemOverload(
                    title = stringResource(R.string.histogram),
                    subtitle = stringResource(R.string.histogram_sub),
                    endIcon = {
                        AnimatedContent(component.previewBitmap != null) {
                            if (it) {
                                ImageHistogram(
                                    image = component.previewBitmap,
                                    modifier = Modifier
                                        .width(100.dp)
                                        .height(65.dp)
                                        .background(MaterialTheme.colorScheme.background),
                                    bordersColor = Color.White
                                )
                            } else {
                                Box(modifier = Modifier.size(56.dp)) {
                                    LoadingIndicator()
                                }
                            }
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp)
                )
            }
            PreferenceRowSwitch(
                title = stringResource(id = R.string.mask_preview),
                subtitle = stringResource(id = R.string.mask_preview_sub),
                color = animateColorAsState(
                    if (component.maskPreviewModeEnabled) MaterialTheme.colorScheme.onPrimary
                    else Color.Unspecified,
                ).value,
                modifier = Modifier.padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                contentColor = animateColorAsState(
                    if (component.maskPreviewModeEnabled) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
                ).value,
                onClick = component::togglePreviewMode,
                checked = component.maskPreviewModeEnabled,
                startIcon = Icons.Rounded.Preview
            )
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DrawColorSelector(
            color = Color.Unspecified,
            titleText = stringResource(id = R.string.mask_color),
            defaultColors = remember {
                listOf(
                    Color.Red,
                    Color.Green,
                    Color.Blue,
                    Color.Yellow,
                    Color.Cyan,
                    Color.Magenta
                )
            },
            value = component.maskColor,
            onValueChange = component::updateMaskColor,
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp
            )
        )
        DrawPathModeSelector(
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp
            ),
            values = remember {
                listOf(
                    DrawPathMode.Free,
                    DrawPathMode.Lasso,
                    DrawPathMode.Rect(),
                    DrawPathMode.Oval,
                    DrawPathMode.Triangle,
                    DrawPathMode.Polygon(),
                    DrawPathMode.Star()
                )
            },
            value = domainDrawPathMode,
            onValueChange = { onDrawPathModeChange(it.toUi()) }
        )
        LineWidthSelector(
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp
            ),
            color = Color.Unspecified,
            value = strokeWidth.value,
            onValueChange = { onStrokeWidthChange(it.pt) }
        )
        BrushSoftnessSelector(
            modifier = Modifier
                .padding(top = 8.dp, end = 16.dp, start = 16.dp),
            color = Color.Unspecified,
            value = brushSoftness.value,
            onValueChange = { onBrushSoftnessChange(it.pt) }
        )
    }

    PreferenceRowSwitch(
        title = stringResource(id = R.string.inverse_fill_type),
        subtitle = stringResource(id = R.string.inverse_fill_type_sub),
        checked = component.isInverseFillType,
        modifier = Modifier.padding(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp
        ),
        color = Color.Unspecified,
        resultModifier = Modifier.padding(16.dp),
        applyHorizontalPadding = false,
        shape = RoundedCornerShape(24.dp),
        onClick = {
            component.toggleIsInverseFillType()
        }
    )
    AnimatedContent(
        targetState = component.filterList.isNotEmpty(),
        transitionSpec = {
            fadeIn() + expandVertically() togetherWith fadeOut() + shrinkVertically()
        }
    ) { notEmpty ->
        if (notEmpty) {
            Column(
                Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .container(MaterialTheme.shapes.extraLarge)
            ) {
                TitleItem(text = stringResource(R.string.filters))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    component.filterList.forEachIndexed { index, filter ->
                        FilterItem(
                            backgroundColor = MaterialTheme.colorScheme.surface,
                            filter = filter,
                            onFilterChange = {
                                component.updateFilter(
                                    value = it,
                                    index = index,
                                    showError = {
                                        scope.launch {
                                            toastHostState.showFailureToast(
                                                context = context,
                                                throwable = it
                                            )
                                        }
                                    }
                                )
                            },
                            onLongPress = {
                                showReorderSheet = true
                            },
                            showDragHandle = false,
                            onRemove = {
                                component.removeFilterAtIndex(
                                    index
                                )
                            }
                        )
                    }
                    AddFilterButton(
                        onClick = {
                            showAddFilterSheet = true
                        },
                        modifier = Modifier.padding(
                            horizontal = 16.dp
                        )
                    )
                }
            }
        } else {
            AddFilterButton(
                onClick = {
                    showAddFilterSheet = true
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }

    AddFiltersSheet(
        visible = showAddFilterSheet,
        onVisibleChange = { showAddFilterSheet = it },
        previewBitmap = null,
        onFilterPicked = { component.addFilter(it.newInstance()) },
        onFilterPickedWithParams = { component.addFilter(it) },
        component = component.addFiltersSheetComponent,
        filterTemplateCreationSheetComponent = component.filterTemplateCreationSheetComponent
    )
    FilterReorderSheet(
        filterList = component.filterList,
        visible = showReorderSheet,
        onDismiss = {
            showReorderSheet = false
        },
        onReorder = component::updateFiltersOrder
    )
}