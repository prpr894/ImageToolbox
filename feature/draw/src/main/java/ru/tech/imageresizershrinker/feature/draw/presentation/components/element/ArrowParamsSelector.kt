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

package ru.tech.imageresizershrinker.feature.draw.presentation.components.element

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.tech.imageresizershrinker.core.domain.utils.roundTo
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedSliderItem
import ru.tech.imageresizershrinker.core.ui.widget.modifier.ContainerShapeDefaults
import ru.tech.imageresizershrinker.feature.draw.domain.DrawPathMode
import ru.tech.imageresizershrinker.feature.draw.presentation.components.utils.angle
import ru.tech.imageresizershrinker.feature.draw.presentation.components.utils.isArrow
import ru.tech.imageresizershrinker.feature.draw.presentation.components.utils.sizeScale
import ru.tech.imageresizershrinker.feature.draw.presentation.components.utils.updateArrow

@Composable
internal fun ArrowParamsSelector(
    value: DrawPathMode,
    onValueChange: (DrawPathMode) -> Unit
) {
    AnimatedVisibility(
        visible = value.isArrow(),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Column {
            EnhancedSliderItem(
                value = value.sizeScale(),
                title = stringResource(R.string.head_length_scale),
                valueRange = 0.5f..8f,
                internalStateTransformation = {
                    it.roundTo(1)
                },
                onValueChange = {
                    onValueChange(
                        value.updateArrow(sizeScale = it)
                    )
                },
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = ContainerShapeDefaults.topShape
            )
            Spacer(modifier = Modifier.height(4.dp))
            EnhancedSliderItem(
                value = value.angle() - 90f,
                title = stringResource(R.string.angle),
                valueRange = 0f..90f,
                internalStateTransformation = {
                    it.roundTo(1)
                },
                onValueChange = {
                    onValueChange(
                        value.updateArrow(angle = it + 90f)
                    )
                },
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = ContainerShapeDefaults.bottomShape
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}