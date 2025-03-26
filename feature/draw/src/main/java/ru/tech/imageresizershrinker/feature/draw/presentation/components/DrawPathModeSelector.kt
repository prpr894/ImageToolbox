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

package ru.tech.imageresizershrinker.feature.draw.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.ui.widget.buttons.SupportingButton
import ru.tech.imageresizershrinker.core.ui.widget.buttons.ToggleGroupButton
import ru.tech.imageresizershrinker.core.ui.widget.modifier.container
import ru.tech.imageresizershrinker.feature.draw.domain.DrawPathMode
import ru.tech.imageresizershrinker.feature.draw.presentation.components.element.ArrowParamsSelector
import ru.tech.imageresizershrinker.feature.draw.presentation.components.element.DrawPathModeInfoSheet
import ru.tech.imageresizershrinker.feature.draw.presentation.components.element.PolygonParamsSelector
import ru.tech.imageresizershrinker.feature.draw.presentation.components.element.RectParamsSelector
import ru.tech.imageresizershrinker.feature.draw.presentation.components.element.StarParamsSelector
import ru.tech.imageresizershrinker.feature.draw.presentation.components.utils.getIcon
import ru.tech.imageresizershrinker.feature.draw.presentation.components.utils.saveState

@Composable
fun DrawPathModeSelector(
    modifier: Modifier,
    values: List<DrawPathMode> = DrawPathMode.entries,
    value: DrawPathMode,
    onValueChange: (DrawPathMode) -> Unit,
    containerColor: Color = Color.Unspecified
) {
    var isSheetVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(value, values) {
        if (values.find { it::class.isInstance(value) } == null) {
            values.firstOrNull()?.let { onValueChange(it) }
        }
    }

    Column(
        modifier = modifier
            .container(
                shape = RoundedCornerShape(24.dp),
                color = containerColor
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ToggleGroupButton(
            enabled = true,
            itemCount = values.size,
            title = {
                Text(
                    text = stringResource(R.string.draw_path_mode),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(8.dp))
                SupportingButton(
                    onClick = {
                        isSheetVisible = true
                    }
                )
            },
            selectedIndex = remember(values, value) {
                derivedStateOf {
                    values.indexOfFirst {
                        value::class.isInstance(it)
                    }
                }
            }.value,
            buttonIcon = {},
            activeButtonColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            itemContent = {
                Icon(
                    imageVector = values[it].getIcon(),
                    contentDescription = null
                )
            },
            onIndexChange = {
                onValueChange(values[it].saveState(value))
            }
        )

        PolygonParamsSelector(
            value = value,
            onValueChange = onValueChange
        )

        StarParamsSelector(
            value = value,
            onValueChange = onValueChange
        )

        RectParamsSelector(
            value = value,
            onValueChange = onValueChange
        )

        ArrowParamsSelector(
            value = value,
            onValueChange = onValueChange
        )
    }

    DrawPathModeInfoSheet(
        visible = isSheetVisible,
        onDismiss = { isSheetVisible = false },
        values = values
    )
}