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

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.rounded.FilePresent
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.transformations
import coil3.transform.Transformation
import dev.shreyaspatil.capturable.capturable
import dev.shreyaspatil.capturable.controller.rememberCaptureController
import kotlinx.coroutines.launch
import ru.tech.imageresizershrinker.core.domain.model.ImageModel
import ru.tech.imageresizershrinker.core.filters.domain.model.TemplateFilter
import ru.tech.imageresizershrinker.core.filters.presentation.model.UiFilter
import ru.tech.imageresizershrinker.core.filters.presentation.model.toUiFilter
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.resources.icons.EditAlt
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedAlertDialog
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedButton
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedIconButton
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedModalBottomSheet
import ru.tech.imageresizershrinker.core.ui.widget.modifier.ContainerShapeDefaults
import ru.tech.imageresizershrinker.core.ui.widget.modifier.shimmer
import ru.tech.imageresizershrinker.core.ui.widget.modifier.transparencyChecker
import ru.tech.imageresizershrinker.core.ui.widget.other.QrCode
import ru.tech.imageresizershrinker.core.ui.widget.preferences.PreferenceItem
import ru.tech.imageresizershrinker.core.ui.widget.preferences.PreferenceItemDefaults
import ru.tech.imageresizershrinker.core.ui.widget.text.TitleItem

