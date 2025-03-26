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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.resources.icons.Toolbox
import ru.tech.imageresizershrinker.core.settings.presentation.provider.LocalSettingsState
import ru.tech.imageresizershrinker.core.ui.theme.outlineVariant
import ru.tech.imageresizershrinker.core.ui.widget.modifier.container
import ru.tech.imageresizershrinker.core.ui.widget.text.marquee

@Composable
internal fun MainNavigationRailForFavorites(
    selectedIndex: Int,
    onValueChange: (Int) -> Unit
) {
    val settingsState = LocalSettingsState.current

    Row {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(min = 80.dp)
                .container(
                    shape = RectangleShape,
                    autoShadowElevation = 10.dp,
                    resultPadding = 0.dp
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 8.dp)
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(
                        start = WindowInsets
                            .statusBars
                            .asPaddingValues()
                            .calculateStartPadding(LocalLayoutDirection.current)
                    )
                    .padding(
                        start = WindowInsets
                            .displayCutout
                            .asPaddingValues()
                            .calculateStartPadding(LocalLayoutDirection.current)
                    ),
                verticalArrangement = Arrangement.spacedBy(
                    4.dp,
                    Alignment.CenterVertically
                ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(8.dp))
                val haptics = LocalHapticFeedback.current
                NavigationRailItem(
                    modifier = Modifier
                        .height(height = 56.dp)
                        .width(100.dp),
                    selected = selectedIndex == 0,
                    onClick = {
                        onValueChange(0)
                        haptics.performHapticFeedback(
                            HapticFeedbackType.LongPress
                        )
                    },
                    icon = {
                        AnimatedContent(
                            targetState = selectedIndex == 0,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            }
                        ) { selected ->
                            Icon(
                                imageVector = if (selected) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                                contentDescription = null
                            )
                        }
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.favorite),
                            modifier = Modifier.marquee()
                        )
                    }
                )

                NavigationRailItem(
                    modifier = Modifier
                        .height(height = 56.dp)
                        .width(100.dp),
                    selected = selectedIndex == 1,
                    onClick = {
                        onValueChange(1)
                        haptics.performHapticFeedback(
                            HapticFeedbackType.LongPress
                        )
                    },
                    icon = {
                        AnimatedContent(
                            targetState = selectedIndex == 1,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            }
                        ) { selected ->
                            Icon(
                                imageVector = if (selected) Icons.Rounded.Toolbox else Icons.Outlined.Toolbox,
                                contentDescription = null
                            )
                        }
                    },
                    label = {
                        Text(
                            text = stringResource(R.string.tools),
                            modifier = Modifier.marquee()
                        )
                    }
                )
                Spacer(Modifier.height(8.dp))
            }
        }
        Box(
            Modifier
                .fillMaxHeight()
                .width(settingsState.borderWidth)
                .background(
                    MaterialTheme.colorScheme.outlineVariant(
                        0.3f,
                        DrawerDefaults.standardContainerColor
                    )
                )
        )
    }
}