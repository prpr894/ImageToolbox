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

package ru.tech.imageresizershrinker.feature.recognize.text.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Engineering
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.resources.icons.MiniEdit
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedButton
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedModalBottomSheet
import ru.tech.imageresizershrinker.core.ui.widget.modifier.ContainerShapeDefaults
import ru.tech.imageresizershrinker.core.ui.widget.preferences.PreferenceItem
import ru.tech.imageresizershrinker.core.ui.widget.text.TitleItem
import ru.tech.imageresizershrinker.feature.recognize.text.domain.OcrEngineMode

@Composable
fun OcrEngineModeSelector(
    value: OcrEngineMode,
    onValueChange: (OcrEngineMode) -> Unit
) {
    var showSelectionSheet by remember {
        mutableStateOf(false)
    }
    PreferenceItem(
        modifier = Modifier.fillMaxWidth(),
        title = stringResource(id = R.string.engine_mode),
        subtitle = stringResource(id = value.title),
        onClick = {
            showSelectionSheet = true
        },
        shape = RoundedCornerShape(24.dp),
        startIcon = Icons.Outlined.Engineering,
        endIcon = Icons.Rounded.MiniEdit
    )

    EnhancedModalBottomSheet(
        visible = showSelectionSheet,
        onDismiss = {
            showSelectionSheet = it
        },
        confirmButton = {
            EnhancedButton(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                onClick = {
                    showSelectionSheet = false
                }
            ) {
                Text(stringResource(R.string.close))
            }
        },
        title = {
            TitleItem(
                text = stringResource(id = R.string.engine_mode),
                icon = Icons.Outlined.Engineering
            )
        }
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            itemsIndexed(OcrEngineMode.entries) { index, mode ->
                PreferenceItem(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(id = mode.title),
                    onClick = {
                        onValueChange(mode)
                    },
                    color = animateColorAsState(
                        if (value == mode) MaterialTheme.colorScheme.secondaryContainer
                        else MaterialTheme.colorScheme.surfaceContainer
                    ).value,
                    shape = ContainerShapeDefaults.shapeForIndex(
                        index = index,
                        size = OcrEngineMode.entries.size
                    )
                )
            }
        }
    }
}

private inline val OcrEngineMode.title: Int
    get() = when (this) {
        OcrEngineMode.TESSERACT_ONLY -> R.string.legacy
        OcrEngineMode.LSTM_ONLY -> R.string.lstm_network
        OcrEngineMode.TESSERACT_LSTM_COMBINED -> R.string.legacy_and_lstm
        OcrEngineMode.DEFAULT -> R.string.defaultt
    }
