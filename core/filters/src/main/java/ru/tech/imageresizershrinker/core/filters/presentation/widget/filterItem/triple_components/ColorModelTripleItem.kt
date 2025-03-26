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

package ru.tech.imageresizershrinker.core.filters.presentation.widget.filterItem.triple_components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.tech.imageresizershrinker.core.domain.model.ColorModel
import ru.tech.imageresizershrinker.core.filters.presentation.model.UiFilter
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.ui.utils.helper.toColor
import ru.tech.imageresizershrinker.core.ui.utils.helper.toModel
import ru.tech.imageresizershrinker.core.ui.widget.color_picker.ColorSelectionRowDefaults
import ru.tech.imageresizershrinker.core.ui.widget.controls.selection.ColorRowSelector

@Composable
internal fun ColorModelTripleItem(
    value: Triple<ColorModel, ColorModel, ColorModel>,
    filter: UiFilter<Triple<*, *, *>>,
    onFilterChange: (value: Triple<ColorModel, ColorModel, ColorModel>) -> Unit,
    previewOnly: Boolean
) {
    Box(
        modifier = Modifier.padding(
            start = 16.dp,
            top = 16.dp,
            end = 16.dp
        )
    ) {
        var color1 by remember(value) { mutableStateOf(value.first.toColor()) }
        var color2 by remember(value) { mutableStateOf(value.second.toColor()) }
        var color3 by remember(value) { mutableStateOf(value.third.toColor()) }

        Column {
            ColorRowSelector(
                title = stringResource(R.string.first_color),
                value = color1,
                onValueChange = {
                    color1 = it
                    onFilterChange(
                        Triple(
                            color1.toModel(),
                            color2.toModel(),
                            color3.toModel()
                        )
                    )
                },
                allowScroll = !previewOnly,
                icon = null,
                defaultColors = ColorSelectionRowDefaults.colorList,
                titleFontWeight = FontWeight.Normal,
                contentHorizontalPadding = 0.dp
            )
            Spacer(Modifier.height(8.dp))
            ColorRowSelector(
                title = stringResource(R.string.second_color),
                value = color2,
                onValueChange = {
                    color2 = it
                    onFilterChange(
                        Triple(
                            color1.toModel(),
                            color2.toModel(),
                            color3.toModel()
                        )
                    )
                },
                allowScroll = !previewOnly,
                icon = null,
                defaultColors = ColorSelectionRowDefaults.colorList,
                titleFontWeight = FontWeight.Normal,
                contentHorizontalPadding = 0.dp
            )
            Spacer(Modifier.height(8.dp))
            ColorRowSelector(
                title = stringResource(R.string.third_color),
                value = color3,
                onValueChange = {
                    color3 = it
                    onFilterChange(
                        Triple(
                            color1.toModel(),
                            color2.toModel(),
                            color3.toModel()
                        )
                    )
                },
                allowScroll = !previewOnly,
                icon = null,
                defaultColors = ColorSelectionRowDefaults.colorList,
                titleFontWeight = FontWeight.Normal,
                contentHorizontalPadding = 0.dp
            )
        }
    }
}