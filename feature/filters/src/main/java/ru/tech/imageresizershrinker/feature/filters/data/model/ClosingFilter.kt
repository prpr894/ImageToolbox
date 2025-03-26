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

package ru.tech.imageresizershrinker.feature.filters.data.model

import android.graphics.Bitmap
import com.awxkee.aire.Aire
import com.awxkee.aire.EdgeMode
import com.awxkee.aire.MorphKernels
import com.awxkee.aire.MorphOp
import com.awxkee.aire.MorphOpMode
import com.awxkee.aire.Scalar
import com.t8rin.trickle.TrickleUtils.checkHasAlpha
import ru.tech.imageresizershrinker.core.domain.model.IntegerSize
import ru.tech.imageresizershrinker.core.domain.transformation.Transformation
import ru.tech.imageresizershrinker.core.filters.domain.model.Filter

internal class ClosingFilter(
    override val value: Pair<Float, Boolean> = 25f to true
) : Transformation<Bitmap>, Filter.Closing {

    override val cacheKey: String
        get() = value.hashCode().toString()

    override suspend fun transform(
        input: Bitmap,
        size: IntegerSize
    ): Bitmap = Aire.morphology(
        bitmap = input,
        kernel = if (value.second) {
            MorphKernels.circle(value.first.toInt())
        } else {
            MorphKernels.box(value.first.toInt())
        },
        morphOp = MorphOp.CLOSING,
        morphOpMode = if (input.checkHasAlpha()) MorphOpMode.RGBA
        else MorphOpMode.RGB,
        borderMode = EdgeMode.REFLECT_101,
        kernelHeight = value.first.toInt(),
        kernelWidth = value.first.toInt(),
        borderScalar = Scalar.ZEROS
    )

}