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

package com.t8rin.imagetoolbox.core.ui.widget.enhanced

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.ui.theme.outlineVariant
import com.t8rin.imagetoolbox.core.ui.utils.helper.ProvidesValue
import com.t8rin.imagetoolbox.core.ui.widget.modifier.fadingEdges
import com.t8rin.imagetoolbox.core.ui.widget.text.AutoSizeText

@Composable
fun EnhancedButtonGroup(
    modifier: Modifier = defaultModifier,
    enabled: Boolean = true,
    items: List<String>,
    selectedIndex: Int,
    title: String? = null,
    onIndexChange: (Int) -> Unit,
    inactiveButtonColor: Color = MaterialTheme.colorScheme.surface
) {
    EnhancedButtonGroup(
        enabled = enabled,
        items = items,
        selectedIndex = selectedIndex,
        onIndexChange = onIndexChange,
        modifier = modifier,
        title = {
            title?.let {
                Text(
                    text = it,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        },
        inactiveButtonColor = inactiveButtonColor
    )
}

@Composable
fun EnhancedButtonGroup(
    modifier: Modifier = defaultModifier,
    enabled: Boolean,
    items: List<String>,
    selectedIndex: Int,
    title: @Composable RowScope.() -> Unit = {},
    onIndexChange: (Int) -> Unit,
    inactiveButtonColor: Color = MaterialTheme.colorScheme.surface
) {
    EnhancedButtonGroup(
        modifier = modifier,
        enabled = enabled,
        itemCount = items.size,
        selectedIndex = selectedIndex,
        itemContent = {
            AutoSizeText(
                text = items[it],
                style = LocalTextStyle.current.copy(
                    fontSize = 13.sp
                ),
                maxLines = 1
            )
        },
        onIndexChange = onIndexChange,
        title = title,
        inactiveButtonColor = inactiveButtonColor
    )
}

@Composable
fun EnhancedButtonGroup(
    modifier: Modifier = defaultModifier,
    enabled: Boolean = true,
    itemCount: Int,
    selectedIndex: Int,
    itemContent: @Composable (item: Int) -> Unit,
    title: @Composable RowScope.() -> Unit = {},
    onIndexChange: (Int) -> Unit,
    inactiveButtonColor: Color = MaterialTheme.colorScheme.surface,
    activeButtonColor: Color = MaterialTheme.colorScheme.secondary,
    isScrollable: Boolean = true
) {
    EnhancedButtonGroup(
        modifier = modifier,
        enabled = enabled,
        itemCount = itemCount,
        selectedIndices = setOf(selectedIndex),
        itemContent = itemContent,
        title = title,
        onIndexChange = onIndexChange,
        inactiveButtonColor = inactiveButtonColor,
        activeButtonColor = activeButtonColor,
        isScrollable = isScrollable,
    )
}

@Composable
fun EnhancedButtonGroup(
    modifier: Modifier = defaultModifier,
    enabled: Boolean = true,
    itemCount: Int,
    selectedIndices: Set<Int>,
    itemContent: @Composable (item: Int) -> Unit,
    title: @Composable RowScope.() -> Unit = {},
    onIndexChange: (Int) -> Unit,
    inactiveButtonColor: Color = MaterialTheme.colorScheme.surface,
    activeButtonColor: Color = MaterialTheme.colorScheme.secondary,
    isScrollable: Boolean = true,
) {
    val settingsState = LocalSettingsState.current

    val disabledColor = MaterialTheme.colorScheme.onSurface
        .copy(alpha = 0.38f)
        .compositeOver(MaterialTheme.colorScheme.surface)

    ProvideTextStyle(
        value = LocalTextStyle.current.copy(
            color = if (!enabled) disabledColor
            else Color.Unspecified
        )
    ) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                content = title
            )
            val scrollState = rememberScrollState()
            val elevation by animateDpAsState(
                if (settingsState.borderWidth > 0.dp || !enabled) 0.dp else 0.5.dp
            )

            LocalMinimumInteractiveComponentSize.ProvidesValue(Dp.Unspecified) {
                MaterialTheme(
                    motionScheme = object : MotionScheme by MotionScheme.expressive() {
                        override fun <T> fastSpatialSpec(): FiniteAnimationSpec<T> = tween(400)
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .height(IntrinsicSize.Max)
                            .then(
                                if (isScrollable) {
                                    Modifier
                                        .fadingEdges(scrollState)
                                        .horizontalScroll(scrollState)
                                } else Modifier.fillMaxWidth()
                            )
                            .padding(
                                start = 6.dp,
                                end = 6.dp,
                                bottom = 6.dp,
                                top = 8.dp
                            ),
                        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                    ) {
                        repeat(itemCount) { index ->
                            val activeContainerColor = if (enabled) {
                                activeButtonColor
                            } else {
                                MaterialTheme.colorScheme.surfaceContainer
                            }

                            val selected = index in selectedIndices

                            EnhancedToggleButton(
                                enabled = enabled,
                                onCheckedChange = {
                                    onIndexChange(index)
                                },
                                border = BorderStroke(
                                    width = settingsState.borderWidth,
                                    color = MaterialTheme.colorScheme.outlineVariant(
                                        onTopOf = if (selected) activeContainerColor
                                        else inactiveButtonColor
                                    )
                                ),
                                colors = ToggleButtonDefaults.toggleButtonColors(
                                    containerColor = inactiveButtonColor,
                                    contentColor = contentColorFor(inactiveButtonColor),
                                    checkedContainerColor = activeContainerColor,
                                    checkedContentColor = contentColorFor(activeContainerColor)
                                ),
                                checked = selected,
                                shapes = when (index) {
                                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes(
                                        pressedShape = ButtonDefaults.pressedShape
                                    )

                                    itemCount - 1 -> ButtonGroupDefaults.connectedTrailingButtonShapes(
                                        pressedShape = ButtonDefaults.pressedShape
                                    )

                                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes(
                                        pressedShape = ButtonDefaults.pressedShape,
                                    )
                                },
                                elevation = elevation,
                                modifier = Modifier.then(
                                    if (isScrollable) Modifier
                                    else Modifier.weight(1f)
                                )
                            ) {
                                itemContent(index)
                            }
                        }
                    }
                }
            }
        }
    }
}

private val defaultModifier = Modifier
    .fillMaxWidth()
    .padding(8.dp)