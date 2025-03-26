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

package ru.tech.imageresizershrinker.feature.main.presentation.components

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import kotlinx.coroutines.delay
import ru.tech.imageresizershrinker.core.settings.presentation.provider.LocalSettingsState
import ru.tech.imageresizershrinker.core.ui.utils.helper.ProvidesValue
import ru.tech.imageresizershrinker.core.ui.utils.provider.LocalScreenSize
import ru.tech.imageresizershrinker.core.ui.widget.modifier.autoElevatedBorder

@Composable
internal fun MainDrawerContent(
    sideSheetState: DrawerState,
    isSheetSlideable: Boolean,
    sheetExpanded: Boolean,
    layoutDirection: LayoutDirection,
    settingsBlockContent: @Composable () -> Unit
) {
    val settingsState = LocalSettingsState.current
    val settingsBlock = remember {
        movableContentOf {
            settingsBlockContent()
        }
    }

    val screenSize = LocalScreenSize.current
    val widthState by remember(sheetExpanded, screenSize) {
        derivedStateOf {
            if (isSheetSlideable) {
                min(
                    screenSize.width * 0.85f,
                    DrawerDefaults.MaximumDrawerWidth
                )
            } else {
                if (sheetExpanded) screenSize.width * 0.55f
                else min(
                    screenSize.width * 0.4f,
                    DrawerDefaults.MaximumDrawerWidth
                )
            }.coerceAtLeast(1.dp)
        }
    }

    var predictiveBackProgress by remember {
        mutableFloatStateOf(0f)
    }
    val animatedPredictiveBackProgress by animateFloatAsState(predictiveBackProgress)

    val clean = {
        predictiveBackProgress = 0f
    }
    val shape = if (isSheetSlideable) {
        RoundedCornerShape(
            topStart = 0.dp,
            bottomStart = 0.dp,
            bottomEnd = 24.dp,
            topEnd = 24.dp
        )
    } else RectangleShape

    LaunchedEffect(sideSheetState.isOpen, isSheetSlideable) {
        if (!sideSheetState.isOpen || isSheetSlideable) {
            delay(300L)
            clean()
        }
    }

    if ((sideSheetState.isOpen || sideSheetState.isAnimationRunning) && isSheetSlideable) {
        PredictiveBackHandler { progress ->
            try {
                progress.collect { event ->
                    if (event.progress <= 0.05f) {
                        clean()
                    }
                    predictiveBackProgress = event.progress
                }
                sideSheetState.close()
            } catch (_: Throwable) {
                clean()
            }
        }
    }
    val autoElevation by animateDpAsState(
        if (settingsState.drawContainerShadows) 16.dp
        else 0.dp
    )
    ModalDrawerSheet(
        modifier = Modifier
            .width(animateDpAsState(targetValue = widthState).value)
            .then(
                if (isSheetSlideable) {
                    Modifier
                        .graphicsLayer {
                            val sheetOffset = 0f
                            val sheetHeight = size.height
                            if (!sheetOffset.isNaN() && !sheetHeight.isNaN() && sheetHeight != 0f) {
                                val progress = animatedPredictiveBackProgress
                                scaleX = calculatePredictiveBackScaleX(progress)
                                scaleY = calculatePredictiveBackScaleY(progress)
                                transformOrigin =
                                    TransformOrigin((sheetOffset + sheetHeight) / sheetHeight, 0.5f)
                            }
                        }
                        .offset(-((settingsState.borderWidth + 1.dp)))
                        .autoElevatedBorder(
                            shape = shape,
                            autoElevation = autoElevation
                        )
                        .autoElevatedBorder(
                            height = 0.dp,
                            shape = shape,
                            autoElevation = autoElevation
                        )
                        .clip(shape)
                } else Modifier
            ),
        drawerContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        drawerShape = shape,
        windowInsets = WindowInsets(0)
    ) {
        LocalLayoutDirection.ProvidesValue(layoutDirection) {
            settingsBlock()
        }
    }
}

fun GraphicsLayerScope.calculatePredictiveBackScaleX(progress: Float): Float {
    val width = size.width
    return if (width.isNaN() || width == 0f) {
        1f
    } else {
        (1f - progress).coerceAtLeast(0.8f)
    }
}

fun GraphicsLayerScope.calculatePredictiveBackScaleY(progress: Float): Float {
    val height = size.height
    return if (height.isNaN() || height == 0f) {
        1f
    } else {
        (1f - progress).coerceAtLeast(0.8f)
    }
}