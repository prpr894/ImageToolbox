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

import ru.tech.imageresizershrinker.core.filters.domain.model.ClaheParams
import ru.tech.imageresizershrinker.core.filters.domain.model.Filter
import ru.tech.imageresizershrinker.core.filters.domain.model.FilterParam
import ru.tech.imageresizershrinker.core.resources.R

class UiClaheLABFilter(
    override val value: ClaheParams = ClaheParams.Default
) : UiFilter<ClaheParams>(
    title = R.string.clahe_lab,
    paramsInfo = listOf(
        FilterParam(R.string.threshold, -10f..10f, 2),
        FilterParam(R.string.grid_size_x, 1f..100f, 0),
        FilterParam(R.string.grid_size_y, 1f..100f, 0),
        FilterParam(R.string.bins_count, 2f..256f, 0)
    ),
    value = value
), Filter.ClaheLAB