@Composable
internal fun FilterTemplateInfoSheet(
    component: FilterTemplateCreationSheetComponent,
    visible: Boolean,
    onDismiss: (Boolean) -> Unit,
    templateFilter: TemplateFilter,
    onShareImage: (Bitmap) -> Unit,
    onSaveImage: (Bitmap) -> Unit,
    onSaveFile: (fileUri: Uri, content: String) -> Unit,
    onConvertTemplateFilterToString: suspend (TemplateFilter) -> String,
    onRemoveTemplateFilter: (TemplateFilter) -> Unit,
    onShareFile: (content: String) -> Unit,
    onRequestTemplateFilename: () -> String,
    onRequestFilterMapping: (UiFilter<*>) -> Transformation,
    previewModel: ImageModel
) {
    EnhancedModalBottomSheet(
        visible = visible,
        onDismiss = onDismiss,
        confirmButton = {
            EnhancedButton(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                onClick = {
                    onDismiss(false)
                }
            ) {
                Text(stringResource(R.string.close))
            }
        },
        title = {
            TitleItem(
                text = stringResource(id = R.string.template_filter),
                icon = Icons.Outlined.Extension
            )
        }
    ) {
        var filterContent by rememberSaveable {
            mutableStateOf("")
        }
        LaunchedEffect(filterContent) {
            if (filterContent.isEmpty()) {
                filterContent = onConvertTemplateFilterToString(templateFilter)
            }
        }

        var showShareDialog by rememberSaveable {
            mutableStateOf(false)
        }

        var showDeleteDialog by rememberSaveable {
            mutableStateOf(false)
        }

        var showEditTemplateSheet by rememberSaveable {
            mutableStateOf(false)
        }

        val scope = rememberCoroutineScope()
        val captureController = rememberCaptureController()

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Column(Modifier.capturable(captureController)) {
                    Spacer(modifier = Modifier.height(32.dp))
                    BoxWithConstraints(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(16.dp)
                    ) {
                        val targetSize = min(min(this.maxWidth, maxHeight), 300.dp)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            QrCode(
                                content = filterContent,
                                modifier = Modifier
                                    .padding(top = 36.dp, bottom = 16.dp)
                                    .size(targetSize)
                            )

                            Text(
                                text = templateFilter.name,
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(targetSize)
                            )
                        }

                        TemplateFilterPreviewItem(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = (-48).dp)
                                .size(64.dp),
                            templateFilter = templateFilter,
                            onRequestFilterMapping = onRequestFilterMapping,
                            previewModel = previewModel
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    EnhancedIconButton(
                        onClick = { showDeleteDialog = true },
                        containerColor = MaterialTheme.colorScheme.error
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = stringResource(R.string.delete)
                        )
                    }
                    EnhancedButton(
                        onClick = { showShareDialog = true },
                        modifier = Modifier.fillMaxWidth(0.75f)
                    ) {
                        Text(stringResource(R.string.share))
                    }
                    EnhancedIconButton(
                        onClick = { showEditTemplateSheet = true },
                        containerColor = MaterialTheme.colorScheme.secondary
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.EditAlt,
                            contentDescription = stringResource(R.string.edit)
                        )
                    }
                }
            }
        }

        EnhancedAlertDialog(
            visible = showDeleteDialog,
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                EnhancedButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
            dismissButton = {
                EnhancedButton(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    onClick = {
                        onRemoveTemplateFilter(templateFilter)
                        onDismiss(false)
                        showDeleteDialog = false
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            title = {
                Text(stringResource(R.string.delete_template))
            },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.delete)
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    TemplateFilterPreviewItem(
                        modifier = Modifier
                            .sizeIn(
                                maxHeight = 80.dp,
                                maxWidth = 80.dp
                            )
                            .aspectRatio(1f),
                        templateFilter = templateFilter,
                        onRequestFilterMapping = onRequestFilterMapping,
                        previewModel = previewModel
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(R.string.delete_template_warn))
                }
            }
        )

        val saveLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("*/*"),
            onResult = {
                it?.let { uri ->
                    showShareDialog = false
                    onSaveFile(uri, filterContent)
                }
            }
        )

        EnhancedAlertDialog(
            visible = showShareDialog,
            onDismissRequest = { showShareDialog = false },
            confirmButton = {
                EnhancedButton(
                    onClick = { showShareDialog = false },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
            title = {
                Text(stringResource(R.string.share))
            },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.AutoFixHigh,
                    contentDescription = null
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    PreferenceItem(
                        title = stringResource(R.string.as_qr_code),
                        shape = ContainerShapeDefaults.topShape,
                        startIcon = Icons.Rounded.QrCode,
                        onClick = {
                            showShareDialog = false
                            scope.launch {
                                captureController.captureAsync()
                                    .await()
                                    .asAndroidBitmap()
                                    .let(onShareImage)
                            }
                        },
                        titleFontStyle = PreferenceItemDefaults.TitleFontStyleCentered
                    )
                    Spacer(Modifier.height(4.dp))
                    PreferenceItem(
                        title = stringResource(R.string.as_file),
                        shape = ContainerShapeDefaults.centerShape,
                        startIcon = Icons.Rounded.FilePresent,
                        onClick = {
                            showShareDialog = false
                            onShareFile(filterContent)
                        },
                        titleFontStyle = PreferenceItemDefaults.TitleFontStyleCentered
                    )
                    Spacer(Modifier.height(4.dp))
                    PreferenceItem(
                        title = stringResource(R.string.save_as_qr_code_image),
                        shape = ContainerShapeDefaults.centerShape,
                        startIcon = Icons.Rounded.QrCode2,
                        onClick = {
                            showShareDialog = false
                            scope.launch {
                                captureController.captureAsync()
                                    .await()
                                    .asAndroidBitmap()
                                    .let(onSaveImage)
                            }
                        },
                        titleFontStyle = PreferenceItemDefaults.TitleFontStyleCentered
                    )
                    Spacer(Modifier.height(4.dp))
                    PreferenceItem(
                        title = stringResource(R.string.save_as_file),
                        shape = ContainerShapeDefaults.bottomShape,
                        startIcon = Icons.Rounded.Save,
                        onClick = {
                            saveLauncher.launch(onRequestTemplateFilename())
                        },
                        titleFontStyle = PreferenceItemDefaults.TitleFontStyleCentered
                    )
                }
            }
        )

        FilterTemplateCreationSheet(
            visible = showEditTemplateSheet,
            onDismiss = { showEditTemplateSheet = false },
            initialTemplateFilter = templateFilter,
            component = component
        )
    }
}

@Composable
internal fun TemplateFilterPreviewItem(
    modifier: Modifier,
    onRequestFilterMapping: (UiFilter<*>) -> Transformation,
    templateFilter: TemplateFilter,
    previewModel: ImageModel
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
    val painter = rememberAsyncImagePainter(
        model = model,
        onLoading = {
            loading = true
        },
        onSuccess = {
            loading = false
        }
    )

    Image(
        painter = painter,
        contentScale = ContentScale.Crop,
        contentDescription = null,
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .transparencyChecker()
            .shimmer(loading)
    )
}