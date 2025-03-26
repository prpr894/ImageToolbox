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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.tech.imageresizershrinker.core.domain.TELEGRAM_CHANNEL_LINK
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.resources.icons.Sitemap
import ru.tech.imageresizershrinker.core.ui.widget.modifier.ContainerShapeDefaults
import ru.tech.imageresizershrinker.core.ui.widget.preferences.PreferenceRow

@Composable
fun TelegramChannelSettingItem(
    shape: Shape = ContainerShapeDefaults.centerShape,
    modifier: Modifier = Modifier.padding(horizontal = 8.dp)
) {
    val linkHandler = LocalUriHandler.current
    PreferenceRow(
        shape = shape,
        onClick = {
            linkHandler.openUri(TELEGRAM_CHANNEL_LINK)
        },
        startIcon = Icons.Outlined.Sitemap,
        title = stringResource(R.string.ci_channel),
        subtitle = stringResource(R.string.ci_channel_sub),
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        modifier = modifier
    )
}