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

package ru.tech.imageresizershrinker.core.ui.widget.enhanced

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.tech.imageresizershrinker.core.ui.theme.outlineVariant
import ru.tech.imageresizershrinker.core.ui.utils.provider.LocalContainerShape
import ru.tech.imageresizershrinker.core.ui.widget.modifier.container
import ru.tech.imageresizershrinker.core.ui.widget.modifier.shapeByInteraction

@Composable
fun EnhancedChip(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(6.dp),
    selectedColor: Color,
    selectedContentColor: Color = MaterialTheme.colorScheme.contentColorFor(selectedColor),
    unselectedColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    unselectedContentColor: Color = MaterialTheme.colorScheme.onSurface,
    shape: Shape = RoundedCornerShape(10.dp),
    pressedShape: Shape = RoundedCornerShape(4.dp),
    interactionSource: MutableInteractionSource? = null,
    defaultMinSize: Dp = 36.dp,
    label: @Composable () -> Unit
) {
    val color by animateColorAsState(
        if (selected) selectedColor
        else unselectedColor
    )
    val contentColor by animateColorAsState(
        if (selected) selectedContentColor
        else unselectedContentColor
    )

    val realInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val shape = shapeByInteraction(
        shape = shape,
        pressedShape = pressedShape,
        interactionSource = realInteractionSource
    )

    CompositionLocalProvider(
        LocalTextStyle provides MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        ),
        LocalContentColor provides contentColor,
        LocalContainerShape provides null
    ) {
        Box(
            modifier = modifier
                .defaultMinSize(defaultMinSize, defaultMinSize)
                .container(
                    color = color,
                    resultPadding = 0.dp,
                    borderColor = if (!selected) MaterialTheme.colorScheme.outlineVariant()
                    else selectedColor
                        .copy(alpha = 0.9f)
                        .compositeOver(Color.Black),
                    shape = shape,
                    autoShadowElevation = 0.5.dp
                )
                .then(
                    onClick?.let {
                        Modifier.hapticsClickable(
                            indication = LocalIndication.current,
                            interactionSource = realInteractionSource,
                            onClick = onClick
                        )
                    } ?: Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.padding(contentPadding),
                contentAlignment = Alignment.Center
            ) {
                label()
            }
        }
    }
}