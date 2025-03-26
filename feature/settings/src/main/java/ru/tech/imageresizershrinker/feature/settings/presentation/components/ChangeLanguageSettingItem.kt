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

package ru.tech.imageresizershrinker.feature.settings.presentation.components

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.resources.icons.MiniEdit
import ru.tech.imageresizershrinker.core.ui.utils.helper.ContextUtils
import ru.tech.imageresizershrinker.core.ui.utils.helper.ContextUtils.getCurrentLocaleString
import ru.tech.imageresizershrinker.core.ui.utils.helper.ContextUtils.getLanguages
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedBottomSheetDefaults
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedButton
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedModalBottomSheet
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedRadioButton
import ru.tech.imageresizershrinker.core.ui.widget.modifier.ContainerShapeDefaults
import ru.tech.imageresizershrinker.core.ui.widget.preferences.PreferenceItem
import ru.tech.imageresizershrinker.core.ui.widget.preferences.PreferenceItemOverload
import ru.tech.imageresizershrinker.core.ui.widget.text.AutoSizeText
import ru.tech.imageresizershrinker.core.ui.widget.text.TitleItem

@Composable
fun ChangeLanguageSettingItem(
    modifier: Modifier = Modifier.padding(horizontal = 8.dp),
    shape: Shape = ContainerShapeDefaults.topShape
) {
    val context = LocalContext.current
    var showEmbeddedLanguagePicker by rememberSaveable { mutableStateOf(false) }

    Column(Modifier.animateContentSize()) {
        PreferenceItem(
            shape = shape,
            modifier = modifier.padding(bottom = 1.dp),
            title = stringResource(R.string.language),
            subtitle = remember {
                context.getCurrentLocaleString()
            },
            startIcon = Icons.Outlined.Language,
            endIcon = Icons.Rounded.MiniEdit,
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !ContextUtils.isMiUi() && !ContextUtils.isRedMagic()) {
                    runCatching {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_APP_LOCALE_SETTINGS,
                                Uri.parse("package:${context.packageName}")
                            )
                        )
                    }.onFailure {
                        showEmbeddedLanguagePicker = true
                    }
                } else {
                    showEmbeddedLanguagePicker = true
                }
            }
        )
    }

    PickLanguageSheet(
        entries = remember {
            context.getLanguages()
        },
        selected = remember {
            context.getCurrentLocaleString()
        },
        onSelect = {
            val locale = if (it == "") {
                LocaleListCompat.getEmptyLocaleList()
            } else {
                LocaleListCompat.forLanguageTags(it)
            }
            AppCompatDelegate.setApplicationLocales(locale)
        },
        visible = showEmbeddedLanguagePicker,
        onDismiss = {
            showEmbeddedLanguagePicker = false
        }
    )
}

@Composable
private fun PickLanguageSheet(
    entries: Map<String, String>,
    selected: String,
    onSelect: (String) -> Unit,
    visible: Boolean,
    onDismiss: () -> Unit
) {
    EnhancedModalBottomSheet(
        onDismiss = {
            if (!it) onDismiss()
        },
        title = {
            TitleItem(
                text = stringResource(R.string.language),
                icon = Icons.Rounded.Language
            )
        },
        sheetContent = {
            Box {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp)
                ) {
                    entries.entries.forEachIndexed { index, locale ->
                        val isSelected =
                            selected == locale.value || (selected.isEmpty() && index == 0)
                        PreferenceItemOverload(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                onSelect(locale.key)
                            },
                            resultModifier = Modifier.padding(
                                start = 16.dp,
                                end = 8.dp,
                                top = 8.dp,
                                bottom = 8.dp
                            ),
                            color = animateColorAsState(
                                if (isSelected) MaterialTheme
                                    .colorScheme
                                    .secondaryContainer
                                else EnhancedBottomSheetDefaults.containerColor
                            ).value,
                            shape = ContainerShapeDefaults.shapeForIndex(
                                index = index,
                                size = entries.size
                            ),
                            endIcon = {
                                EnhancedRadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        onSelect(locale.key)
                                    }
                                )
                            },
                            title = locale.value
                        )
                    }
                }
            }
        },
        confirmButton = {
            EnhancedButton(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                onClick = onDismiss
            ) {
                AutoSizeText(stringResource(R.string.close))
            }
        },
        visible = visible
    )
}