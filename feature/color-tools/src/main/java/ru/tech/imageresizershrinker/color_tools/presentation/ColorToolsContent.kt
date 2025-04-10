/*
 * ImageToolbox is an image copyor for android
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

package ru.tech.imageresizershrinker.color_tools.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.colordetector.parser.rememberColorParser
import com.t8rin.dynamic.theme.LocalDynamicThemeState
import ru.tech.imageresizershrinker.color_tools.presentation.components.ColorHarmonies
import ru.tech.imageresizershrinker.color_tools.presentation.components.ColorHistogram
import ru.tech.imageresizershrinker.color_tools.presentation.components.ColorInfo
import ru.tech.imageresizershrinker.color_tools.presentation.components.ColorMixing
import ru.tech.imageresizershrinker.color_tools.presentation.components.ColorShading
import ru.tech.imageresizershrinker.color_tools.presentation.screenLogic.ColorToolsComponent
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.settings.presentation.provider.LocalSettingsState
import ru.tech.imageresizershrinker.core.settings.presentation.provider.rememberAppColorTuple
import ru.tech.imageresizershrinker.core.ui.utils.helper.isPortraitOrientationAsState
import ru.tech.imageresizershrinker.core.ui.widget.AdaptiveLayoutScreen
import ru.tech.imageresizershrinker.core.ui.widget.controls.selection.ColorRowSelector
import ru.tech.imageresizershrinker.core.ui.widget.modifier.container
import ru.tech.imageresizershrinker.core.ui.widget.other.TopAppBarEmoji
import ru.tech.imageresizershrinker.core.ui.widget.text.marquee

@Composable
fun ColorToolsContent(
    component: ColorToolsComponent
) {
    val themeState = LocalDynamicThemeState.current
    val settingsState = LocalSettingsState.current
    val allowChangeColor = settingsState.allowChangeColorByImage

    val appColorTuple = rememberAppColorTuple()

    val selectedColor = component.selectedColor.takeOrElse { appColorTuple.primary }

    LaunchedEffect(selectedColor) {
        if (allowChangeColor) {
            themeState.updateColor(selectedColor)
        }
    }

    val isPortrait by isPortraitOrientationAsState()

    AdaptiveLayoutScreen(
        title = {
            Text(
                text = stringResource(R.string.color_tools),
                textAlign = TextAlign.Center,
                modifier = Modifier.marquee()
            )
        },
        shouldDisableBackHandler = true,
        onGoBack = component.onGoBack,
        actions = {},
        topAppBarPersistentActions = {
            TopAppBarEmoji()
        },
        imagePreview = {},
        controls = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val parser = rememberColorParser()

                if (isPortrait) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
                ColorRowSelector(
                    value = selectedColor,
                    onValueChange = component::updateSelectedColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .container(
                            shape = RoundedCornerShape(20.dp)
                        ),
                    title = stringResource(R.string.selected_color)
                )
                Spacer(modifier = Modifier.fillMaxWidth())
                ColorInfo(
                    selectedColor = selectedColor,
                    onColorChange = component::updateSelectedColor,
                    parser = parser
                )
                ColorMixing(
                    selectedColor = selectedColor,
                    appColorTuple = appColorTuple,
                    parser = parser
                )
                ColorHarmonies(
                    selectedColor = selectedColor
                )
                ColorShading(
                    selectedColor = selectedColor
                )
                ColorHistogram()
            }
        },
        buttons = {},
        placeImagePreview = false,
        canShowScreenData = true
    )
}