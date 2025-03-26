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

package ru.tech.imageresizershrinker.feature.settings.presentation.components.additional

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.settings.presentation.model.UiFontFamily
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.hapticsClickable
import ru.tech.imageresizershrinker.core.ui.widget.modifier.animateShape
import ru.tech.imageresizershrinker.core.ui.widget.modifier.container
import ru.tech.imageresizershrinker.core.ui.widget.other.FontSelectionItem
import ru.tech.imageresizershrinker.core.ui.widget.other.RevealDirection
import ru.tech.imageresizershrinker.core.ui.widget.other.RevealValue
import ru.tech.imageresizershrinker.core.ui.widget.other.SwipeToReveal
import ru.tech.imageresizershrinker.core.ui.widget.other.rememberRevealState

@Composable
internal fun LazyItemScope.FontItem(
    font: UiFontFamily,
    onFontSelected: (UiFontFamily) -> Unit,
    onRemoveFont: (UiFontFamily.Custom) -> Unit
) {
    if (font is UiFontFamily.Custom) {
        val scope = rememberCoroutineScope()
        val state = rememberRevealState()
        val interactionSource = remember {
            MutableInteractionSource()
        }
        val isDragged by interactionSource.collectIsDraggedAsState()
        val shape = animateShape(
            if (isDragged) RoundedCornerShape(4.dp)
            else RoundedCornerShape(16.dp)
        )
        SwipeToReveal(
            state = state,
            modifier = Modifier.animateItem(),
            revealedContentEnd = {
                Box(
                    Modifier
                        .fillMaxSize()
                        .container(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = shape,
                            autoShadowElevation = 0.dp,
                            resultPadding = 0.dp
                        )
                        .hapticsClickable {
                            scope.launch {
                                state.animateTo(RevealValue.Default)
                            }
                            onRemoveFont(font)
                        }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = stringResource(R.string.delete),
                        modifier = Modifier
                            .padding(16.dp)
                            .padding(end = 8.dp)
                            .align(Alignment.CenterEnd),
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            },
            directions = setOf(RevealDirection.EndToStart),
            swipeableContent = {
                FontSelectionItem(
                    font = font,
                    onClick = {
                        onFontSelected(font)
                    },
                    onLongClick = {
                        scope.launch {
                            state.animateTo(RevealValue.FullyRevealedStart)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = shape
                )
            },
            interactionSource = interactionSource
        )
    } else {
        FontSelectionItem(
            font = font,
            onClick = {
                onFontSelected(font)
            }
        )
    }
}