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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.tech.imageresizershrinker.core.domain.image.model.ResizeType
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.resources.icons.Resize
import ru.tech.imageresizershrinker.core.settings.presentation.provider.LocalSettingsState
import ru.tech.imageresizershrinker.core.ui.utils.state.derivedValueOf
import ru.tech.imageresizershrinker.core.ui.widget.buttons.ToggleGroupButton
import ru.tech.imageresizershrinker.core.ui.widget.modifier.ContainerShapeDefaults
import ru.tech.imageresizershrinker.core.ui.widget.modifier.container
import ru.tech.imageresizershrinker.core.ui.widget.text.TitleItem

@Composable
fun DefaultResizeTypeSettingItem(
    onValueChange: (ResizeType) -> Unit,
    shape: Shape = ContainerShapeDefaults.bottomShape,
    modifier: Modifier = Modifier.padding(horizontal = 8.dp)
) {
    val settingsState = LocalSettingsState.current
    val value = settingsState.defaultResizeType
    val entries = remember {
        ResizeType.entries
    }

    Column(modifier = modifier.container(shape = shape)) {
        TitleItem(
            text = stringResource(R.string.resize_type),
            icon = Icons.Outlined.Resize,
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .padding(top = 12.dp)
        )
        ToggleGroupButton(
            enabled = true,
            itemCount = entries.size,
            title = {},
            selectedIndex = derivedValueOf(value) {
                entries.indexOfFirst { it::class.isInstance(value) }
            },
            buttonIcon = {},
            activeButtonColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            inactiveButtonColor = MaterialTheme.colorScheme.surfaceContainer,
            itemContent = {
                Text(stringResource(entries[it].getTitle()))
            },
            onIndexChange = {
                onValueChange(entries[it])
            }
        )
    }
}

private fun ResizeType.getTitle(): Int = when (this) {
    is ResizeType.CenterCrop -> R.string.crop
    is ResizeType.Explicit -> R.string.explicit
    is ResizeType.Flexible -> R.string.flexible
    is ResizeType.Fit -> R.string.fit
}