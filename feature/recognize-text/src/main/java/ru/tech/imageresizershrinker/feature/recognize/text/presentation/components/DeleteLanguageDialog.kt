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

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedAlertDialog
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedButton
import ru.tech.imageresizershrinker.feature.recognize.text.domain.OCRLanguage
import ru.tech.imageresizershrinker.feature.recognize.text.domain.RecognitionType

@Composable
internal fun DeleteLanguageDialog(
    languageToDelete: OCRLanguage?,
    onDismiss: () -> Unit,
    onDeleteLanguage: (OCRLanguage, List<RecognitionType>) -> Unit,
    currentRecognitionType: RecognitionType
) {
    EnhancedAlertDialog(
        visible = languageToDelete != null,
        icon = {
            Icon(
                imageVector = Icons.Outlined.DeleteOutline,
                contentDescription = null
            )
        },
        title = { Text(stringResource(id = R.string.delete)) },
        text = {
            Text(
                stringResource(
                    id = R.string.delete_language_sub,
                    languageToDelete?.name ?: "",
                    currentRecognitionType.displayName
                )
            )
        },
        onDismissRequest = onDismiss,
        confirmButton = {
            EnhancedButton(
                containerColor = MaterialTheme.colorScheme.error,
                onClick = {
                    languageToDelete?.let {
                        onDeleteLanguage(it, listOf(currentRecognitionType))
                    }
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.current))
            }
        },
        dismissButton = {
            EnhancedButton(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                onClick = {
                    languageToDelete?.let {
                        onDeleteLanguage(it, RecognitionType.entries)
                    }
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.all))
            }
        }
    )
}