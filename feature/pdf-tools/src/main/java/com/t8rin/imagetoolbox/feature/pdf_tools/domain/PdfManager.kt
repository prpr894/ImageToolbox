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

package com.t8rin.imagetoolbox.feature.pdf_tools.domain

import com.t8rin.imagetoolbox.core.domain.image.model.Preset
import com.t8rin.imagetoolbox.core.domain.model.IntegerSize

interface PdfManager<I> {

    suspend fun getPdfPages(
        uri: String,
        password: String?
    ): List<Int>

    suspend fun getPdfPageSizes(
        uri: String,
        password: String?
    ): List<IntegerSize>

    suspend fun convertImagesToPdf(
        imageUris: List<String>,
        onProgressChange: suspend (Int) -> Unit,
        scaleSmallImagesToLarge: Boolean,
        preset: Preset.Percentage,
        tempFilename: String
    ): String

    suspend fun convertPdfToImages(
        pdfUri: String,
        password: String?,
        onFailure: (Throwable) -> Unit,
        pages: List<Int>?,
        preset: Preset.Percentage,
        onGetPagesCount: suspend (Int) -> Unit,
        onProgressChange: suspend (Int, I) -> Unit,
        onComplete: suspend () -> Unit = {}
    )

}