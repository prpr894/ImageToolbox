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

package ru.tech.imageresizershrinker.feature.image_stacking.data

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuffXfermode
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.withContext
import ru.tech.imageresizershrinker.core.data.image.utils.drawBitmap
import ru.tech.imageresizershrinker.core.data.image.utils.toPorterDuffMode
import ru.tech.imageresizershrinker.core.data.utils.getSuitableConfig
import ru.tech.imageresizershrinker.core.domain.dispatchers.DispatchersHolder
import ru.tech.imageresizershrinker.core.domain.image.ImageGetter
import ru.tech.imageresizershrinker.core.domain.image.ImagePreviewCreator
import ru.tech.imageresizershrinker.core.domain.image.ImageScaler
import ru.tech.imageresizershrinker.core.domain.image.model.ImageFormat
import ru.tech.imageresizershrinker.core.domain.image.model.ImageInfo
import ru.tech.imageresizershrinker.core.domain.image.model.Quality
import ru.tech.imageresizershrinker.core.domain.image.model.ResizeAnchor
import ru.tech.imageresizershrinker.core.domain.image.model.ResizeType
import ru.tech.imageresizershrinker.core.domain.model.IntegerSize
import ru.tech.imageresizershrinker.feature.image_stacking.domain.ImageStacker
import ru.tech.imageresizershrinker.feature.image_stacking.domain.StackImage
import ru.tech.imageresizershrinker.feature.image_stacking.domain.StackingParams
import javax.inject.Inject

internal class AndroidImageStacker @Inject constructor(
    private val imageGetter: ImageGetter<Bitmap>,
    private val imagePreviewCreator: ImagePreviewCreator<Bitmap>,
    private val imageScaler: ImageScaler<Bitmap>,
    dispatchersHolder: DispatchersHolder
) : DispatchersHolder by dispatchersHolder, ImageStacker<Bitmap> {

    override suspend fun stackImages(
        stackImages: List<StackImage>,
        stackingParams: StackingParams,
        onFailure: (Throwable) -> Unit,
        onProgress: (Int) -> Unit
    ): Bitmap? = withContext(defaultDispatcher) {
        val resultSize = stackingParams.size
            ?: imageGetter.getImage(
                data = stackImages.firstOrNull()?.uri ?: "",
                originalSize = true
            )?.let {
                IntegerSize(it.width, it.height)
            } ?: IntegerSize(0, 0)

        if (resultSize.width <= 0 || resultSize.height <= 0) {
            onFailure(IllegalArgumentException("Width and height must be > 0"))
            return@withContext null
        }

        val outputBitmap = createBitmap(
            width = resultSize.width,
            height = resultSize.height,
            config = getSuitableConfig()
        )

        val canvas = Canvas(outputBitmap)
        val paint = Paint()

        stackImages.forEachIndexed { index, stackImage ->
            val bitmap = imageGetter.getImage(
                data = stackImage.uri
            )?.let { bitmap ->
                bitmap.setHasAlpha(true)

                val resizeType = when (stackImage.scale) {
                    StackImage.Scale.None -> null
                    StackImage.Scale.Fill -> ResizeType.Explicit
                    StackImage.Scale.Fit -> ResizeType.Flexible(ResizeAnchor.Min)
                    StackImage.Scale.FitWidth -> ResizeType.Flexible(ResizeAnchor.Width)
                    StackImage.Scale.FitHeight -> ResizeType.Flexible(ResizeAnchor.Height)
                    StackImage.Scale.Crop -> ResizeType.CenterCrop(0x00000000)
                }

                resizeType?.let {
                    imageScaler.scaleImage(
                        image = bitmap,
                        width = resultSize.width,
                        height = resultSize.height,
                        resizeType = resizeType
                    )
                } ?: bitmap
            }
            paint.alpha = (stackImage.alpha * 255).toInt()
            paint.xfermode = PorterDuffXfermode(stackImage.blendingMode.toPorterDuffMode())

            bitmap?.let {
                canvas.drawBitmap(
                    bitmap = it,
                    position = stackImage.position,
                    paint = paint
                )
            }

            onProgress(index + 1)
        }

        outputBitmap
    }

    override suspend fun stackImagesPreview(
        stackImages: List<StackImage>,
        stackingParams: StackingParams,
        imageFormat: ImageFormat,
        quality: Quality,
        onGetByteCount: (Int) -> Unit
    ): Bitmap? = withContext(defaultDispatcher) {
        stackImages(
            stackImages = stackImages,
            stackingParams = stackingParams,
            onProgress = {},
            onFailure = {}
        )?.let { image ->
            val imageSize = IntegerSize(
                width = image.width,
                height = image.height
            )
            return@let imagePreviewCreator.createPreview(
                image = image,
                imageInfo = ImageInfo(
                    width = imageSize.width,
                    height = imageSize.height,
                    imageFormat = imageFormat,
                    quality = quality
                ),
                transformations = emptyList(),
                onGetByteCount = onGetByteCount
            )
        }
    }

}