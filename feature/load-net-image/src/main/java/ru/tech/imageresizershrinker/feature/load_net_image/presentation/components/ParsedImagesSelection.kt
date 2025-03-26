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

package ru.tech.imageresizershrinker.feature.load_net_image.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import ru.tech.imageresizershrinker.core.ui.widget.image.ImagesPreviewWithSelection
import ru.tech.imageresizershrinker.feature.load_net_image.presentation.screenLogic.LoadNetImageComponent

@Composable
internal fun ParsedImagesSelection(
    component: LoadNetImageComponent
) {
    AnimatedVisibility(component.parsedImages.size > 1) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth()
        ) {
            val screenWidth = this.maxWidth

            ImagesPreviewWithSelection(
                imageUris = component.parsedImages,
                imageFrames = component.imageFrames,
                onFrameSelectionChange = component::updateImageFrames,
                isPortrait = true,
                isLoadingImages = component.isImageLoading,
                contentScale = ContentScale.Fit,
                contentPadding = PaddingValues(20.dp),
                modifier = Modifier
                    .height(
                        (130.dp * component.parsedImages.size).coerceAtMost(420.dp)
                    )
                    .layout { measurable, constraints ->
                        val result =
                            measurable.measure(
                                constraints.copy(
                                    maxWidth = (screenWidth + 40.dp).roundToPx()
                                )
                            )
                        layout(result.measuredWidth, result.measuredHeight) {
                            result.place(0, 0)
                        }
                    },
                showExtension = false
            )
        }
    }
}