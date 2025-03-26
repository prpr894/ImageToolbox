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

package ru.tech.imageresizershrinker.noise_generation.presentation

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.tech.imageresizershrinker.core.domain.image.model.ImageInfo
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.ui.utils.animation.animate
import ru.tech.imageresizershrinker.core.ui.utils.helper.asClip
import ru.tech.imageresizershrinker.core.ui.utils.helper.isPortraitOrientationAsState
import ru.tech.imageresizershrinker.core.ui.utils.provider.LocalComponentActivity
import ru.tech.imageresizershrinker.core.ui.utils.provider.rememberLocalEssentials
import ru.tech.imageresizershrinker.core.ui.utils.state.derivedValueOf
import ru.tech.imageresizershrinker.core.ui.widget.AdaptiveLayoutScreen
import ru.tech.imageresizershrinker.core.ui.widget.buttons.BottomButtonsBlock
import ru.tech.imageresizershrinker.core.ui.widget.buttons.ShareButton
import ru.tech.imageresizershrinker.core.ui.widget.controls.ResizeImageField
import ru.tech.imageresizershrinker.core.ui.widget.controls.selection.ImageFormatSelector
import ru.tech.imageresizershrinker.core.ui.widget.controls.selection.QualitySelector
import ru.tech.imageresizershrinker.core.ui.widget.dialogs.LoadingDialog
import ru.tech.imageresizershrinker.core.ui.widget.dialogs.OneTimeSaveLocationSelectionDialog
import ru.tech.imageresizershrinker.core.ui.widget.image.Picture
import ru.tech.imageresizershrinker.core.ui.widget.modifier.container
import ru.tech.imageresizershrinker.core.ui.widget.other.LoadingIndicator
import ru.tech.imageresizershrinker.core.ui.widget.other.TopAppBarEmoji
import ru.tech.imageresizershrinker.core.ui.widget.sheets.ProcessImagesPreferenceSheet
import ru.tech.imageresizershrinker.core.ui.widget.text.marquee
import ru.tech.imageresizershrinker.noise_generation.presentation.components.NoiseParamsSelection
import ru.tech.imageresizershrinker.noise_generation.presentation.screenLogic.NoiseGenerationComponent

@Composable
fun NoiseGenerationContent(
    component: NoiseGenerationComponent
) {
    val context = LocalComponentActivity.current

    val essentials = rememberLocalEssentials()
    val showConfetti: () -> Unit = essentials::showConfetti

    val isPortrait by isPortraitOrientationAsState()

    val saveBitmap: (oneTimeSaveLocationUri: String?) -> Unit = {
        component.saveNoise(
            oneTimeSaveLocationUri = it,
            onComplete = essentials::parseSaveResult
        )
    }

    val shareButton: @Composable () -> Unit = {
        var editSheetData by remember {
            mutableStateOf(listOf<Uri>())
        }
        ShareButton(
            onShare = {
                component.shareNoise(showConfetti)
            },
            onCopy = { manager ->
                component.cacheCurrentNoise { uri ->
                    manager.copyToClipboard(uri.asClip(context))
                    showConfetti()
                }
            },
            onEdit = {
                component.cacheCurrentNoise {
                    editSheetData = listOf(it)
                }
            }
        )
        ProcessImagesPreferenceSheet(
            uris = editSheetData,
            visible = editSheetData.isNotEmpty(),
            onDismiss = {
                editSheetData = emptyList()
            },
            onNavigate = component.onNavigate
        )
    }

    AdaptiveLayoutScreen(
        shouldDisableBackHandler = true,
        title = {
            Text(
                text = stringResource(R.string.noise_generation),
                textAlign = TextAlign.Center,
                modifier = Modifier.marquee()
            )
        },
        onGoBack = component.onGoBack,
        actions = {},
        topAppBarPersistentActions = {
            TopAppBarEmoji()
        },
        imagePreview = {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Picture(
                    model = component.previewBitmap,
                    modifier = Modifier
                        .container(MaterialTheme.shapes.medium)
                        .aspectRatio(component.noiseSize.safeAspectRatio.animate()),
                    shape = MaterialTheme.shapes.medium,
                    contentScale = ContentScale.FillBounds
                )
                if (component.isImageLoading) LoadingIndicator()
            }
        },
        controls = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ResizeImageField(
                    imageInfo = derivedValueOf(component.noiseSize) {
                        ImageInfo(component.noiseSize.width, component.noiseSize.height)
                    },
                    originalSize = null,
                    onWidthChange = component::setNoiseWidth,
                    onHeightChange = component::setNoiseHeight
                )
                NoiseParamsSelection(
                    value = component.noiseParams,
                    onValueChange = component::updateParams
                )
                Spacer(Modifier.height(4.dp))
                ImageFormatSelector(
                    value = component.imageFormat,
                    onValueChange = component::setImageFormat,
                    forceEnabled = true
                )
                QualitySelector(
                    quality = component.quality,
                    imageFormat = component.imageFormat,
                    onQualityChange = component::setQuality
                )
            }
        },
        buttons = {
            var showFolderSelectionDialog by rememberSaveable {
                mutableStateOf(false)
            }
            BottomButtonsBlock(
                targetState = false to isPortrait,
                isSecondaryButtonVisible = false,
                onSecondaryButtonClick = {},
                onPrimaryButtonClick = {
                    saveBitmap(null)
                },
                onPrimaryButtonLongClick = {
                    showFolderSelectionDialog = true
                },
                actions = {
                    shareButton()
                }
            )
            OneTimeSaveLocationSelectionDialog(
                visible = showFolderSelectionDialog,
                onDismiss = { showFolderSelectionDialog = false },
                onSaveRequest = saveBitmap,
                formatForFilenameSelection = component.getFormatForFilenameSelection()
            )
        },
        canShowScreenData = true,
        isPortrait = isPortrait
    )

    LoadingDialog(
        visible = component.isSaving,
        onCancelLoading = component::cancelSaving
    )
}