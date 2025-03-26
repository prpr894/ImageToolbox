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

package ru.tech.imageresizershrinker.feature.compare.presentation

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t8rin.dynamic.theme.LocalDynamicThemeState
import com.t8rin.dynamic.theme.extractPrimaryColor
import kotlinx.coroutines.delay
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.settings.presentation.provider.LocalSettingsState
import ru.tech.imageresizershrinker.core.ui.theme.blend
import ru.tech.imageresizershrinker.core.ui.utils.content_pickers.Picker
import ru.tech.imageresizershrinker.core.ui.utils.content_pickers.rememberImagePicker
import ru.tech.imageresizershrinker.core.ui.utils.helper.asClip
import ru.tech.imageresizershrinker.core.ui.utils.helper.isPortraitOrientationAsState
import ru.tech.imageresizershrinker.core.ui.utils.provider.LocalComponentActivity
import ru.tech.imageresizershrinker.core.ui.utils.provider.rememberLocalEssentials
import ru.tech.imageresizershrinker.core.ui.widget.dialogs.LoadingDialog
import ru.tech.imageresizershrinker.core.ui.widget.dialogs.OneTimeImagePickingDialog
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedFloatingActionButton
import ru.tech.imageresizershrinker.core.ui.widget.image.AutoFilePicker
import ru.tech.imageresizershrinker.feature.compare.presentation.components.CompareScreenContent
import ru.tech.imageresizershrinker.feature.compare.presentation.components.CompareScreenTopAppBar
import ru.tech.imageresizershrinker.feature.compare.presentation.components.CompareShareSheet
import ru.tech.imageresizershrinker.feature.compare.presentation.components.CompareType
import ru.tech.imageresizershrinker.feature.compare.presentation.screenLogic.CompareComponent


@Composable
fun CompareContent(
    component: CompareComponent
) {
    val settingsState = LocalSettingsState.current

    val context = LocalComponentActivity.current
    val themeState = LocalDynamicThemeState.current
    val allowChangeColor = settingsState.allowChangeColorByImage

    val essentials = rememberLocalEssentials()
    val showConfetti: () -> Unit = essentials::showConfetti



    LaunchedEffect(component.bitmapData) {
        component.bitmapData?.let { (b, a) ->
            if (allowChangeColor && a != null && b != null) {
                delay(100L) //delay to perform screen rotation
                themeState.updateColor(
                    a.second.extractPrimaryColor()
                        .blend(b.second.extractPrimaryColor(), 0.5f)
                )
            }
        }
    }

    val imagePicker = rememberImagePicker { uris: List<Uri> ->
        if (uris.size != 2) {
            essentials.showToast(
                message = context.getString(R.string.pick_two_images),
                icon = Icons.Rounded.ErrorOutline
            )
        } else {
            component.updateUris(
                uris = uris[0] to uris[1],
                onFailure = {
                    essentials.showToast(
                        context.getString(R.string.something_went_wrong),
                        Icons.Rounded.ErrorOutline
                    )
                }
            )
        }
    }

    val pickImage = imagePicker::pickImage

    AutoFilePicker(
        onAutoPick = pickImage,
        isPickedAlready = component.initialComparableUris != null
    )

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val isPortrait by isPortraitOrientationAsState()

    var showShareSheet by rememberSaveable { mutableStateOf(false) }

    Box {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            var isLabelsEnabled by rememberSaveable {
                mutableStateOf(true)
            }

            CompareScreenTopAppBar(
                imageNotPicked = component.bitmapData == null,
                scrollBehavior = scrollBehavior,
                onNavigationIconClick = component.onGoBack,
                onShareButtonClick = {
                    showShareSheet = true
                },
                onSwapImagesClick = component::swap,
                onRotateImagesClick = component::rotate,
                isShareButtonVisible = component.compareType == CompareType.Slide
                        || component.compareType == CompareType.PixelByPixel,
                isImagesRotated = component.rotation == 90f,
                titleWhenBitmapsPicked = stringResource(component.compareType.title),
                isLabelsEnabled = isLabelsEnabled,
                onToggleLabelsEnabled = { isLabelsEnabled = it },
                isLabelsButtonVisible = component.compareType != CompareType.PixelByPixel
            )

            CompareScreenContent(
                bitmapData = component.bitmapData,
                compareType = component.compareType,
                onCompareTypeSelected = component::setCompareType,
                isPortrait = isPortrait,
                compareProgress = component.compareProgress,
                onCompareProgressChange = component::setCompareProgress,
                imagePicker = imagePicker,
                isLabelsEnabled = isLabelsEnabled,
                pixelByPixelCompareState = component.pixelByPixelCompareState,
                onPixelByPixelCompareStateChange = component::updatePixelByPixelCompareState,
                createPixelByPixelTransformation = component::createPixelByPixelTransformation
            )
        }

        if (component.bitmapData == null) {
            var showOneTimeImagePickingDialog by rememberSaveable {
                mutableStateOf(false)
            }
            EnhancedFloatingActionButton(
                onClick = pickImage,
                onLongClick = {
                    showOneTimeImagePickingDialog = true
                },
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(16.dp)
                    .align(settingsState.fabAlignment),
                content = {
                    Spacer(Modifier.width(16.dp))
                    Icon(
                        imageVector = Icons.Rounded.AddPhotoAlternate,
                        contentDescription = stringResource(R.string.pick_image_alt)
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(stringResource(R.string.pick_image_alt))
                    Spacer(Modifier.width(16.dp))
                }
            )
            OneTimeImagePickingDialog(
                onDismiss = { showOneTimeImagePickingDialog = false },
                picker = Picker.Multiple,
                imagePicker = imagePicker,
                visible = showOneTimeImagePickingDialog
            )
        }
    }

    val previewBitmap by remember(component.bitmapData) {
        derivedStateOf {
            component.getImagePreview()
        }
    }
    val transformations = remember(
        component.bitmapData,
        component.compareProgress,
        component.pixelByPixelCompareState,
        component.compareType,
        showShareSheet
    ) {
        if (component.compareType == CompareType.PixelByPixel && showShareSheet) {
            listOf(component.createPixelByPixelTransformation())
        } else emptyList()
    }
    CompareShareSheet(
        visible = showShareSheet,
        onVisibleChange = {
            showShareSheet = it
        },
        onSaveBitmap = { imageFormat, oneTimeSaveLocationUri ->
            component.saveBitmap(
                imageFormat = imageFormat,
                oneTimeSaveLocationUri = oneTimeSaveLocationUri,
                onComplete = essentials::parseSaveResult
            )
            showShareSheet = false
        },
        onShare = { imageFormat ->
            component.shareBitmap(
                imageFormat = imageFormat,
                onComplete = showConfetti
            )
            showShareSheet = false
        },
        onCopy = { imageFormat, manager ->
            component.cacheCurrentImage(
                imageFormat = imageFormat
            ) { uri ->
                manager.copyToClipboard(uri.asClip(context))
                showConfetti()
            }
        },
        previewData = previewBitmap,
        transformations = transformations
    )

    LoadingDialog(
        visible = component.isImageLoading,
        onCancelLoading = component::cancelSaving
    )
}