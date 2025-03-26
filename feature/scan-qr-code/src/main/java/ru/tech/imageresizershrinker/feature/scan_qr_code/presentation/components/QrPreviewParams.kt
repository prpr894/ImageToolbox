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

package ru.tech.imageresizershrinker.feature.scan_qr_code.presentation.components

import android.net.Uri
import ru.tech.imageresizershrinker.core.settings.presentation.model.UiFontFamily
import ru.tech.imageresizershrinker.core.ui.widget.other.BarcodeType

data class QrPreviewParams(
    val imageUri: Uri?,
    val description: String,
    val content: String,
    val cornersSize: Int,
    val descriptionFont: UiFontFamily,
    val heightRatio: Float,
    val type: BarcodeType,
    val enforceBlackAndWhite: Boolean
) {
    companion object {
        val Default by lazy {
            QrPreviewParams(
                imageUri = null,
                description = "",
                content = "",
                cornersSize = 4,
                descriptionFont = UiFontFamily.System,
                heightRatio = 2f,
                type = BarcodeType.QR_CODE,
                enforceBlackAndWhite = false
            )
        }
    }
}