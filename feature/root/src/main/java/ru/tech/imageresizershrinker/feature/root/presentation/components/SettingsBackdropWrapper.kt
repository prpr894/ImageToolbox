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

package ru.tech.imageresizershrinker.feature.root.presentation.components

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BackdropScaffold
import androidx.compose.material.BackdropValue
import androidx.compose.material.Surface
import androidx.compose.material.rememberBackdropScaffoldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.gesture.detectPointerTransformGestures
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import ru.tech.imageresizershrinker.core.settings.domain.model.FastSettingsSide
import ru.tech.imageresizershrinker.core.ui.utils.animation.FancyTransitionEasing
import ru.tech.imageresizershrinker.core.ui.utils.navigation.Screen
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedModalSheetDragHandle
import ru.tech.imageresizershrinker.core.ui.widget.modifier.animateShape
import ru.tech.imageresizershrinker.core.ui.widget.modifier.toShape
import ru.tech.imageresizershrinker.core.ui.widget.modifier.withLayoutCorners
import ru.tech.imageresizershrinker.feature.settings.presentation.SettingsContent
import ru.tech.imageresizershrinker.feature.settings.presentation.screenLogic.SettingsComponent
import kotlin.coroutines.cancellation.CancellationException

@Composable
internal fun SettingsBackdropWrapper(
    currentScreen: Screen?,
    concealBackdropFlow: Flow<Boolean>,
    settingsComponent: SettingsComponent,
    children: @Composable () -> Unit
) {
    var shape by remember { mutableStateOf<RoundedCornerShape>(RoundedCornerShape(0.dp)) }
    val scaffoldState = rememberBackdropScaffoldState(
        initialValue = BackdropValue.Concealed,
        animationSpec = tween(
            durationMillis = 400,
            easing = FancyTransitionEasing
        )
    )
    val canExpandSettings = ((currentScreen?.id ?: -1) >= 0)
        .and(settingsComponent.settingsState.fastSettingsSide != FastSettingsSide.None)

    var predictiveBackProgress by remember {
        mutableFloatStateOf(0f)
    }
    val animatedPredictiveBackProgress by animateFloatAsState(predictiveBackProgress)

    val clean = {
        predictiveBackProgress = 0f
    }

    LaunchedEffect(canExpandSettings) {
        if (!canExpandSettings) {
            clean()
            scaffoldState.conceal()
        }
    }

    LaunchedEffect(concealBackdropFlow) {
        concealBackdropFlow
            .debounce(200)
            .collectLatest {
                if (it) {
                    clean()
                    scaffoldState.conceal()
                }
            }
    }

    val scope = rememberCoroutineScope()
    val isTargetRevealed = scaffoldState.targetValue == BackdropValue.Revealed

    BackdropScaffold(
        scaffoldState = scaffoldState,
        modifier = Modifier.withLayoutCorners {
            shape = it.toShape(1f)
            this
        },
        appBar = {},
        frontLayerContent = {

            val alpha by animateFloatAsState(
                if (isTargetRevealed) 1f else 0f
            )
            val color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha / 2f)
            var isWantOpenSettings by remember {
                mutableStateOf(false)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        drawContent()
                        drawRect(color)
                    }
            ) {
                Box(
                    modifier = Modifier.pointerInput(isWantOpenSettings) {
                        detectPointerTransformGestures(
                            consume = false,
                            onGestureEnd = {},
                            onGestureStart = {
                                isWantOpenSettings = false
                            },
                            onGesture = { _, _, _, _, _, _ -> }
                        )
                    },
                    content = {
                        children()

                        if (isTargetRevealed || scaffoldState.isRevealed) {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = Color.Transparent
                            ) {}
                        }
                    }
                )

                SettingsOpenButton(
                    isWantOpenSettings = isWantOpenSettings,
                    onStateChange = { isWantOpenSettings = it },
                    scaffoldState = scaffoldState,
                    canExpandSettings = canExpandSettings
                )

                EnhancedModalSheetDragHandle(
                    color = Color.Transparent,
                    drawStroke = false,
                    modifier = Modifier.alpha(alpha)
                )
            }
        },
        backLayerContent = {
            if (canExpandSettings && (scaffoldState.isRevealed || isTargetRevealed)) {
                if (isTargetRevealed) {
                    PredictiveBackHandler { progress ->
                        try {
                            progress.collect { event ->
                                if (event.progress <= 0.05f) {
                                    clean()
                                }
                                predictiveBackProgress = event.progress * 1.3f
                            }
                            scope.launch {
                                scaffoldState.conceal()
                                clean()
                            }
                        } catch (_: CancellationException) {
                            clean()
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(shape)
                        .alpha(1f - animatedPredictiveBackProgress)
                ) {
                    SettingsContent(
                        component = settingsComponent
                    )
                }
            }
        },
        peekHeight = 0.dp,
        headerHeight = 70.dp,
        persistentAppBar = false,
        frontLayerElevation = 0.dp,
        backLayerBackgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        frontLayerBackgroundColor = MaterialTheme.colorScheme.surface,
        frontLayerScrimColor = Color.Transparent,
        frontLayerShape = animateShape(
            if (scaffoldState.isRevealed) shape
            else RoundedCornerShape(0.dp)
        ),
        gesturesEnabled = scaffoldState.isRevealed
    )
}