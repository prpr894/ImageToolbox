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

package ru.tech.imageresizershrinker.core.filters.presentation.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.Slideshow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.transformations
import coil3.toBitmap
import coil3.transform.Transformation
import kotlinx.coroutines.launch
import ru.tech.imageresizershrinker.core.domain.model.ImageModel
import ru.tech.imageresizershrinker.core.filters.domain.model.TemplateFilter
import ru.tech.imageresizershrinker.core.filters.presentation.model.UiFilter
import ru.tech.imageresizershrinker.core.filters.presentation.model.toUiFilter
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.ui.theme.StrongBlack
import ru.tech.imageresizershrinker.core.ui.theme.White
import ru.tech.imageresizershrinker.core.ui.theme.outlineVariant
import ru.tech.imageresizershrinker.core.ui.utils.helper.toImageModel
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedIconButton
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.hapticsClickable
import ru.tech.imageresizershrinker.core.ui.widget.modifier.shimmer
import ru.tech.imageresizershrinker.core.ui.widget.modifier.transparencyChecker
import ru.tech.imageresizershrinker.core.ui.widget.preferences.PreferenceItemOverload

@Composable
internal fun TemplateFilterSelectionItem(
    templateFilter: TemplateFilter,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRequestFilterMapping: (UiFilter<*>) -> Transformation,
    onInfoClick: () -> Unit,
    shape: Shape,
    modifier: Modifier,
    previewModel: ImageModel = remember { R.drawable.filter_preview_source.toImageModel() }
) {
    val context = LocalContext.current
    val model = remember(templateFilter, previewModel) {
        ImageRequest.Builder(context)
            .data(previewModel.data)
            .error(R.drawable.filter_preview_source)
            .transformations(templateFilter.filters.map { onRequestFilterMapping(it.toUiFilter()) })
            .diskCacheKey(templateFilter.toString() + previewModel.data.hashCode())
            .memoryCacheKey(templateFilter.toString() + previewModel.data.hashCode())
            .size(300, 300)
            .build()
    }
    var loading by remember {
        mutableStateOf(false)
    }
    var isBitmapDark by remember {
        mutableStateOf(true)
    }
    val scope = rememberCoroutineScope()

    val painter = rememberAsyncImagePainter(
        model = model,
        onLoading = {
            loading = true
        },
        onSuccess = {
            loading = false
            scope.launch {
                isBitmapDark = calculateBrightnessEstimate(it.result.image.toBitmap()) < 110
            }
        }
    )

    PreferenceItemOverload(
        title = templateFilter.name,
        startIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painter,
                        contentScale = ContentScale.Crop,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .scale(1.2f)
                            .clip(MaterialTheme.shapes.medium)
                            .transparencyChecker()
                            .shimmer(loading)
                    )
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .hapticsClickable(onClick = onLongClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Slideshow,
                            contentDescription = stringResource(R.string.image_preview),
                            tint = if (isBitmapDark) StrongBlack
                            else White,
                            modifier = Modifier.scale(1.2f)
                        )
                        Icon(
                            imageVector = Icons.Rounded.Slideshow,
                            contentDescription = stringResource(R.string.image_preview),
                            tint = if (isBitmapDark) White
                            else StrongBlack
                        )
                    }
                }
                Spacer(Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant())
                )
            }
        },
        endIcon = {
            EnhancedIconButton(
                onClick = onInfoClick,
                modifier = Modifier.offset(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null
                )
            }
        },
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        onClick = onClick,
        drawStartIconContainer = false
    )
}