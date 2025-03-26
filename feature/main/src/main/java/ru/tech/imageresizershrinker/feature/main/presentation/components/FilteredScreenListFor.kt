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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import ru.tech.imageresizershrinker.core.settings.presentation.provider.LocalSettingsState
import ru.tech.imageresizershrinker.core.ui.utils.helper.ContextUtils.getStringLocalized
import ru.tech.imageresizershrinker.core.ui.utils.navigation.Screen
import java.util.Locale

@Composable
internal fun filteredScreenListFor(
    screenSearchKeyword: String,
    selectedNavigationItem: Int,
    showScreenSearch: Boolean
): State<List<Screen>> {
    val settingsState = LocalSettingsState.current
    val context = LocalContext.current
    val canSearchScreens = settingsState.screensSearchEnabled

    val screenList by remember(settingsState.screenList) {
        derivedStateOf {
            settingsState.screenList.mapNotNull {
                Screen.entries.find { s -> s.id == it }
            }.takeIf { it.isNotEmpty() } ?: Screen.entries
        }
    }

    return remember(
        settingsState.groupOptionsByTypes,
        settingsState.favoriteScreenList,
        screenSearchKeyword,
        screenList,
        selectedNavigationItem
    ) {
        derivedStateOf {
            if (settingsState.groupOptionsByTypes && (screenSearchKeyword.isEmpty() && !showScreenSearch)) {
                Screen.typedEntries[selectedNavigationItem].entries
            } else if (!settingsState.groupOptionsByTypes && (screenSearchKeyword.isEmpty() && !showScreenSearch)) {
                if (selectedNavigationItem == 0) {
                    screenList.filter {
                        it.id in settingsState.favoriteScreenList
                    }
                } else screenList
            } else {
                screenList
            }.let { screens ->
                if (screenSearchKeyword.isNotEmpty() && canSearchScreens) {
                    screens.filter {
                        val string =
                            context.getString(it.title) + " " + context.getString(it.subtitle)
                        val stringEn = context.getStringLocalized(it.title, Locale.ENGLISH)
                            .plus(" ")
                            .plus(context.getStringLocalized(it.subtitle, Locale.ENGLISH))
                        stringEn.contains(other = screenSearchKeyword, ignoreCase = true).or(
                            string.contains(other = screenSearchKeyword, ignoreCase = true)
                        )
                    }
                } else screens
            }
        }
    }
}