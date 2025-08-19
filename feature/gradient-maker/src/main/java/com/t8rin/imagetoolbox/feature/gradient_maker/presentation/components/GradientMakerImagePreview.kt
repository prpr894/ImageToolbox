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

package com.t8rin.imagetoolbox.feature.gradient_maker.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.modifier.container
import com.t8rin.imagetoolbox.core.ui.widget.modifier.detectSwipes
import com.t8rin.imagetoolbox.feature.gradient_maker.presentation.components.model.canPickImage
import com.t8rin.imagetoolbox.feature.gradient_maker.presentation.components.model.isMesh
import com.t8rin.imagetoolbox.feature.gradient_maker.presentation.screenLogic.GradientMakerComponent

@Composable
internal fun GradientMakerImagePreview(component: GradientMakerComponent) {
    val screenType = component.screenType

    Box(
        modifier = Modifier
            .detectSwipes(
                onSwipeRight = component::selectLeftUri,
                onSwipeLeft = component::selectRightUri
            )
            .container()
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (screenType.isMesh()) {
            MeshGradientPreview(
                meshGradientState = component.meshGradientState,
                gradientAlpha = if (component.showOriginal) 0f else component.gradientAlpha,
                allowPickingImage = screenType.canPickImage(),
                gradientSize = component.gradientSize,
                selectedUri = component.selectedUri,
                imageAspectRatio = component.imageAspectRatio
            )
        } else {
            GradientPreview(
                brush = component.brush,
                gradientAlpha = if (component.showOriginal) 0f else component.gradientAlpha,
                allowPickingImage = screenType.canPickImage(),
                gradientSize = component.gradientSize,
                onSizeChanged = component::setPreviewSize,
                selectedUri = component.selectedUri,
                imageAspectRatio = component.imageAspectRatio
            )
        }
    }
}