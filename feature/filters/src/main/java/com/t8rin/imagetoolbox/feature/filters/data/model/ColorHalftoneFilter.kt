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

package com.t8rin.imagetoolbox.feature.filters.data.model

import com.jhlabs.ColorHalftoneFilter
import com.jhlabs.JhFilter
import com.t8rin.imagetoolbox.core.domain.utils.Quad
import com.t8rin.imagetoolbox.core.filters.domain.model.Filter
import com.t8rin.imagetoolbox.feature.filters.data.transformation.JhFilterTransformation

internal class ColorHalftoneFilter(
    override val value: Quad<Float, Float, Float, Float> = Quad(
        first = 2f,
        second = 108f,
        third = 162f,
        fourth = 90f
    )
) : JhFilterTransformation(), Filter.ColorHalftone {

    override val cacheKey: String
        get() = value.hashCode().toString()

    override fun createFilter(): JhFilter = ColorHalftoneFilter().apply {
        setdotRadius(value.first)
        cyanScreenAngle = Math.toRadians(value.second.toDouble()).toFloat()
        magentaScreenAngle = Math.toRadians(value.third.toDouble()).toFloat()
        yellowScreenAngle = Math.toRadians(value.fourth.toDouble()).toFloat()
    }

}