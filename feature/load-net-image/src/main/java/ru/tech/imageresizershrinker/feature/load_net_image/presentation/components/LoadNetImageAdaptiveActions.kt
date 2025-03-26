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

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import ru.tech.imageresizershrinker.core.ui.utils.helper.asClip
import ru.tech.imageresizershrinker.core.ui.utils.provider.rememberLocalEssentials
import ru.tech.imageresizershrinker.core.ui.widget.buttons.ShareButton
import ru.tech.imageresizershrinker.core.ui.widget.buttons.ZoomButton
import ru.tech.imageresizershrinker.core.ui.widget.sheets.ZoomModalSheet
import ru.tech.imageresizershrinker.feature.load_net_image.presentation.screenLogic.LoadNetImageComponent

@Composable
internal fun RowScope.LoadNetImageAdaptiveActions(
    component: LoadNetImageComponent
) {
    val context = LocalContext.current
    val essentials = rememberLocalEssentials()

    ShareButton(
        enabled = component.parsedImages.isNotEmpty(),
        onShare = {
            component.performSharing(essentials::showConfetti)
        },
        onCopy = { manager ->
            component.cacheCurrentImage { uri ->
                manager.copyToClipboard(uri.asClip(context))
                essentials.showConfetti()
            }
        }
    )
    var showZoomSheet by rememberSaveable { mutableStateOf(false) }
    ZoomButton(
        onClick = { showZoomSheet = true },
        visible = component.bitmap != null,
    )
    ZoomModalSheet(
        data = component.bitmap,
        visible = showZoomSheet,
        onDismiss = {
            showZoomSheet = false
        }
    )
}