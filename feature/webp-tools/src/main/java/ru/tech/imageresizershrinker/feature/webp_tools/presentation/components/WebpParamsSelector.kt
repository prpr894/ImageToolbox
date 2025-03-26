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

package ru.tech.imageresizershrinker.feature.webp_tools.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhotoSizeSelectLarge
import androidx.compose.material.icons.outlined.RepeatOne
import androidx.compose.material.icons.outlined.Timelapse
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.tech.imageresizershrinker.core.domain.image.model.ImageFormat
import ru.tech.imageresizershrinker.core.domain.image.model.ImageInfo
import ru.tech.imageresizershrinker.core.domain.image.model.Quality
import ru.tech.imageresizershrinker.core.domain.model.IntegerSize
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.ui.widget.controls.ResizeImageField
import ru.tech.imageresizershrinker.core.ui.widget.controls.selection.QualitySelector
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedSliderItem
import ru.tech.imageresizershrinker.core.ui.widget.preferences.PreferenceRowSwitch
import ru.tech.imageresizershrinker.feature.webp_tools.domain.WebpParams
import kotlin.math.roundToInt

@Composable
fun WebpParamsSelector(
    value: WebpParams,
    onValueChange: (WebpParams) -> Unit
) {
    Column {
        val size = value.size ?: IntegerSize.Undefined
        AnimatedVisibility(size.isDefined()) {
            ResizeImageField(
                imageInfo = ImageInfo(size.width, size.height),
                originalSize = null,
                onWidthChange = {
                    onValueChange(
                        value.copy(
                            size = size.copy(width = it)
                        )
                    )
                },
                onHeightChange = {
                    onValueChange(
                        value.copy(
                            size = size.copy(height = it)
                        )
                    )
                }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        PreferenceRowSwitch(
            title = stringResource(id = R.string.use_size_of_first_frame),
            subtitle = stringResource(id = R.string.use_size_of_first_frame_sub),
            checked = value.size == null,
            onClick = {
                onValueChange(
                    value.copy(size = if (it) null else IntegerSize(1000, 1000))
                )
            },
            startIcon = Icons.Outlined.PhotoSizeSelectLarge,
            modifier = Modifier.fillMaxWidth(),
            color = Color.Unspecified,
            shape = RoundedCornerShape(24.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        QualitySelector(
            imageFormat = ImageFormat.Jpg,
            quality = value.quality,
            onQualityChange = {
                onValueChange(
                    value.copy(
                        quality = Quality.Base(it.qualityValue)
                    )
                )
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        EnhancedSliderItem(
            value = value.repeatCount,
            icon = Icons.Outlined.RepeatOne,
            title = stringResource(id = R.string.repeat_count),
            valueRange = 1f..10f,
            steps = 9,
            internalStateTransformation = { it.roundToInt() },
            onValueChange = {
                onValueChange(
                    value.copy(
                        repeatCount = it.roundToInt()
                    )
                )
            },
            shape = RoundedCornerShape(24.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        EnhancedSliderItem(
            value = value.delay,
            icon = Icons.Outlined.Timelapse,
            title = stringResource(id = R.string.frame_delay),
            valueRange = 1f..4000f,
            internalStateTransformation = { it.roundToInt() },
            onValueChange = {
                onValueChange(
                    value.copy(
                        delay = it.roundToInt()
                    )
                )
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}