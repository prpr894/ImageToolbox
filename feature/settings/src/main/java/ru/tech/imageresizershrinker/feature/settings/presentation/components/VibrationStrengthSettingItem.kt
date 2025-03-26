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
import androidx.compose.material.icons.outlined.Power
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentMapOf
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.settings.presentation.provider.LocalSettingsState
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedSliderItem
import ru.tech.imageresizershrinker.core.ui.widget.modifier.ContainerShapeDefaults
import kotlin.math.roundToInt

@Composable
fun VibrationStrengthSettingItem(
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
        .padding(horizontal = 8.dp),
    shape: Shape = ContainerShapeDefaults.defaultShape
) {
    val settingsState = LocalSettingsState.current
    val context = LocalContext.current

    EnhancedSliderItem(
        modifier = modifier,
        shape = shape,
        value = settingsState.hapticsStrength,
        title = stringResource(R.string.vibration_strength),
        icon = Icons.Outlined.Power,
        onValueChange = {
            onValueChange(it.roundToInt())
        },
        internalStateTransformation = {
            it.roundToInt()
        },
        valueRange = 0f..2f,
        valuesPreviewMapping = remember {
            persistentMapOf(0f to context.getString(R.string.disabled))
        },
        steps = 1,
        valueTextTapEnabled = false
    )
}