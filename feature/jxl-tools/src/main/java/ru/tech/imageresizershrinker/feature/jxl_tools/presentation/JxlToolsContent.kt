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

package ru.tech.imageresizershrinker.feature.jxl_tools.presentation

import android.net.Uri
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.resources.icons.Jxl
import ru.tech.imageresizershrinker.core.ui.utils.content_pickers.rememberFilePicker
import ru.tech.imageresizershrinker.core.ui.utils.content_pickers.rememberImagePicker
import ru.tech.imageresizershrinker.core.ui.utils.helper.isJxl
import ru.tech.imageresizershrinker.core.ui.utils.navigation.Screen
import ru.tech.imageresizershrinker.core.ui.utils.provider.LocalComponentActivity
import ru.tech.imageresizershrinker.core.ui.utils.provider.rememberLocalEssentials
import ru.tech.imageresizershrinker.core.ui.widget.AdaptiveLayoutScreen
import ru.tech.imageresizershrinker.core.ui.widget.buttons.ShareButton
import ru.tech.imageresizershrinker.core.ui.widget.dialogs.ExitWithoutSavingDialog
import ru.tech.imageresizershrinker.core.ui.widget.dialogs.LoadingDialog
import ru.tech.imageresizershrinker.core.ui.widget.text.TopAppBarTitle
import ru.tech.imageresizershrinker.feature.jxl_tools.presentation.components.JxlToolsBitmapPreview
import ru.tech.imageresizershrinker.feature.jxl_tools.presentation.components.JxlToolsButtons
import ru.tech.imageresizershrinker.feature.jxl_tools.presentation.components.JxlToolsControls
import ru.tech.imageresizershrinker.feature.jxl_tools.presentation.components.JxlToolsNoDataControls
import ru.tech.imageresizershrinker.feature.jxl_tools.presentation.components.JxlToolsTopAppBarActions
import ru.tech.imageresizershrinker.feature.jxl_tools.presentation.screenLogic.JxlToolsComponent

