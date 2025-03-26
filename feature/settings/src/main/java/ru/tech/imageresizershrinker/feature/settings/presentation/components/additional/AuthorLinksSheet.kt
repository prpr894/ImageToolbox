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

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import ru.tech.imageresizershrinker.core.domain.AUTHOR_LINK
import ru.tech.imageresizershrinker.core.domain.AUTHOR_TG
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.resources.icons.Github
import ru.tech.imageresizershrinker.core.resources.icons.Telegram
import ru.tech.imageresizershrinker.core.ui.utils.helper.ContextUtils.shareText
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedButton
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedModalBottomSheet
import ru.tech.imageresizershrinker.core.ui.widget.modifier.ContainerShapeDefaults.bottomShape
import ru.tech.imageresizershrinker.core.ui.widget.modifier.ContainerShapeDefaults.centerShape
import ru.tech.imageresizershrinker.core.ui.widget.modifier.ContainerShapeDefaults.topShape
import ru.tech.imageresizershrinker.core.ui.widget.preferences.PreferenceItem
import ru.tech.imageresizershrinker.core.ui.widget.text.AutoSizeText
import ru.tech.imageresizershrinker.core.ui.widget.text.TitleItem

@Composable
fun AuthorLinksSheet(
    visible: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val linkHandler = LocalUriHandler.current

    EnhancedModalBottomSheet(
        visible = visible,
        onDismiss = {
            if (!it) onDismiss()
        },
        title = {
            TitleItem(
                text = stringResource(R.string.app_developer_nick),
                icon = Icons.Rounded.Person
            )
        },
        confirmButton = {
            EnhancedButton(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                onClick = onDismiss,
            ) {
                AutoSizeText(stringResource(R.string.close))
            }
        },
        sheetContent = {
            Box {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Spacer(Modifier.height(16.dp))
                    PreferenceItem(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        onClick = {
                            linkHandler.openUri(AUTHOR_TG)
                        },
                        endIcon = Icons.Rounded.Link,
                        shape = topShape,
                        title = stringResource(R.string.telegram),
                        startIcon = Icons.Rounded.Telegram,
                        subtitle = stringResource(R.string.app_developer_nick)
                    )
                    Spacer(Modifier.height(4.dp))
                    PreferenceItem(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        onClick = {
                            val mail = context.getString(R.string.developer_email)
                            runCatching {
                                context.startActivity(
                                    Intent(Intent.ACTION_SENDTO).apply {
                                        data = "mailto:$mail".toUri()
                                    }
                                )
                            }.onFailure {
                                context.shareText(mail)
                            }
                        },
                        shape = centerShape,
                        endIcon = Icons.Rounded.Link,
                        title = stringResource(R.string.email),
                        startIcon = Icons.Rounded.AlternateEmail,
                        subtitle = stringResource(R.string.developer_email)
                    )
                    Spacer(Modifier.height(4.dp))
                    PreferenceItem(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        onClick = {
                            linkHandler.openUri(AUTHOR_LINK)
                        },
                        endIcon = Icons.Rounded.Link,
                        shape = bottomShape,
                        title = stringResource(R.string.github),
                        startIcon = Icons.Rounded.Github,
                        subtitle = stringResource(R.string.app_developer_nick)
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    )
}