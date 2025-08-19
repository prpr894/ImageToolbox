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

package com.t8rin.imagetoolbox.core.filters.presentation.model

import com.t8rin.imagetoolbox.core.filters.domain.model.Filter
import com.t8rin.imagetoolbox.core.filters.domain.model.FilterParam
import com.t8rin.imagetoolbox.core.filters.domain.model.params.PinchParams
import com.t8rin.imagetoolbox.core.resources.R

class UiPinchFilter(
    override val value: PinchParams = PinchParams.Default
) : UiFilter<PinchParams>(
    title = R.string.whirl_and_pinch,
    value = value,
    paramsInfo = listOf(
        FilterParam(R.string.angle, 0f..360f, 0),
        R.string.center_x paramTo 0f..1f,
        R.string.center_y paramTo 0f..1f,
        R.string.radius paramTo 0f..2f,
        R.string.amount paramTo -1f..1f
    )
), Filter.Pinch