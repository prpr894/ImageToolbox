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

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.colordetector.util.ColorUtil.roundToTwoDigits
import kotlinx.collections.immutable.persistentMapOf
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.settings.presentation.provider.LocalSettingsState
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedSliderItem
import ru.tech.imageresizershrinker.core.ui.widget.modifier.ContainerShapeDefaults

@Composable
fun FontScaleSettingItem(
    onValueChange: (Float) -> Unit,
    shape: Shape = ContainerShapeDefaults.bottomShape,
    modifier: Modifier = Modifier.padding(horizontal = 8.dp)
) {
    val settingsState = LocalSettingsState.current
    val context = LocalContext.current

    var sliderValue by remember(settingsState.fontScale) {
        mutableFloatStateOf(settingsState.fontScale ?: 0.45f)
    }

    EnhancedSliderItem(
        modifier = modifier,
        shape = shape,
        value = sliderValue,
        title = stringResource(R.string.font_scale),
        icon = Icons.Rounded.TextFields,
        onValueChange = {
            sliderValue = it.roundToTwoDigits()
        },
        internalStateTransformation = {
            it.roundToTwoDigits()
        },
        onValueChangeFinished = {
            onValueChange(
                if (sliderValue < 0.5f) 0f
                else sliderValue
            )
        },
        valueRange = 0.45f..1.5f,
        steps = 20,
        valuesPreviewMapping = remember {
            persistentMapOf(0.45f to context.getString(R.string.defaultt))
        },
        valueTextTapEnabled = false
    )
}