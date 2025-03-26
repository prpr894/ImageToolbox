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

package ru.tech.imageresizershrinker.feature.base64_tools.data

import android.graphics.Bitmap
import android.util.Base64
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.withContext
import ru.tech.imageresizershrinker.core.domain.dispatchers.DispatchersHolder
import ru.tech.imageresizershrinker.core.domain.image.ImageCompressor
import ru.tech.imageresizershrinker.core.domain.image.ImageGetter
import ru.tech.imageresizershrinker.core.domain.image.ShareProvider
import ru.tech.imageresizershrinker.core.domain.image.model.ImageFormat
import ru.tech.imageresizershrinker.core.domain.image.model.Quality
import ru.tech.imageresizershrinker.core.domain.saving.FileController
import ru.tech.imageresizershrinker.feature.base64_tools.domain.Base64Converter
import javax.inject.Inject

internal class AndroidBase64Converter @Inject constructor(
    private val imageGetter: ImageGetter<Bitmap, ExifInterface>,
    private val fileController: FileController,
    private val shareProvider: ShareProvider<Bitmap>,
    private val imageCompressor: ImageCompressor<Bitmap>,
    dispatchersHolder: DispatchersHolder
) : Base64Converter, DispatchersHolder by dispatchersHolder {

    override suspend fun decode(
        base64: String
    ): String? = withContext(ioDispatcher) {
        val decoded = runCatching {
            Base64.decode(base64, Base64.DEFAULT or Base64.NO_WRAP)
        }.getOrNull() ?: return@withContext null

        imageGetter.getImage(decoded)?.let { bitmap ->
            shareProvider.cacheData(
                writeData = {
                    it.writeBytes(
                        imageCompressor.compress(
                            image = bitmap,
                            imageFormat = ImageFormat.Png.Lossless,
                            quality = Quality.Base()
                        )
                    )
                },
                filename = "Base64_decoded_${System.currentTimeMillis()}.png"
            )
        }
    }

    override suspend fun encode(
        uri: String
    ): String = withContext(ioDispatcher) {
        Base64.encodeToString(fileController.readBytes(uri), Base64.DEFAULT or Base64.NO_WRAP)
    }

}