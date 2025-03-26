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

import android.content.Context
import android.graphics.Bitmap
import androidx.core.net.toUri
import com.t8rin.trickle.Trickle
import com.t8rin.trickle.TrickleUtils
import dagger.assisted.AssistedInject
import ru.tech.imageresizershrinker.core.domain.model.FileModel
import ru.tech.imageresizershrinker.core.domain.model.IntegerSize
import ru.tech.imageresizershrinker.core.domain.transformation.Transformation
import ru.tech.imageresizershrinker.core.filters.domain.model.Filter

internal class CubeLutFilter @AssistedInject internal constructor(
    override val value: Pair<Float, FileModel> = 1f to FileModel(""),
    private val context: Context
) : Transformation<Bitmap>, Filter.CubeLut {

    override val cacheKey: String
        get() = value.hashCode().toString()

    override suspend fun transform(
        input: Bitmap,
        size: IntegerSize
    ): Bitmap {
        if (value.second.uri.isEmpty()) return input

        val lutPath = TrickleUtils.getAbsolutePath(value.second.uri.toUri(), context)

        return Trickle.applyCubeLut(
            input = input,
            cubeLutPath = lutPath,
            intensity = value.first
        )
    }

}