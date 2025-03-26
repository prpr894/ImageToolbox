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

package ru.tech.imageresizershrinker.feature.jxl_tools.data

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import com.awxkee.jxlcoder.JxlAnimatedEncoder
import com.awxkee.jxlcoder.JxlAnimatedImage
import com.awxkee.jxlcoder.JxlChannelsConfiguration
import com.awxkee.jxlcoder.JxlCoder
import com.awxkee.jxlcoder.JxlCompressionOption
import com.awxkee.jxlcoder.JxlDecodingSpeed
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import ru.tech.imageresizershrinker.core.domain.dispatchers.DispatchersHolder
import ru.tech.imageresizershrinker.core.domain.image.ImageGetter
import ru.tech.imageresizershrinker.core.domain.image.ImageScaler
import ru.tech.imageresizershrinker.core.domain.image.ShareProvider
import ru.tech.imageresizershrinker.core.domain.image.model.ImageFormat
import ru.tech.imageresizershrinker.core.domain.image.model.ImageFrames
import ru.tech.imageresizershrinker.core.domain.image.model.ImageInfo
import ru.tech.imageresizershrinker.core.domain.image.model.Quality
import ru.tech.imageresizershrinker.core.domain.image.model.ResizeType
import ru.tech.imageresizershrinker.core.domain.model.IntegerSize
import ru.tech.imageresizershrinker.core.domain.utils.runSuspendCatching
import ru.tech.imageresizershrinker.feature.jxl_tools.domain.AnimatedJxlParams
import ru.tech.imageresizershrinker.feature.jxl_tools.domain.JxlConverter
import javax.inject.Inject


internal class AndroidJxlConverter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageGetter: ImageGetter<Bitmap, ExifInterface>,
    private val imageShareProvider: ShareProvider<Bitmap>,
    private val imageScaler: ImageScaler<Bitmap>,
    dispatchersHolder: DispatchersHolder
) : DispatchersHolder by dispatchersHolder, JxlConverter {

    override suspend fun jpegToJxl(
        jpegUris: List<String>,
        onFailure: (Throwable) -> Unit,
        onProgress: suspend (String, ByteArray) -> Unit
    ) = withContext(defaultDispatcher) {
        jpegUris.forEach { uri ->
            runSuspendCatching {
                uri.jxl?.let { onProgress(uri, it) }
            }.onFailure(onFailure)
        }
    }

    override suspend fun jxlToJpeg(
        jxlUris: List<String>,
        onFailure: (Throwable) -> Unit,
        onProgress: suspend (String, ByteArray) -> Unit
    ) = withContext(defaultDispatcher) {
        jxlUris.forEach { uri ->
            runSuspendCatching {
                uri.jpeg?.let { onProgress(uri, it) }
            }.onFailure(onFailure)
        }
    }

    override suspend fun createJxlAnimation(
        imageUris: List<String>,
        params: AnimatedJxlParams,
        onFailure: (Throwable) -> Unit,
        onProgress: () -> Unit
    ): ByteArray? = withContext(defaultDispatcher) {
        val jxlQuality = params.quality as? Quality.Jxl

        if (jxlQuality == null) {
            onFailure(IllegalArgumentException("Quality Must be Jxl"))
            return@withContext null
        }

        runSuspendCatching {
            val size = params.size ?: imageGetter.getImage(data = imageUris[0])!!.run {
                IntegerSize(width, height)
            }
            if (size.width <= 0 || size.height <= 0) {
                onFailure(IllegalArgumentException("Width and height must be > 0"))
                return@withContext null
            }

            val (quality, compressionOption) = if (params.isLossy) {
                params.quality.qualityValue to JxlCompressionOption.LOSSY
            } else 100 to JxlCompressionOption.LOSSLESS

            val encoder = JxlAnimatedEncoder(
                width = size.width,
                height = size.height,
                numLoops = params.repeatCount,
                channelsConfiguration = when (params.quality.channels) {
                    Quality.Channels.RGBA -> JxlChannelsConfiguration.RGBA
                    Quality.Channels.RGB -> JxlChannelsConfiguration.RGB
                    Quality.Channels.Monochrome -> JxlChannelsConfiguration.MONOCHROME
                },
                compressionOption = compressionOption,
                effort = params.quality.effort.coerceAtLeast(1),
                quality = quality,
                decodingSpeed = JxlDecodingSpeed.entries.first { it.ordinal == params.quality.speed }
            )
            imageUris.forEach { uri ->
                imageGetter.getImage(
                    data = uri,
                    size = params.size
                )?.let {
                    encoder.addFrame(
                        bitmap = imageScaler.scaleImage(
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
                        ).apply {
                            setHasAlpha(true)
                        },
                        duration = params.delay
                    )
                }
                onProgress()
            }
            encoder.encode()
        }.onFailure {
            onFailure(it)
            return@withContext null
        }.getOrNull()
    }

    override fun extractFramesFromJxl(
        jxlUri: String,
        imageFormat: ImageFormat,
        imageFrames: ImageFrames,
        quality: Quality,
        onFailure: (Throwable) -> Unit,
        onGetFramesCount: (frames: Int) -> Unit
    ): Flow<String> = flow {
        val bytes = jxlUri.bytes ?: return@flow

        val decoder = JxlAnimatedImage(bytes)

        onGetFramesCount(decoder.numberOfFrames)
        val indexes = imageFrames
            .getFramePositions(decoder.numberOfFrames)
            .map { it - 1 }
        repeat(decoder.numberOfFrames) { pos ->
            if (!currentCoroutineContext().isActive) {
                currentCoroutineContext().cancel(null)
                return@repeat
            }
            decoder.getFrame(pos).let { frame ->
                imageShareProvider.cacheImage(
                    image = frame,
                    imageInfo = ImageInfo(
                        width = frame.width,
                        height = frame.height,
                        imageFormat = imageFormat,
                        quality = quality
                    )
                )
            }?.takeIf {
                pos in indexes
            }?.let { emit(it) }
        }
    }.catch {
        onFailure(it)
    }

    private val String.jxl: ByteArray?
        get() = bytes?.let { JxlCoder.Convenience.construct(it) }

    private val String.jpeg: ByteArray?
        get() = bytes?.let { JxlCoder.Convenience.reconstructJPEG(it) }

    private val String.bytes: ByteArray?
        get() = context
            .contentResolver
            .openInputStream(toUri())?.use {
                it.readBytes()
            }

}