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

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.ImageOverlay
import com.t8rin.imagetoolbox.core.resources.icons.MeshDownload
import com.t8rin.imagetoolbox.core.resources.icons.MeshGradient
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberImagePicker
import com.t8rin.imagetoolbox.core.ui.utils.helper.isPortraitOrientationAsState
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.widget.modifier.withModifier
import com.t8rin.imagetoolbox.core.ui.widget.preferences.PreferenceItem
import com.t8rin.imagetoolbox.feature.gradient_maker.presentation.components.model.GradientMakerType
import com.t8rin.imagetoolbox.feature.gradient_maker.presentation.screenLogic.GradientMakerComponent

@Composable
internal fun GradientMakerNoDataControls(
    component: GradientMakerComponent
) {
    val isPortrait by isPortraitOrientationAsState()
    var requestedType by rememberSaveable(component.screenType) {
        mutableStateOf<GradientMakerType?>(null)
    }

    val imagePicker = rememberImagePicker { uris: List<Uri> ->
        component.setScreenType(requestedType)
        component.setUris(uris)
        component.updateGradientAlpha(0.5f)
    }

    val preference1 = @Composable {
        val screen = remember {
            Screen.GradientMaker()
        }
        PreferenceItem(
            title = stringResource(screen.title),
            subtitle = stringResource(screen.subtitle),
            startIcon = screen.icon,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                component.setScreenType(GradientMakerType.Default)
            }
        )
    }
    val preference2 = @Composable {
        PreferenceItem(
            title = stringResource(R.string.gradient_maker_type_image),
            subtitle = stringResource(R.string.gradient_maker_type_image_sub),
            startIcon = Icons.Outlined.Collections,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                requestedType = GradientMakerType.Overlay
                imagePicker.pickImage()
            }
        )
    }
    val preference3 = @Composable {
        PreferenceItem(
            title = stringResource(R.string.mesh_gradients),
            subtitle = stringResource(R.string.mesh_gradients_sub),
            startIcon = Icons.Outlined.MeshGradient,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                component.setScreenType(GradientMakerType.Mesh)
            }
        )
    }
    val preference4 = @Composable {
        PreferenceItem(
            title = stringResource(R.string.gradient_maker_type_image_mesh),
            subtitle = stringResource(R.string.gradient_maker_type_image_mesh_sub),
            startIcon = Icons.Outlined.ImageOverlay,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                requestedType = GradientMakerType.MeshOverlay
                imagePicker.pickImage()
            }
        )
    }
    val preference5 = @Composable {
        PreferenceItem(
            title = stringResource(R.string.collection_mesh_gradients),
            subtitle = stringResource(R.string.collection_mesh_gradients_sub),
            startIcon = Icons.Outlined.MeshDownload,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                component.onNavigate(Screen.MeshGradients)
            }
        )
    }
    if (isPortrait) {
        Column {
            preference1()
            Spacer(modifier = Modifier.height(8.dp))
            preference2()
            Spacer(modifier = Modifier.height(8.dp))
            preference3()
            Spacer(modifier = Modifier.height(8.dp))
            preference4()
            Spacer(modifier = Modifier.height(8.dp))
            preference5()
        }
    } else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.windowInsetsPadding(
                    WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal)
                )
            ) {
                preference1.withModifier(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                preference2.withModifier(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.windowInsetsPadding(
                    WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal)
                )
            ) {
                preference3.withModifier(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                preference4.withModifier(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            preference5.withModifier(modifier = Modifier.fillMaxWidth(0.5f))
        }
    }
}