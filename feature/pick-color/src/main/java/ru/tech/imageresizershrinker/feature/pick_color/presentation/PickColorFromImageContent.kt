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

package ru.tech.imageresizershrinker.feature.pick_color.presentation

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ZoomIn
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.settings.presentation.provider.LocalSettingsState
import ru.tech.imageresizershrinker.core.settings.presentation.provider.LocalSimpleSettingsInteractor
import ru.tech.imageresizershrinker.core.ui.theme.takeColorFromScheme
import ru.tech.imageresizershrinker.core.ui.utils.content_pickers.Picker
import ru.tech.imageresizershrinker.core.ui.utils.content_pickers.rememberImagePicker
import ru.tech.imageresizershrinker.core.ui.utils.helper.isPortraitOrientationAsState
import ru.tech.imageresizershrinker.core.ui.utils.provider.rememberLocalEssentials
import ru.tech.imageresizershrinker.core.ui.widget.buttons.PanModeButton
import ru.tech.imageresizershrinker.core.ui.widget.dialogs.LoadingDialog
import ru.tech.imageresizershrinker.core.ui.widget.dialogs.OneTimeImagePickingDialog
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedFloatingActionButton
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedIconButton
import ru.tech.imageresizershrinker.core.ui.widget.image.AutoFilePicker
import ru.tech.imageresizershrinker.core.ui.widget.utils.AutoContentBasedColors
import ru.tech.imageresizershrinker.feature.pick_color.presentation.components.PickColorFromImageBottomAppBar
import ru.tech.imageresizershrinker.feature.pick_color.presentation.components.PickColorFromImageContentImpl
import ru.tech.imageresizershrinker.feature.pick_color.presentation.components.PickColorFromImageTopAppBar
import ru.tech.imageresizershrinker.feature.pick_color.presentation.screenLogic.PickColorFromImageComponent

@Composable
fun PickColorFromImageContent(
    component: PickColorFromImageComponent
) {
    val settingsState = LocalSettingsState.current

    val essentials = rememberLocalEssentials()
    val scope = essentials.coroutineScope

    var panEnabled by rememberSaveable { mutableStateOf(false) }

    AutoContentBasedColors(component.bitmap)
    AutoContentBasedColors(component.color)

    val imagePicker = rememberImagePicker { uri: Uri ->
        component.setUri(
            uri = uri,
            onFailure = essentials::showFailureToast
        )
    }

    val pickImage = imagePicker::pickImage

    AutoFilePicker(
        onAutoPick = pickImage,
        isPickedAlready = component.initialUri != null
    )

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val isPortrait by isPortraitOrientationAsState()

    val switch = @Composable {
        PanModeButton(
            selected = panEnabled,
            onClick = { panEnabled = !panEnabled }
        )
    }

    val magnifierButton = @Composable {
        val settingsInteractor = LocalSimpleSettingsInteractor.current
        EnhancedIconButton(
            containerColor = takeColorFromScheme {
                if (settingsState.magnifierEnabled) {
                    secondary
                } else surfaceContainer
            },
            contentColor = takeColorFromScheme {
                if (settingsState.magnifierEnabled) {
                    onSecondary
                } else onSurface
            },
            enableAutoShadowAndBorder = false,
            onClick = {
                scope.launch {
                    settingsInteractor.toggleMagnifierEnabled()
                }
            },
            modifier = Modifier.statusBarsPadding()
        ) {
            Icon(
                imageVector = Icons.Outlined.ZoomIn,
                contentDescription = stringResource(R.string.magnifier)
            )
        }
    }

    var showOneTimeImagePickingDialog by rememberSaveable {
        mutableStateOf(false)
    }

    Box {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            PickColorFromImageTopAppBar(
                bitmap = component.bitmap,
                scrollBehavior = scrollBehavior,
                onGoBack = component.onGoBack,
                isPortrait = isPortrait,
                magnifierButton = magnifierButton,
                color = component.color
            )
            PickColorFromImageContentImpl(
                bitmap = component.bitmap,
                isPortrait = isPortrait,
                panEnabled = panEnabled,
                onColorChange = component::updateColor,
                onPickImage = pickImage,
                onOneTimePickImage = { showOneTimeImagePickingDialog = true },
                magnifierButton = magnifierButton,
                switch = switch,
                color = component.color
            )
            PickColorFromImageBottomAppBar(
                bitmap = component.bitmap,
                isPortrait = isPortrait,
                switch = switch,
                color = component.color,
                onPickImage = pickImage,
                onOneTimePickImage = { showOneTimeImagePickingDialog = true },
            )
        }

        if (component.bitmap == null) {
            EnhancedFloatingActionButton(
                onClick = pickImage,
                onLongClick = {
                    showOneTimeImagePickingDialog = true
                },
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(16.dp)
                    .align(settingsState.fabAlignment)
            ) {
                Spacer(Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Rounded.AddPhotoAlternate,
                    contentDescription = stringResource(R.string.pick_image_alt)
                )
                Spacer(Modifier.width(16.dp))
                Text(stringResource(R.string.pick_image_alt))
                Spacer(Modifier.width(16.dp))
            }
        }
    }

    OneTimeImagePickingDialog(
        onDismiss = { showOneTimeImagePickingDialog = false },
        picker = Picker.Single,
        imagePicker = imagePicker,
        visible = showOneTimeImagePickingDialog
    )

    LoadingDialog(
        visible = component.isImageLoading,
        canCancel = false
    )
}