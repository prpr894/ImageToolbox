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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FitScreen
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.tech.imageresizershrinker.core.domain.image.model.ImageScaleMode
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.settings.presentation.provider.LocalSettingsState
import ru.tech.imageresizershrinker.core.ui.widget.controls.selection.ScaleModeSelector
import ru.tech.imageresizershrinker.core.ui.widget.icon_shape.IconShapeContainer
import ru.tech.imageresizershrinker.core.ui.widget.modifier.ContainerShapeDefaults

@Composable
fun DefaultScaleModeSettingItem(
    onValueChange: (ImageScaleMode) -> Unit,
    shape: Shape = ContainerShapeDefaults.topShape,
    modifier: Modifier = Modifier.padding(horizontal = 8.dp)
) {
    val settingsState = LocalSettingsState.current
    ScaleModeSelector(
        modifier = modifier,
        shape = shape,
        backgroundColor = Color.Unspecified,
        value = settingsState.defaultImageScaleMode,
        onValueChange = onValueChange,
        titlePadding = PaddingValues(
            top = 16.dp,
            start = 16.dp,
            end = 16.dp
        ),
        titleArrangement = Arrangement.Start,
        enableItemsCardBackground = false,
        title = {
            IconShapeContainer(
                enabled = true,
                content = {
                    Icon(
                        imageVector = Icons.Outlined.FitScreen,
                        contentDescription = null
                    )
                }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = stringResource(R.string.scale_mode),
                style = LocalTextStyle.current.copy(lineHeight = 18.sp),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f, false)
            )
        }
    )
}