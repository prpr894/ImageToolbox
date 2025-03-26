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

package ru.tech.imageresizershrinker.color_tools.presentation.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t8rin.histogram.HistogramType
import com.t8rin.histogram.ImageHistogram
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.ui.widget.controls.selection.ImageSelector
import ru.tech.imageresizershrinker.core.ui.widget.other.ExpandableItem
import ru.tech.imageresizershrinker.core.ui.widget.text.TitleItem

@Composable
internal fun ColorHistogram() {
    var imageUri by rememberSaveable {
        mutableStateOf<Uri?>(null)
    }

    ExpandableItem(
        visibleContent = {
            TitleItem(
                text = stringResource(R.string.histogram),
                icon = Icons.Rounded.AutoGraph
            )
        },
        expandableContent = {
            Column(
                modifier = Modifier.padding(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 8.dp
                )
            ) {
                ImageSelector(
                    value = imageUri,
                    onValueChange = {
                        imageUri = it
                    },
                    subtitle = stringResource(R.string.image_for_histogram),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ImageHistogram(
                        imageUri = imageUri,
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .fillMaxWidth()
                            .height(250.dp)
                            .background(MaterialTheme.colorScheme.background),
                        initialType = HistogramType.RGB,
                        onSwapType = null,
                        linesThickness = 1.dp,
                        bordersShape = RoundedCornerShape(6.dp)
                    )
                    ImageHistogram(
                        imageUri = imageUri,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .background(MaterialTheme.colorScheme.background),
                        initialType = HistogramType.Brightness,
                        onSwapType = null,
                        linesThickness = 1.dp,
                        bordersShape = RoundedCornerShape(6.dp)
                    )
                    ImageHistogram(
                        imageUri = imageUri,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .background(MaterialTheme.colorScheme.background),
                        initialType = HistogramType.Camera,
                        onSwapType = null,
                        linesThickness = 1.dp,
                        bordersShape = RoundedCornerShape(6.dp)
                    )
                }
            }
        },
        initialState = false
    )
}