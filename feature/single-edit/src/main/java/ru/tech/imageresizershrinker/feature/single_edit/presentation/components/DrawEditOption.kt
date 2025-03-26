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

package ru.tech.imageresizershrinker.feature.single_edit.presentation.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.outlined.ZoomIn
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.tech.imageresizershrinker.core.domain.model.coerceIn
import ru.tech.imageresizershrinker.core.domain.model.pt
import ru.tech.imageresizershrinker.core.domain.utils.notNullAnd
import ru.tech.imageresizershrinker.core.filters.domain.model.Filter
import ru.tech.imageresizershrinker.core.filters.presentation.widget.FilterTemplateCreationSheetComponent
import ru.tech.imageresizershrinker.core.filters.presentation.widget.addFilters.AddFiltersSheetComponent
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.settings.presentation.provider.LocalSettingsState
import ru.tech.imageresizershrinker.core.settings.presentation.provider.LocalSimpleSettingsInteractor
import ru.tech.imageresizershrinker.core.ui.theme.outlineVariant
import ru.tech.imageresizershrinker.core.ui.widget.buttons.EraseModeButton
import ru.tech.imageresizershrinker.core.ui.widget.buttons.PanModeButton
import ru.tech.imageresizershrinker.core.ui.widget.controls.selection.AlphaSelector
import ru.tech.imageresizershrinker.core.ui.widget.controls.selection.HelperGridParamsSelector
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedIconButton
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedTopAppBar
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedTopAppBarType
import ru.tech.imageresizershrinker.core.ui.widget.modifier.HelperGridParams
import ru.tech.imageresizershrinker.core.ui.widget.modifier.container
import ru.tech.imageresizershrinker.core.ui.widget.other.DrawLockScreenOrientation
import ru.tech.imageresizershrinker.core.ui.widget.preferences.PreferenceRowSwitch
import ru.tech.imageresizershrinker.core.ui.widget.saver.ColorSaver
import ru.tech.imageresizershrinker.core.ui.widget.saver.PtSaver
import ru.tech.imageresizershrinker.core.ui.widget.text.marquee
import ru.tech.imageresizershrinker.feature.draw.domain.DrawLineStyle
import ru.tech.imageresizershrinker.feature.draw.domain.DrawMode
import ru.tech.imageresizershrinker.feature.draw.domain.DrawPathMode
import ru.tech.imageresizershrinker.feature.draw.presentation.components.BitmapDrawer
import ru.tech.imageresizershrinker.feature.draw.presentation.components.BrushSoftnessSelector
import ru.tech.imageresizershrinker.feature.draw.presentation.components.DrawColorSelector
import ru.tech.imageresizershrinker.feature.draw.presentation.components.DrawLineStyleSelector
import ru.tech.imageresizershrinker.feature.draw.presentation.components.DrawModeSelector
import ru.tech.imageresizershrinker.feature.draw.presentation.components.DrawPathModeSelector
import ru.tech.imageresizershrinker.feature.draw.presentation.components.LineWidthSelector
import ru.tech.imageresizershrinker.feature.draw.presentation.components.OpenColorPickerCard
import ru.tech.imageresizershrinker.feature.draw.presentation.components.UiPathPaint
import ru.tech.imageresizershrinker.feature.pick_color.presentation.components.PickColorFromImageSheet

