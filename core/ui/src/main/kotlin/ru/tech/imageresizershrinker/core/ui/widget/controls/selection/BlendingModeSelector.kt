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

package ru.tech.imageresizershrinker.core.ui.widget.controls.selection

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.tech.imageresizershrinker.core.domain.image.model.BlendingMode
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.ui.utils.helper.entries

@Composable
fun BlendingModeSelector(
    value: BlendingMode,
    onValueChange: (BlendingMode) -> Unit,
    entries: List<BlendingMode> = remember {
        mutableListOf<BlendingMode>().apply {
            add(BlendingMode.SrcOver)
            addAll(
                BlendingMode
                    .entries
                    .toList() - listOf(
                    BlendingMode.SrcOver,
                    BlendingMode.Clear,
                    BlendingMode.Src,
                    BlendingMode.Dst
                ).toSet()
            )
        }
    },
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    color: Color = MaterialTheme.colorScheme.surface
) {
    DataSelector(
        value = value,
        onValueChange = onValueChange,
        entries = entries,
        title = stringResource(R.string.overlay_mode),
        titleIcon = Icons.Outlined.Layers,
        itemContentText = { it.toString() },
        modifier = modifier,
        shape = shape,
        color = color
    )
}