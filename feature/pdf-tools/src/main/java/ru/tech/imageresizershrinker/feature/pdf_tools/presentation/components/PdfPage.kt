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

package ru.tech.imageresizershrinker.feature.pdf_tools.presentation.components

import android.graphics.pdf.PdfRenderer
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import coil3.Image
import coil3.asImage
import coil3.imageLoader
import coil3.memory.MemoryCache
import coil3.request.ImageRequest
import coil3.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.tech.imageresizershrinker.core.domain.model.IntegerSize
import ru.tech.imageresizershrinker.core.domain.model.flexibleResize
import ru.tech.imageresizershrinker.core.ui.widget.image.Picture

@Composable
internal fun PdfPage(
    selected: Boolean,
    selectionEnabled: Boolean,
    contentScale: ContentScale = ContentScale.Crop,
    modifier: Modifier,
    index: Int,
    renderWidth: Int,
    renderHeight: Int,
    zoom: Float = 1f,
    mutex: Mutex,
    renderer: PdfRenderer?,
    cacheKey: MemoryCache.Key,
) {
    val context = LocalContext.current
    val imageLoadingScope = rememberCoroutineScope()

    val cacheValue: Image? = context.imageLoader.memoryCache?.get(cacheKey)?.image

    var bitmap: Image? by remember { mutableStateOf(cacheValue) }
    if (bitmap == null) {
        DisposableEffect(cacheKey, index) {
            val job = imageLoadingScope.launch(Dispatchers.IO) {
                mutex.withLock {
                    if (!coroutineContext.isActive) return@launch
                    try {
                        renderer?.let {
                            it.openPage(index).use { page ->
                                val size = IntegerSize(
                                    width = page.width,
                                    height = page.height
                                ).flexibleResize(renderWidth, renderHeight)
                                val destinationBitmap = createBitmap(
                                    width = size.width,
                                    height = size.height
                                )
                                page.render(
                                    destinationBitmap,
                                    null,
                                    null,
                                    PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                                )
                                bitmap = destinationBitmap.asImage()
                            }
                        }
                    } catch (_: Throwable) {
                        //Just catch and return in case the renderer is being closed
                        return@launch
                    }
                }
            }
            onDispose {
                job.cancel()
            }
        }
    }

    val request = remember(context, renderWidth, renderHeight, bitmap) {
        ImageRequest.Builder(context)
            .size(renderWidth, renderHeight)
            .memoryCacheKey(cacheKey)
            .data(bitmap?.toBitmap())
            .build()
    }

    val transition = updateTransition(selected)
    val padding by transition.animateDp { s ->
        if (s) 10.dp else 0.dp
    }
    val corners by transition.animateDp { s ->
        if (s) 16.dp else 0.dp
    }
    val bgColor = MaterialTheme.colorScheme.secondaryContainer

    val density = LocalDensity.current
    Box(
        modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
    ) {
        Picture(
            modifier = Modifier
                .then(
                    if (contentScale == ContentScale.Crop) Modifier.matchParentSize()
                    else Modifier
                )
                .width(with(density) { renderWidth.toDp() * zoom })
                .aspectRatio(renderWidth / renderHeight.toFloat())
                .padding(padding)
                .clip(RoundedCornerShape(corners))
                .background(Color.White),
            shape = RectangleShape,
            contentScale = contentScale,
            showTransparencyChecker = false,
            model = request
        )
        AnimatedVisibility(
            visible = selectionEnabled,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .clip(RoundedCornerShape(corners))
                    .background(MaterialTheme.colorScheme.scrim.copy(0.32f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (index + 1).toString(),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            AnimatedContent(
                targetState = selected,
                transitionSpec = {
                    fadeIn() + scaleIn() togetherWith fadeOut() + scaleOut()
                }
            ) { selected ->
                if (selected) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(4.dp)
                            .border(2.dp, bgColor, CircleShape)
                            .clip(CircleShape)
                            .background(bgColor)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.RadioButtonUnchecked,
                        tint = Color.White.copy(alpha = 0.7f),
                        contentDescription = null,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }
        }
    }

}