@Composable
fun DrawEditOption(
    visible: Boolean,
    onRequestFiltering: suspend (Bitmap, List<Filter<*>>) -> Bitmap?,
    drawMode: DrawMode,
    onUpdateDrawMode: (DrawMode) -> Unit,
    drawPathMode: DrawPathMode,
    onUpdateDrawPathMode: (DrawPathMode) -> Unit,
    drawLineStyle: DrawLineStyle,
    onUpdateDrawLineStyle: (DrawLineStyle) -> Unit,
    onDismiss: () -> Unit,
    useScaffold: Boolean,
    bitmap: Bitmap?,
    onGetBitmap: (Bitmap) -> Unit,
    undo: () -> Unit,
    redo: () -> Unit,
    paths: List<UiPathPaint>,
    lastPaths: List<UiPathPaint>,
    undonePaths: List<UiPathPaint>,
    addPath: (UiPathPaint) -> Unit,
    helperGridParams: HelperGridParams,
    onUpdateHelperGridParams: (HelperGridParams) -> Unit,
    addFiltersSheetComponent: AddFiltersSheetComponent,
    filterTemplateCreationSheetComponent: FilterTemplateCreationSheetComponent
) {
    bitmap?.let {
        var panEnabled by rememberSaveable { mutableStateOf(false) }

        val switch = @Composable {
            PanModeButton(
                selected = panEnabled,
                onClick = {
                    panEnabled = !panEnabled
                }
            )
        }

        var showPickColorSheet by rememberSaveable { mutableStateOf(false) }

        var isEraserOn by rememberSaveable { mutableStateOf(false) }

        val settingsState = LocalSettingsState.current
        var strokeWidth by rememberSaveable(stateSaver = PtSaver) { mutableStateOf(settingsState.defaultDrawLineWidth.pt) }
        var drawColor by rememberSaveable(stateSaver = ColorSaver) { mutableStateOf(settingsState.defaultDrawColor) }

        var alpha by rememberSaveable(drawMode) {
            mutableFloatStateOf(if (drawMode is DrawMode.Highlighter) 0.4f else 1f)
        }
        var brushSoftness by rememberSaveable(drawMode, stateSaver = PtSaver) {
            mutableStateOf(if (drawMode is DrawMode.Neon) 35.pt else 0.pt)
        }

        LaunchedEffect(drawMode, strokeWidth) {
            strokeWidth = if (drawMode is DrawMode.Image) {
                strokeWidth.coerceIn(10.pt, 120.pt)
            } else {
                strokeWidth.coerceIn(1.pt, 100.pt)
            }
        }

        val secondaryControls = @Composable {
            Row(
                modifier = Modifier.then(
                    if (!useScaffold) {
                        Modifier
                            .padding(16.dp)
                            .container(shape = CircleShape)
                    } else Modifier
                )
            ) {
                switch()
                Spacer(Modifier.width(8.dp))
                EnhancedIconButton(
                    containerColor = Color.Transparent,
                    borderColor = MaterialTheme.colorScheme.outlineVariant(
                        luminance = 0.1f
                    ),
                    onClick = undo,
                    enabled = lastPaths.isNotEmpty() || paths.isNotEmpty()
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
                    onClick = redo,
                    enabled = undonePaths.isNotEmpty()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Redo,
                        contentDescription = "Redo"
                    )
                }
                EraseModeButton(
                    selected = isEraserOn,
                    enabled = !panEnabled,
                    onClick = {
                        isEraserOn = !isEraserOn
                    }
                )
            }
        }

        var stateBitmap by remember(bitmap, visible) { mutableStateOf(bitmap) }
        FullscreenEditOption(
            canGoBack = paths.isEmpty(),
            visible = visible,
            onDismiss = onDismiss,
            useScaffold = useScaffold,
            controls = { scaffoldState ->
                Column(
                    modifier = Modifier.padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val focus = LocalFocusManager.current
                    LaunchedEffect(scaffoldState?.bottomSheetState?.currentValue, focus) {
                        val current = scaffoldState?.bottomSheetState?.currentValue
                        if (current.notNullAnd { it != SheetValue.Expanded }) {
                            focus.clearFocus()
                        }
                    }

                    if (!useScaffold) secondaryControls()
                    AnimatedVisibility(
                        visible = drawMode !is DrawMode.SpotHeal,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        OpenColorPickerCard(
                            onOpen = {
                                showPickColorSheet = true
                            }
                        )
                    }
                    AnimatedVisibility(
                        visible = drawMode !is DrawMode.PathEffect && drawMode !is DrawMode.Image && drawMode !is DrawMode.SpotHeal,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        DrawColorSelector(
                            value = drawColor,
                            onValueChange = { drawColor = it },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    AnimatedVisibility(
                        visible = drawPathMode.isStroke,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        LineWidthSelector(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            title = if (drawMode is DrawMode.Text) {
                                stringResource(R.string.font_size)
                            } else stringResource(R.string.line_width),
                            valueRange = if (drawMode is DrawMode.Image) {
                                10f..120f
                            } else 1f..100f,
                            value = strokeWidth.value,
                            onValueChange = { strokeWidth = it.pt }
                        )
                    }
                    AnimatedVisibility(
                        visible = drawMode !is DrawMode.Highlighter && drawMode !is DrawMode.PathEffect && drawMode !is DrawMode.SpotHeal,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        BrushSoftnessSelector(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            value = brushSoftness.value,
                            onValueChange = { brushSoftness = it.pt }
                        )
                    }
                    AnimatedVisibility(
                        visible = drawMode !is DrawMode.Neon && drawMode !is DrawMode.PathEffect && drawMode !is DrawMode.SpotHeal,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        AlphaSelector(
                            value = alpha,
                            onValueChange = { alpha = it },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    DrawModeSelector(
                        addFiltersSheetComponent = addFiltersSheetComponent,
                        filterTemplateCreationSheetComponent = filterTemplateCreationSheetComponent,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        value = drawMode,
                        strokeWidth = strokeWidth,
                        onValueChange = onUpdateDrawMode,
                        values = remember(drawLineStyle) {
                            derivedStateOf {
                                if (drawLineStyle == DrawLineStyle.None) {
                                    DrawMode.entries
                                } else {
                                    listOf(
                                        DrawMode.Pen,
                                        DrawMode.Highlighter,
                                        DrawMode.Neon
                                    )
                                }
                            }
                        }.value
                    )
                    DrawPathModeSelector(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        value = drawPathMode,
                        onValueChange = onUpdateDrawPathMode,
                        values = remember(drawMode, drawLineStyle) {
                            derivedStateOf {
                                val outlinedModes = listOf(
                                    DrawPathMode.OutlinedRect(),
                                    DrawPathMode.OutlinedOval,
                                    DrawPathMode.OutlinedTriangle,
                                    DrawPathMode.OutlinedPolygon(),
                                    DrawPathMode.OutlinedStar()
                                )
                                if (drawMode !is DrawMode.Text && drawMode !is DrawMode.Image) {
                                    when (drawLineStyle) {
                                        DrawLineStyle.None -> DrawPathMode.entries

                                        !is DrawLineStyle.Stamped<*> -> listOf(
                                            DrawPathMode.Free,
                                            DrawPathMode.Line,
                                            DrawPathMode.LinePointingArrow(),
                                            DrawPathMode.PointingArrow(),
                                            DrawPathMode.DoublePointingArrow(),
                                            DrawPathMode.DoubleLinePointingArrow(),
                                        ) + outlinedModes

                                        else -> listOf(
                                            DrawPathMode.Free,
                                            DrawPathMode.Line
                                        ) + outlinedModes
                                    }
                                } else {
                                    listOf(
                                        DrawPathMode.Free,
                                        DrawPathMode.Line
                                    ) + outlinedModes
                                }
                            }
                        }.value
                    )
                    DrawLineStyleSelector(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        value = drawLineStyle,
                        onValueChange = onUpdateDrawLineStyle
                    )
                    HelperGridParamsSelector(
                        value = helperGridParams,
                        onValueChange = onUpdateHelperGridParams,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    val settingsInteractor = LocalSimpleSettingsInteractor.current
                    val scope = rememberCoroutineScope()
                    PreferenceRowSwitch(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
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
                }
            },
            fabButtons = null,
            actions = {
                if (useScaffold) {
                    secondaryControls()
                    Spacer(Modifier.weight(1f))
                }
            },
            topAppBar = { closeButton ->
                EnhancedTopAppBar(
                    type = EnhancedTopAppBarType.Center,
                    navigationIcon = closeButton,
                    actions = {
                        AnimatedVisibility(
                            visible = paths.isNotEmpty(),
                            enter = fadeIn() + scaleIn(),
                            exit = fadeOut() + scaleOut()
                        ) {
                            EnhancedIconButton(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                onClick = {
                                    onGetBitmap(stateBitmap)
                                    onDismiss()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Done,
                                    contentDescription = "Done"
                                )
                            }
                        }
                    },
                    title = {
                        Text(
                            text = stringResource(R.string.draw),
                            modifier = Modifier.marquee()
                        )
                    }
                )
            }
        ) {
            val direction = LocalLayoutDirection.current
            Box(contentAlignment = Alignment.Center) {
                remember(bitmap) {
                    derivedStateOf {
                        bitmap.copy(Bitmap.Config.ARGB_8888, true).asImageBitmap()
                    }
                }.value.let { imageBitmap ->
                    val aspectRatio = imageBitmap.width / imageBitmap.height.toFloat()
                    BitmapDrawer(
                        imageBitmap = imageBitmap,
                        paths = paths,
                        onRequestFiltering = onRequestFiltering,
                        strokeWidth = strokeWidth,
                        brushSoftness = brushSoftness,
                        drawColor = drawColor.copy(alpha),
                        onAddPath = addPath,
                        isEraserOn = isEraserOn,
                        drawMode = drawMode,
                        modifier = Modifier
                            .padding(
                                start = WindowInsets
                                    .displayCutout
                                    .asPaddingValues()
                                    .calculateStartPadding(direction)
                            )
                            .padding(16.dp)
                            .aspectRatio(aspectRatio, !useScaffold)
                            .fillMaxSize(),
                        panEnabled = panEnabled,
                        onDraw = {
                            stateBitmap = it
                        },
                        drawPathMode = drawPathMode,
                        backgroundColor = Color.Transparent,
                        drawLineStyle = drawLineStyle,
                        helperGridParams = helperGridParams
                    )
                }
            }
        }
        var color by rememberSaveable(stateSaver = ColorSaver) {
            mutableStateOf(Color.Black)
        }
        PickColorFromImageSheet(
            visible = showPickColorSheet,
            onDismiss = {
                showPickColorSheet = false
            },
            bitmap = stateBitmap,
            onColorChange = { color = it },
            color = color
        )

        if (visible) {
            DrawLockScreenOrientation()
        }
    }
}