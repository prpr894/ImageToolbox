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

import androidx.compose.runtime.Composable
import ru.tech.imageresizershrinker.core.domain.model.ColorModel
import ru.tech.imageresizershrinker.core.domain.model.FileModel
import ru.tech.imageresizershrinker.core.domain.model.ImageModel
import ru.tech.imageresizershrinker.core.domain.utils.cast
import ru.tech.imageresizershrinker.core.filters.domain.model.BlurEdgeMode
import ru.tech.imageresizershrinker.core.filters.domain.model.TransferFunc
import ru.tech.imageresizershrinker.core.filters.presentation.model.UiFilter
import ru.tech.imageresizershrinker.core.filters.presentation.widget.filterItem.pair_components.ColorModelPairItem
import ru.tech.imageresizershrinker.core.filters.presentation.widget.filterItem.pair_components.FloatColorModelPairItem
import ru.tech.imageresizershrinker.core.filters.presentation.widget.filterItem.pair_components.FloatFileModelPairItem
import ru.tech.imageresizershrinker.core.filters.presentation.widget.filterItem.pair_components.FloatImageModelPairItem
import ru.tech.imageresizershrinker.core.filters.presentation.widget.filterItem.pair_components.NumberBlurEdgeModePairItem
import ru.tech.imageresizershrinker.core.filters.presentation.widget.filterItem.pair_components.NumberBooleanPairItem
import ru.tech.imageresizershrinker.core.filters.presentation.widget.filterItem.pair_components.NumberPairItem
import ru.tech.imageresizershrinker.core.filters.presentation.widget.filterItem.pair_components.NumberTransferFuncPairItem

@Composable
internal fun PairItem(
    value: Pair<*, *>,
    filter: UiFilter<Pair<*, *>>,
    onFilterChange: (value: Pair<*, *>) -> Unit,
    previewOnly: Boolean
) {
    when {
        value.first is Number && value.second is Number -> {
            NumberPairItem(
                value = value.cast(),
                filter = filter,
                onFilterChange = onFilterChange,
                previewOnly = previewOnly
            )
        }

        value.first is ColorModel && value.second is ColorModel -> {
            ColorModelPairItem(
                value = value.cast(),
                filter = filter,
                onFilterChange = onFilterChange,
                previewOnly = previewOnly
            )
        }

        value.first is Float && value.second is ColorModel -> {
            FloatColorModelPairItem(
                value = value.cast(),
                filter = filter,
                onFilterChange = onFilterChange,
                previewOnly = previewOnly
            )
        }

        value.first is Float && value.second is ImageModel -> {
            FloatImageModelPairItem(
                value = value.cast(),
                filter = filter,
                onFilterChange = onFilterChange,
                previewOnly = previewOnly
            )
        }

        value.first is Float && value.second is FileModel -> {
            FloatFileModelPairItem(
                value = value.cast(),
                filter = filter,
                onFilterChange = onFilterChange,
                previewOnly = previewOnly
            )
        }

        value.first is Number && value.second is Boolean -> {
            NumberBooleanPairItem(
                value = value.cast(),
                filter = filter,
                onFilterChange = onFilterChange,
                previewOnly = previewOnly
            )
        }

        value.first is Number && value.second is BlurEdgeMode -> {
            NumberBlurEdgeModePairItem(
                value = value.cast(),
                filter = filter,
                onFilterChange = onFilterChange,
                previewOnly = previewOnly
            )
        }

        value.first is Number && value.second is TransferFunc -> {
            NumberTransferFuncPairItem(
                value = value.cast(),
                filter = filter,
                onFilterChange = onFilterChange,
                previewOnly = previewOnly
            )
        }
    }
}