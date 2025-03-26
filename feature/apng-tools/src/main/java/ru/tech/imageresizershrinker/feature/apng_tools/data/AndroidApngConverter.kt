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

package ru.tech.imageresizershrinker.feature.apng_tools.data

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import com.awxkee.jxlcoder.JxlCoder
import com.awxkee.jxlcoder.JxlDecodingSpeed
import com.awxkee.jxlcoder.JxlEffort
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import oupson.apng.decoder.ApngDecoder
import oupson.apng.encoder.ApngEncoder
import ru.tech.imageresizershrinker.core.domain.dispatchers.DispatchersHolder
import ru.tech.imageresizershrinker.core.domain.image.ImageGetter
import ru.tech.imageresizershrinker.core.domain.image.ImageScaler
import ru.tech.imageresizershrinker.core.domain.image.ShareProvider
import ru.tech.imageresizershrinker.core.domain.image.model.ImageFormat
import ru.tech.imageresizershrinker.core.domain.image.model.ImageInfo
import ru.tech.imageresizershrinker.core.domain.image.model.Quality
import ru.tech.imageresizershrinker.core.domain.image.model.ResizeType
import ru.tech.imageresizershrinker.core.domain.model.IntegerSize
import ru.tech.imageresizershrinker.core.domain.utils.runSuspendCatching
import ru.tech.imageresizershrinker.feature.apng_tools.domain.ApngConverter
import ru.tech.imageresizershrinker.feature.apng_tools.domain.ApngParams
import java.io.ByteArrayOutputStream
import javax.inject.Inject


internal class AndroidApngConverter @Inject constructor(
    private val imageGetter: ImageGetter<Bitmap, ExifInterface>,
    private val imageShareProvider: ShareProvider<Bitmap>,
    private val imageScaler: ImageScaler<Bitmap>,
    @ApplicationContext private val context: Context,
    dispatchersHolder: DispatchersHolder
) : DispatchersHolder by dispatchersHolder, ApngConverter {

    override fun extractFramesFromApng(
        apngUri: String,
        imageFormat: ImageFormat,
        quality: Quality
    ): Flow<String> = channelFlow {
        ApngDecoder(
            context = context,
            uri = apngUri.toUri()
        ).decodeAsync(defaultDispatcher) { frame ->
            if (!currentCoroutineContext().isActive) {
                currentCoroutineContext().cancel(null)
                return@decodeAsync
            }
            imageShareProvider.cacheImage(
                image = frame,
                imageInfo = ImageInfo(
                    width = frame.width,
                    height = frame.height,
                    imageFormat = imageFormat,
                    quality = quality
                )
            )?.let { send(it) }
        }
    }

    override suspend fun createApngFromImageUris(
        imageUris: List<String>,
        params: ApngParams,
        onFailure: (Throwable) -> Unit,
        onProgress: () -> Unit
    ): ByteArray? = withContext(defaultDispatcher) {
        val out = ByteArrayOutputStream()
        val size = params.size ?: imageGetter.getImage(data = imageUris[0])!!.run {
            IntegerSize(width, height)
        }

        if (size.width <= 0 || size.height <= 0) {
            onFailure(IllegalArgumentException("Width and height must be > 0"))
            return@withContext null
        }

        val encoder = ApngEncoder(
            outputStream = out,
            width = size.width,
            height = size.height,
            numberOfFrames = imageUris.size
        ).apply {
            setOptimiseApng(false)
            setRepetitionCount(params.repeatCount)
            setCompressionLevel(params.quality.qualityValue)
        }
        imageUris.forEach { uri ->
            imageGetter.getImage(
                data = uri,
                size = size
            )?.let {
                encoder.writeFrame(
                    btm = imageScaler.scaleImage(
                        image = imageScaler.scaleImage(
                            image = it,
                            width = size.width,
                            height = size.height,
                            resizeType = ResizeType.Flexible
                        ),
                        width = size.width,
                        height = size.height,
                        resizeType = ResizeType.CenterCrop(
                            canvasColor = Color.Transparent.toArgb()
                        )
                    ),
                    delay = params.delay.toFloat()
                )
            }
            onProgress()
        }
        encoder.writeEnd()

        out.toByteArray()
    }

    override suspend fun convertApngToJxl(
        apngUris: List<String>,
        quality: Quality.Jxl,
        onProgress: suspend (String, ByteArray) -> Unit
    ) = withContext(defaultDispatcher) {
        apngUris.forEach { uri ->
            uri.bytes?.let { apngData ->
                runSuspendCatching {
                    JxlCoder.Convenience.apng2JXL(
                        apngData = apngData,
                        quality = quality.qualityValue,
                        effort = JxlEffort.entries.first { it.ordinal == quality.effort },
                        decodingSpeed = JxlDecodingSpeed.entries.first { it.ordinal == quality.speed }
                    ).let {
                        onProgress(uri, it)
                    }
                }
            }
        }
    }

    private val String.bytes: ByteArray?
        get() = context
            .contentResolver
            .openInputStream(toUri())?.use {
                it.readBytes()
            }


}