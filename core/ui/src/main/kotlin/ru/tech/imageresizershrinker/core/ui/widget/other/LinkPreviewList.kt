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

package ru.tech.imageresizershrinker.core.ui.widget.other

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.settings.presentation.provider.LocalSettingsState
import ru.tech.imageresizershrinker.core.ui.utils.helper.LinkPreview
import ru.tech.imageresizershrinker.core.ui.utils.helper.LinkUtils
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedIconButton
import ru.tech.imageresizershrinker.core.ui.widget.modifier.ContainerShapeDefaults
import ru.tech.imageresizershrinker.core.ui.widget.modifier.container
import ru.tech.imageresizershrinker.core.ui.widget.text.TitleItem

@Composable
fun LinkPreviewList(
    text: String,
    modifier: Modifier
) {
    val settingsState = LocalSettingsState.current
    if (!settingsState.isLinkPreviewEnabled) return

    var isLoading by rememberSaveable {
        mutableStateOf(false)
    }
    var linkPreviewList by remember {
        mutableStateOf(emptySet<LinkPreview>())
    }
    var expanded by rememberSaveable { mutableStateOf(true) }
    val rotation by animateFloatAsState(if (expanded) 180f else 0f)

    LaunchedEffect(text) {
        delay(
            if (linkPreviewList.isNotEmpty()) 1000
            else 0
        )
        isLoading = true
        linkPreviewList = emptySet()
        LinkUtils.parseLinks(text).forEach { link ->
            linkPreviewList += LinkPreview(link)
        }
        isLoading = false
    }

    val links = remember(expanded, linkPreviewList) {
        if (linkPreviewList.size > 3 && expanded) {
            linkPreviewList
        } else linkPreviewList.take(3)
    }

    AnimatedVisibility(
        modifier = Modifier.fillMaxWidth(),
        visible = !isLoading && linkPreviewList.isNotEmpty()
    ) {
        Column(
            modifier = Modifier
                .then(modifier)
                .container(
                    shape = RoundedCornerShape(20.dp),
                    resultPadding = 0.dp
                )
                .padding(8.dp)
        ) {

            Column {
                TitleItem(
                    text = stringResource(R.string.links),
                    icon = Icons.Rounded.Link,
                    modifier = Modifier.padding(8.dp)
                )
                Spacer(modifier = Modifier.padding(4.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    links.forEachIndexed { index, link ->
                        LinkPreviewCard(
                            linkPreview = link,
                            shape = ContainerShapeDefaults.shapeForIndex(
                                index = index,
                                size = links.size
                            )
                        )
                    }
                }
                AnimatedVisibility(
                    modifier = Modifier.fillMaxWidth(),
                    visible = linkPreviewList.size > 3
                ) {
                    EnhancedIconButton(
                        onClick = { expanded = !expanded }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "Expand",
                            modifier = Modifier.rotate(rotation)
                        )
                    }
                }
            }
        }
    }
}