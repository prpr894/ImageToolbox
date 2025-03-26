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

package ru.tech.imageresizershrinker.core.filters.domain.model

data class EnhancedZoomBlurParams(
    val radius: Int,
    val sigma: Float,
    val centerX: Float,
    val centerY: Float,
    val strength: Float,
    val angle: Float,
) {
    companion object {
        val Default by lazy {
            EnhancedZoomBlurParams(
                radius = 25,
                sigma = 5f,
                centerX = 0.5f,
                centerY = 0.5f,
                strength = 1f,
                angle = 135f
            )
        }
    }
}