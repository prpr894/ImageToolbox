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

package ru.tech.imageresizershrinker.image_splitting.domain

import ru.tech.imageresizershrinker.core.domain.image.model.ImageFormat
import ru.tech.imageresizershrinker.core.domain.image.model.Quality

data class SplitParams(
    val rowsCount: Int,
    val columnsCount: Int,
    val imageFormat: ImageFormat,
    val quality: Quality
) {
    companion object {
        val Default by lazy {
            SplitParams(
                rowsCount = 2,
                columnsCount = 2,
                imageFormat = ImageFormat.Default,
                quality = Quality.Base()
            )
        }
    }
}