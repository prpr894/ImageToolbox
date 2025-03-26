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

package ru.tech.imageresizershrinker.feature.markup_layers.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.request.ImageRequest
import ru.tech.imageresizershrinker.core.settings.presentation.model.toUiFont
import ru.tech.imageresizershrinker.core.ui.theme.toColor
import ru.tech.imageresizershrinker.core.ui.widget.image.Picture
import ru.tech.imageresizershrinker.core.ui.widget.text.OutlineParams
import ru.tech.imageresizershrinker.core.ui.widget.text.OutlinedText
import ru.tech.imageresizershrinker.feature.markup_layers.domain.DomainTextDecoration
import ru.tech.imageresizershrinker.feature.markup_layers.domain.LayerType

@Composable
internal fun LayerContent(
    modifier: Modifier = Modifier,
    type: LayerType,
    textFullSize: Int
) {
    when (type) {
        is LayerType.Picture -> {
            Picture(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(type.imageData)
                    .size(1600)
                    .build(),
                contentScale = ContentScale.Fit,
                modifier = modifier,
                showTransparencyChecker = false
            )
        }

        is LayerType.Text -> {
            val style = LocalTextStyle.current
            val fontFamily = type.font.toUiFont().fontFamily
            val mergedStyle by remember(style, type, fontFamily) {
                derivedStateOf {
                    style.copy(
                        color = type.color.toColor(),
                        fontSize = (textFullSize * type.size / 5).sp,
                        lineHeight = (textFullSize * type.size / 5).sp,
                        fontFamily = fontFamily,
                        textDecoration = TextDecoration.combine(
                            type.decorations.mapNotNull {
                                when (it) {
                                    DomainTextDecoration.LineThrough -> TextDecoration.LineThrough
                                    DomainTextDecoration.Underline -> TextDecoration.Underline
                                    else -> null
                                }
                            }
                        ),
                        fontWeight = if (type.decorations.any { it == DomainTextDecoration.Bold }) {
                            FontWeight.Bold
                        } else {
                            style.fontWeight
                        },
                        fontStyle = if (type.decorations.any { it == DomainTextDecoration.Italic }) {
                            FontStyle.Italic
                        } else {
                            FontStyle.Normal
                        }
                    )
                }
            }
            val outlineParams by remember(style, type) {
                derivedStateOf {
                    type.outline?.let {
                        OutlineParams(
                            color = Color(it.color),
                            stroke = Stroke(
                                width = it.width,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }
            }

            OutlinedText(
                text = type.text,
                style = mergedStyle,
                outlineParams = outlineParams,
                modifier = Modifier
                    .background(
                        color = type.backgroundColor.toColor(),
                        shape = RoundedCornerShape(3.dp)
                    )
                    .padding(
                        horizontal = (textFullSize * type.size / 10).dp,
                        vertical = (textFullSize * type.size / 12).dp
                    )
            )
        }
    }
}