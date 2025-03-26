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

package ru.tech.imageresizershrinker.core.filters.presentation.widget.filterItem

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.tech.imageresizershrinker.core.domain.utils.roundTo
import ru.tech.imageresizershrinker.core.filters.domain.model.RadialTiltShiftParams
import ru.tech.imageresizershrinker.core.filters.presentation.model.UiFilter
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedSliderItem

@Composable
internal fun RadialTiltShiftParamsItem(
    value: RadialTiltShiftParams,
    filter: UiFilter<RadialTiltShiftParams>,
    onFilterChange: (value: RadialTiltShiftParams) -> Unit,
    previewOnly: Boolean
) {
    val blurRadius: MutableState<Float> =
        remember(value) { mutableFloatStateOf((value.blurRadius as Number).toFloat()) }
    val sigma: MutableState<Float> =
        remember(value) { mutableFloatStateOf((value.sigma as Number).toFloat()) }
    val anchorX: MutableState<Float> =
        remember(value) { mutableFloatStateOf((value.anchorX as Number).toFloat()) }
    val anchorY: MutableState<Float> =
        remember(value) { mutableFloatStateOf((value.anchorY as Number).toFloat()) }
    val holeRadius: MutableState<Float> =
        remember(value) { mutableFloatStateOf((value.holeRadius as Number).toFloat()) }

    LaunchedEffect(
        blurRadius.value,
        sigma.value,
        anchorX.value,
        anchorY.value,
        holeRadius.value
    ) {
        onFilterChange(
            RadialTiltShiftParams(
                blurRadius = blurRadius.value,
                sigma = sigma.value,
                anchorX = anchorX.value,
                anchorY = anchorY.value,
                holeRadius = holeRadius.value
            )
        )
    }

    val paramsInfo by remember(filter) {
        derivedStateOf {
            filter.paramsInfo.mapIndexedNotNull { index, filterParam ->
                if (filterParam.title == null) return@mapIndexedNotNull null
                when (index) {
                    0 -> blurRadius
                    1 -> sigma
                    2 -> anchorX
                    3 -> anchorY
                    else -> holeRadius
                } to filterParam
            }
        }
    }

    Column(
        modifier = Modifier.padding(8.dp)
    ) {
        paramsInfo.forEach { (state, info) ->
            val (title, valueRange, roundTo) = info
            EnhancedSliderItem(
                enabled = !previewOnly,
                value = state.value,
                title = stringResource(title!!),
                valueRange = valueRange,
                onValueChange = {
                    state.value = it
                },
                internalStateTransformation = {
                    it.roundTo(roundTo)
                },
                behaveAsContainer = false
            )
        }
    }
}