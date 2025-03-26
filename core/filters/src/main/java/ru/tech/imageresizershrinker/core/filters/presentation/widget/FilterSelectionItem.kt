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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.outlined.SignalCellularConnectedNoInternet0Bar
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Slideshow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import ru.tech.imageresizershrinker.core.domain.remote.RemoteResources
import ru.tech.imageresizershrinker.core.domain.remote.RemoteResourcesDownloadProgress
import ru.tech.imageresizershrinker.core.filters.presentation.model.UiFilter
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.resources.icons.BookmarkRemove
import ru.tech.imageresizershrinker.core.ui.theme.StrongBlack
import ru.tech.imageresizershrinker.core.ui.theme.White
import ru.tech.imageresizershrinker.core.ui.theme.outlineVariant
import ru.tech.imageresizershrinker.core.ui.utils.helper.ContextUtils.isNetworkAvailable
import ru.tech.imageresizershrinker.core.ui.utils.helper.toImageModel
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedIconButton
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.hapticsClickable
import ru.tech.imageresizershrinker.core.ui.widget.modifier.shimmer
import ru.tech.imageresizershrinker.core.ui.widget.modifier.transparencyChecker
import ru.tech.imageresizershrinker.core.ui.widget.other.LocalToastHostState
import ru.tech.imageresizershrinker.core.ui.widget.other.ToastDuration
import ru.tech.imageresizershrinker.core.ui.widget.preferences.PreferenceItemOverload

@Composable
internal fun FilterSelectionItem(
    filter: UiFilter<*>,
    isFavoritePage: Boolean,
    canOpenPreview: Boolean,
    favoriteFilters: List<UiFilter<*>>,
    onLongClick: (() -> Unit)?,
    onOpenPreview: () -> Unit,
    onClick: (UiFilter<*>?) -> Unit,
    onToggleFavorite: () -> Unit,
    onRequestFilterMapping: ((UiFilter<*>) -> Transformation),
    shape: Shape,
    modifier: Modifier,
    cubeLutRemoteResources: RemoteResources? = null,
    cubeLutDownloadProgress: RemoteResourcesDownloadProgress? = null,
    onCubeLutDownloadRequest: (forceUpdate: Boolean, downloadOnlyNewData: Boolean) -> Unit = { _, _ -> },
    previewModel: ImageModel = remember { R.drawable.filter_preview_source.toImageModel() }
) {
    val toastHostState = LocalToastHostState.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val model = remember(filter, previewModel) {
        ImageRequest.Builder(context)
            .data(previewModel.data)
            .error(R.drawable.filter_preview_source)
            .transformations(onRequestFilterMapping(filter))
            .diskCacheKey(filter::class.simpleName + previewModel.data.hashCode())
            .memoryCacheKey(filter::class.simpleName + previewModel.data.hashCode())
            .size(300, 300)
            .build()
    }

    var isBitmapDark by remember {
        mutableStateOf(true)
    }
    var loading by remember {
        mutableStateOf(false)
    }
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

    var showDownloadDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var downloadOnlyNewData by rememberSaveable {
        mutableStateOf(false)
    }

    var forceUpdate by rememberSaveable {
        mutableStateOf(false)
    }

    PreferenceItemOverload(
        title = stringResource(filter.title),
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
                    if (canOpenPreview) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .hapticsClickable(onClick = onOpenPreview),
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
                onClick = onToggleFavorite,
                modifier = Modifier.offset(8.dp)
            ) {
                val inFavorite by remember(favoriteFilters, filter) {
                    derivedStateOf {
                        favoriteFilters.filterIsInstance(filter::class.java).isNotEmpty()
                    }
                }
                AnimatedContent(
                    targetState = inFavorite to isFavoritePage,
                    transitionSpec = {
                        (fadeIn() + scaleIn(initialScale = 0.85f))
                            .togetherWith(fadeOut() + scaleOut(targetScale = 0.85f))
                    }
                ) { (isInFavorite, isFavPage) ->
                    val icon by remember(isInFavorite, isFavPage) {
                        derivedStateOf {
                            when {
                                isFavPage && isInFavorite -> Icons.Rounded.BookmarkRemove
                                isInFavorite -> Icons.Rounded.Bookmark
                                else -> Icons.Rounded.BookmarkBorder
                            }
                        }
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null
                    )
                }
            }
        },
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        onLongClick = onLongClick,
        onClick = { onClick(null) },
        drawStartIconContainer = false,
        bottomContent = {
            FilterSelectionCubeLutBottomContent(
                cubeLutRemoteResources = cubeLutRemoteResources,
                shape = shape,
                onShowDownloadDialog = { forceUpdateP, downloadOnlyNewDataP ->
                    showDownloadDialog = true
                    forceUpdate = forceUpdateP
                    downloadOnlyNewData = downloadOnlyNewDataP
                },
                previewModel = previewModel,
                onRequestFilterMapping = onRequestFilterMapping,
                onClick = onClick
            )
        }
    )

    CubeLutDownloadDialog(
        visible = showDownloadDialog,
        onDismiss = { showDownloadDialog = false },
        onDownload = {
            if (context.isNetworkAvailable()) {
                onCubeLutDownloadRequest(
                    forceUpdate, downloadOnlyNewData
                )
                showDownloadDialog = false
            } else {
                scope.launch {
                    toastHostState.showToast(
                        message = context.getString(R.string.no_connection),
                        icon = Icons.Outlined.SignalCellularConnectedNoInternet0Bar,
                        duration = ToastDuration.Long
                    )
                }
            }
        },
        downloadOnlyNewData = downloadOnlyNewData,
        cubeLutDownloadProgress = cubeLutDownloadProgress
    )
}