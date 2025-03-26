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

package ru.tech.imageresizershrinker.feature.settings.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.ui.utils.provider.rememberCurrentLifecycleEvent
import ru.tech.imageresizershrinker.core.ui.widget.modifier.ContainerShapeDefaults
import ru.tech.imageresizershrinker.core.ui.widget.preferences.PreferenceItem

@Composable
fun ClearCacheSettingItem(
    onClearCache: ((String) -> Unit) -> Unit,
    value: String,
    shape: Shape = ContainerShapeDefaults.topShape,
    modifier: Modifier = Modifier.padding(horizontal = 8.dp)
) {
    val context = LocalContext.current
    val currentLifecycleEvent = rememberCurrentLifecycleEvent()
    var cache by remember(context, currentLifecycleEvent) { mutableStateOf(value) }

    PreferenceItem(
        shape = shape,
        onClick = {
            onClearCache { cache = it }
        },
        modifier = modifier,
        title = stringResource(R.string.cache_size),
        subtitle = stringResource(R.string.found_s, cache),
        endIcon = Icons.Rounded.DeleteOutline,
        startIcon = Icons.Outlined.Memory
    )
}