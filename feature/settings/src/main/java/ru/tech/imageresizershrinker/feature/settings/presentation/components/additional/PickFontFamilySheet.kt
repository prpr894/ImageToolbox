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

package ru.tech.imageresizershrinker.feature.settings.presentation.components.additional

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.rounded.FontDownload
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.resources.icons.FileExport
import ru.tech.imageresizershrinker.core.resources.icons.FileImport
import ru.tech.imageresizershrinker.core.settings.presentation.model.UiFontFamily
import ru.tech.imageresizershrinker.core.ui.theme.takeColorFromScheme
import ru.tech.imageresizershrinker.core.ui.utils.content_pickers.rememberFilePicker
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedBottomSheetDefaults
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedButton
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedModalBottomSheet
import ru.tech.imageresizershrinker.core.ui.widget.other.GradientEdge
import ru.tech.imageresizershrinker.core.ui.widget.other.InfoContainer
import ru.tech.imageresizershrinker.core.ui.widget.preferences.PreferenceItemDefaults
import ru.tech.imageresizershrinker.core.ui.widget.preferences.PreferenceRow
import ru.tech.imageresizershrinker.core.ui.widget.text.AutoSizeText
import ru.tech.imageresizershrinker.core.ui.widget.text.TitleItem

@Composable
internal fun PickFontFamilySheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onFontSelected: (UiFontFamily) -> Unit,
    onAddFont: (Uri) -> Unit,
    onRemoveFont: (UiFontFamily.Custom) -> Unit,
    onExportFonts: () -> Unit
) {
    EnhancedModalBottomSheet(
        visible = visible,
        onDismiss = {
            if (!it) onDismiss()
        },
        sheetContent = {
            val defaultEntries = UiFontFamily.defaultEntries
            val customEntries = UiFontFamily.customEntries

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                stickyHeader {
                    Column(
                        modifier = Modifier
                            .layout { measurable, constraints ->
                                val result = measurable.measure(
                                    constraints.copy(
                                        maxWidth = constraints.maxWidth + 32.dp.roundToPx()
                                    )
                                )
                                layout(
                                    result.measuredWidth,
                                    result.measuredHeight
                                ) {
                                    result.place(0, 0)
                                }
                            }
                            .background(EnhancedBottomSheetDefaults.containerColor)
                            .padding(horizontal = 16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.height(IntrinsicSize.Max)
                        ) {
                            val pickFileLauncher = rememberFilePicker(
                                mimeTypes = TTF_MIME_TYPES,
                                onSuccess = onAddFont
                            )
                            PreferenceRow(
                                title = stringResource(R.string.import_font),
                                onClick = pickFileLauncher::pickFile,
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    bottomStart = 16.dp,
                                    topEnd = 4.dp,
                                    bottomEnd = 4.dp
                                ),
                                titleFontStyle = PreferenceItemDefaults.TitleFontStyleCentered,
                                startIcon = Icons.Rounded.FileImport,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                color = MaterialTheme.colorScheme.primaryContainer
                            )

                            val canExport = customEntries.isNotEmpty()

                            PreferenceRow(
                                title = stringResource(R.string.export_fonts),
                                onClick = onExportFonts,
                                shape = RoundedCornerShape(
                                    topEnd = 16.dp,
                                    bottomEnd = 16.dp,
                                    topStart = 4.dp,
                                    bottomStart = 4.dp
                                ),
                                enabled = canExport,
                                startIcon = Icons.Rounded.FileExport,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                color = takeColorFromScheme {
                                    if (canExport) primaryContainer
                                    else surfaceVariant
                                },
                                titleFontStyle = PreferenceItemDefaults.TitleFontStyleCentered
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    GradientEdge(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp),
                        startColor = EnhancedBottomSheetDefaults.containerColor,
                        endColor = Color.Transparent
                    )
                }

                items(
                    items = defaultEntries,
                    key = { it.name ?: "sys" }
                ) { font ->
                    FontItem(
                        font = font,
                        onFontSelected = onFontSelected,
                        onRemoveFont = onRemoveFont
                    )
                }
                item {
                    InfoContainer(
                        text = stringResource(R.string.imported_fonts),
                        icon = Icons.Outlined.Extension,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f),
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(0.4f)
                    )
                }
                items(
                    items = customEntries,
                    key = { it.name ?: "sys" }
                ) { font ->
                    FontItem(
                        font = font,
                        onFontSelected = onFontSelected,
                        onRemoveFont = onRemoveFont
                    )
                }
            }
        },
        confirmButton = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                EnhancedButton(
                    onClick = onDismiss,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    AutoSizeText(stringResource(R.string.close))
                }
            }
        },
        title = {
            TitleItem(
                icon = Icons.Rounded.FontDownload,
                text = stringResource(R.string.font),
            )
        }
    )
}

private val TTF_MIME_TYPES = listOf(
    "font/ttf",
    "application/x-font-ttf",
    "font/otf"
)