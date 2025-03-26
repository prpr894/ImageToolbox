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

package ru.tech.imageresizershrinker.core.filters.presentation.model

import ru.tech.imageresizershrinker.core.filters.domain.model.BlurEdgeMode
import ru.tech.imageresizershrinker.core.filters.domain.model.Filter
import ru.tech.imageresizershrinker.core.filters.domain.model.FilterParam
import ru.tech.imageresizershrinker.core.filters.domain.model.TransferFunc
import ru.tech.imageresizershrinker.core.resources.R

class UiLinearFastGaussianBlurFilter(
    override val value: Triple<Int, TransferFunc, BlurEdgeMode> = Triple(
        first = 10,
        second = TransferFunc.SRGB,
        third = BlurEdgeMode.Reflect101
    )
) : UiFilter<Triple<Int, TransferFunc, BlurEdgeMode>>(
    title = R.string.linear_fast_gaussian_blur,
    value = value,
    paramsInfo = listOf(
        FilterParam(
            title = R.string.radius,
            valueRange = 1f..300f,
            roundTo = 0
        ),
        FilterParam(
            title = R.string.tag_transfer_function,
            valueRange = 0f..0f,
            roundTo = 0
        ),
        FilterParam(
            title = R.string.edge_mode,
            valueRange = 0f..0f,
            roundTo = 0
        )
    )
), Filter.LinearFastGaussianBlur