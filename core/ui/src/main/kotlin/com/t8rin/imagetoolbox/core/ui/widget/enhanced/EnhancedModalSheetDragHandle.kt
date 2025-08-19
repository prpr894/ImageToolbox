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

package com.t8rin.imagetoolbox.core.ui.widget.enhanced

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.ui.widget.modifier.drawHorizontalStroke
import kotlin.math.tan


@Composable
fun EnhancedModalSheetDragHandle(
    modifier: Modifier = Modifier,
    color: Color = EnhancedBottomSheetDefaults.barContainerColor,
    drawStroke: Boolean = true,
    showDragHandle: Boolean = true,
    bendAngle: Float = 0f,
    strokeWidth: Dp = EnhancedBottomSheetDefaults.dragHandleHeight,
    heightWhenDisabled: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    val dragHandleWidth = LocalSettingsState.current.dragHandleWidth
    Column(
        modifier
            .then(
                if (drawStroke) {
                    Modifier
                        .drawHorizontalStroke(autoElevation = 3.dp)
                        .zIndex(Float.MAX_VALUE)
                } else Modifier
            )
            .background(color),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showDragHandle && dragHandleWidth > 0.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 22.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                BendableDragHandle(
                    width = dragHandleWidth,
                    angleDegrees = bendAngle,
                    strokeWidth = strokeWidth,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f).compositeOver(
                        MaterialTheme.colorScheme.surface
                    )
                )
            }
        } else {
            Spacer(modifier = Modifier.height(heightWhenDisabled))
        }

        content()
    }
}

@Composable
private fun BendableDragHandle(
    width: Dp,
    angleDegrees: Float,
    strokeWidth: Dp,
    color: Color,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val stroke = with(density) { strokeWidth.toPx() }
    val totalWidth = with(density) { width.toPx() }
    val halfWidth = totalWidth / 2f
    val halfStroke = stroke / 2f

    val angleRadians =
        Math.toRadians((angleDegrees * (64 / width.value).coerceAtMost(1f)).toDouble()).toFloat()
    val height = tan(angleRadians) * halfWidth

    Canvas(
        modifier = modifier
            .width(width)
            .height(height.dp + strokeWidth)
    ) {
        val centerY = size.height / 2f
        val centerX = size.width / 2f

        val leftStart = Offset(0f, centerY)
        val center = Offset(centerX, centerY + height)
        val rightEnd = Offset(size.width, centerY)

        drawLine(
            color = color,
            start = leftStart,
            end = center,
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )

        drawLine(
            color = color,
            start = center,
            end = rightEnd,
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )

        drawCircle(
            color = color,
            radius = halfStroke,
            center = center
        )
    }

}