@Composable
fun JxlToolsContent(
    component: JxlToolsComponent
) {
    val context = LocalComponentActivity.current

    val essentials = rememberLocalEssentials()
    val showConfetti: () -> Unit = essentials::showConfetti

    val onFailure: (Throwable) -> Unit = essentials::showFailureToast

    val pickJpegsLauncher = rememberFilePicker(
        mimeTypes = listOf("image/jpeg", "image/jpg")
    ) { list: List<Uri> ->
        list.let { uris ->
            component.setType(
                type = Screen.JxlTools.Type.JpegToJxl(uris),
                onFailure = onFailure
            )
        }
    }

    val pickJxlsLauncher = rememberFilePicker { list: List<Uri> ->
        list.filter {
            it.isJxl(context)
        }.let { uris ->
            if (uris.isEmpty()) {
                essentials.showToast(
                    message = context.getString(R.string.select_jxl_image_to_start),
                    icon = Icons.Filled.Jxl
                )
            } else {
                component.setType(
                    type = Screen.JxlTools.Type.JxlToJpeg(uris),
                    onFailure = onFailure
                )
            }
        }
    }

    val pickSingleJxlLauncher = rememberFilePicker { uri: Uri ->
        if (uri.isJxl(context)) {
            component.setType(
                type = Screen.JxlTools.Type.JxlToImage(uri),
                onFailure = onFailure
            )
        } else {
            essentials.showToast(
                message = context.getString(R.string.select_jxl_image_to_start),
                icon = Icons.Filled.Jxl
            )
        }
    }

    val imagePicker = rememberImagePicker { uris: List<Uri> ->
        component.setType(
            type = Screen.JxlTools.Type.ImageToJxl(uris),
            onFailure = onFailure
        )
    }

    val addImagesImagePicker = rememberImagePicker { uris: List<Uri> ->
        component.setType(
            type = Screen.JxlTools.Type.ImageToJxl(
                (component.type as? Screen.JxlTools.Type.ImageToJxl)?.imageUris?.plus(uris)
                    ?.distinct()
            ),
            onFailure = onFailure
        )
    }

    val addJpegsLauncher = rememberFilePicker(
        mimeTypes = listOf("image/jpeg", "image/jpg")
    ) { list: List<Uri> ->
        component.setType(
            type = (component.type as? Screen.JxlTools.Type.JpegToJxl)?.let {
                it.copy(it.jpegImageUris?.plus(list)?.distinct())
            },
            onFailure = onFailure
        )
    }

    val addJxlsLauncher = rememberFilePicker { list: List<Uri> ->
        list.filter {
            it.isJxl(context)
        }.let { uris ->
            if (uris.isEmpty()) {
                essentials.showToast(
                    message = context.getString(R.string.select_jxl_image_to_start),
                    icon = Icons.Filled.Jxl
                )
            } else {
                component.setType(
                    type = (component.type as? Screen.JxlTools.Type.JxlToJpeg)?.let {
                        it.copy(it.jxlImageUris?.plus(uris)?.distinct())
                    },
                    onFailure = onFailure
                )
            }
        }
    }

    fun pickImage(type: Screen.JxlTools.Type? = null) {
        when (type ?: component.type) {
            is Screen.JxlTools.Type.ImageToJxl -> imagePicker.pickImage()
            is Screen.JxlTools.Type.JpegToJxl -> pickJpegsLauncher.pickFile()
            is Screen.JxlTools.Type.JxlToImage -> pickSingleJxlLauncher.pickFile()
            else -> pickJxlsLauncher.pickFile()
        }
    }

    val addImages: () -> Unit = {
        when (component.type) {
            is Screen.JxlTools.Type.ImageToJxl -> addImagesImagePicker.pickImage()
            is Screen.JxlTools.Type.JpegToJxl -> addJpegsLauncher.pickFile()
            else -> addJxlsLauncher.pickFile()
        }
    }

    var showExitDialog by rememberSaveable { mutableStateOf(false) }

    val onBack = {
        if (component.haveChanges) showExitDialog = true
        else component.onGoBack()
    }

    AdaptiveLayoutScreen(
        shouldDisableBackHandler = !component.haveChanges,
        title = {
            TopAppBarTitle(
                title = when (val type = component.type) {
                    null -> stringResource(R.string.jxl_tools)
                    else -> stringResource(type.title)
                },
                input = component.type,
                isLoading = component.isLoading,
                size = null
            )
        },
        onGoBack = onBack,
        topAppBarPersistentActions = {
            JxlToolsTopAppBarActions(component = component)
        },
        actions = {
            if (component.type is Screen.JxlTools.Type.JxlToImage) {
                ShareButton(
                    enabled = !component.isLoading && component.type != null,
                    onShare = {
                        component.performSharing(
                            onFailure = onFailure,
                            onComplete = showConfetti
                        )
                    }
                )
            }
        },
        imagePreview = {
            JxlToolsBitmapPreview(
                component = component,
                onAddImages = addImages
            )
        },
        placeImagePreview = component.type is Screen.JxlTools.Type.JxlToImage
                || component.type is Screen.JxlTools.Type.ImageToJxl,
        showImagePreviewAsStickyHeader = false,
        autoClearFocus = false,
        controls = {
            JxlToolsControls(
                component = component,
                onAddImages = addImages
            )
        },
        contentPadding = animateDpAsState(
            if (component.type == null) 12.dp
            else 20.dp
        ).value,
        buttons = { actions ->
            JxlToolsButtons(
                component = component,
                actions = actions,
                onPickImage = ::pickImage,
                imagePicker = imagePicker
            )
        },
        insetsForNoData = WindowInsets(0),
        noDataControls = {
            JxlToolsNoDataControls(::pickImage)
        },
        canShowScreenData = component.type != null
    )

    LoadingDialog(
        visible = component.isSaving,
        done = component.done,
        left = component.left,
        onCancelLoading = component::cancelSaving
    )

    ExitWithoutSavingDialog(
        onExit = component::clearAll,
        onDismiss = { showExitDialog = false },
        visible = showExitDialog
    )
}