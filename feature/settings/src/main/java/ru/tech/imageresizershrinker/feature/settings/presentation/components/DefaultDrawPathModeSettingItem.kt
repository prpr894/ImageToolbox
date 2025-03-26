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

package ru.tech.imageresizershrinker.feature.settings.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.rounded.CheckBoxOutlineBlank
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.resources.icons.FreeArrow
import ru.tech.imageresizershrinker.core.resources.icons.FreeDoubleArrow
import ru.tech.imageresizershrinker.core.resources.icons.FreeDraw
import ru.tech.imageresizershrinker.core.resources.icons.Lasso
import ru.tech.imageresizershrinker.core.resources.icons.Line
import ru.tech.imageresizershrinker.core.resources.icons.LineArrow
import ru.tech.imageresizershrinker.core.resources.icons.LineDoubleArrow
import ru.tech.imageresizershrinker.core.resources.icons.Polygon
import ru.tech.imageresizershrinker.core.resources.icons.Square
import ru.tech.imageresizershrinker.core.resources.icons.Triangle
import ru.tech.imageresizershrinker.core.settings.presentation.provider.LocalSettingsState
import ru.tech.imageresizershrinker.core.ui.widget.buttons.ToggleGroupButton
import ru.tech.imageresizershrinker.core.ui.widget.modifier.ContainerShapeDefaults
import ru.tech.imageresizershrinker.core.ui.widget.modifier.container
import ru.tech.imageresizershrinker.core.ui.widget.text.TitleItem

@Composable
fun DefaultDrawPathModeSettingItem(
    onValueChange: (Int) -> Unit,
    shape: Shape = ContainerShapeDefaults.centerShape,
    modifier: Modifier = Modifier
        .padding(horizontal = 8.dp)
) {
    val settingsState = LocalSettingsState.current

    Column(modifier = modifier.container(shape = shape)) {
        TitleItem(
            text = stringResource(R.string.default_draw_path_mode),
            icon = Icons.Outlined.TouchApp,
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .padding(top = 12.dp)
        )
        ToggleGroupButton(
            enabled = true,
            itemCount = 17,
            title = {},
            selectedIndex = settingsState.defaultDrawPathMode,
            buttonIcon = {},
            activeButtonColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            inactiveButtonColor = MaterialTheme.colorScheme.surfaceContainer,
            itemContent = {
                Icon(
                    imageVector = it.getIcon(),
                    contentDescription = null
                )
            },
            onIndexChange = {
                onValueChange(it)
            }
        )
    }
}

private fun Int.getIcon(): ImageVector = when (this) {
    5 -> Icons.Rounded.LineDoubleArrow
    3 -> Icons.Rounded.FreeDoubleArrow
    0 -> Icons.Rounded.FreeDraw
    1 -> Icons.Rounded.Line
    4 -> Icons.Rounded.LineArrow
    2 -> Icons.Rounded.FreeArrow
    8 -> Icons.Rounded.RadioButtonUnchecked
    7 -> Icons.Rounded.CheckBoxOutlineBlank
    10 -> Icons.Rounded.Circle
    9 -> Icons.Rounded.Square
    6 -> Icons.Rounded.Lasso
    11 -> Icons.Rounded.Triangle
    12 -> Icons.Outlined.Triangle
    13 -> Icons.Rounded.Polygon
    14 -> Icons.Outlined.Polygon
    16 -> Icons.Rounded.StarOutline
    15 -> Icons.Rounded.Star
    else -> Icons.Rounded.HourglassEmpty
}