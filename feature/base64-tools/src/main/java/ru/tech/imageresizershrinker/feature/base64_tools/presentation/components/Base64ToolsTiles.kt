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

package ru.tech.imageresizershrinker.feature.base64_tools.presentation.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.CopyAll
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.tech.imageresizershrinker.core.domain.utils.isBase64
import ru.tech.imageresizershrinker.core.domain.utils.trimToBase64
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.resources.icons.Base64
import ru.tech.imageresizershrinker.core.ui.utils.provider.rememberLocalEssentials
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedIconButton
import ru.tech.imageresizershrinker.core.ui.widget.modifier.container
import ru.tech.imageresizershrinker.core.ui.widget.preferences.PreferenceItemDefaults
import ru.tech.imageresizershrinker.core.ui.widget.preferences.PreferenceRow
import ru.tech.imageresizershrinker.feature.base64_tools.presentation.screenLogic.Base64ToolsComponent

@Composable
internal fun Base64ToolsTiles(component: Base64ToolsComponent) {
    val essentials = rememberLocalEssentials()
    val context = LocalContext.current

    val pasteTile: @Composable RowScope.(shape: Shape) -> Unit = { shape ->
        Tile(
            onClick = {
                essentials.getTextFromClipboard { text ->
                    val text = text.trimToBase64()

                    if (text.isBase64()) {
                        component.setBase64(text)
                    } else {
                        essentials.showToast(
                            message = context.getString(R.string.not_a_valid_base_64),
                            icon = Icons.Rounded.Base64
                        )
                    }
                }
            },
            shape = shape,
            icon = Icons.Rounded.ContentPaste,
            textRes = R.string.paste_base_64,
            isButton = component.uri != null
        )
    }

    val importTile: @Composable RowScope.(shape: Shape) -> Unit = { shape ->
        val pickLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            uri?.let {
                component.setBase64FromUri(
                    uri = uri,
                    onFailure = {
                        essentials.showToast(
                            message = context.getString(R.string.not_a_valid_base_64),
                            icon = Icons.Rounded.Base64
                        )
                    }
                )
            }
        }

        Tile(
            onClick = {
                pickLauncher.launch(arrayOf("text/plain"))
            },
            shape = shape,
            icon = Icons.Rounded.FileOpen,
            textRes = R.string.import_base_64,
            isButton = component.uri != null
        )
    }

    AnimatedContent(component.uri == null) { isNoUri ->
        if (isNoUri) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                pasteTile(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 4.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 4.dp
                    )
                )
                importTile(
                    RoundedCornerShape(
                        topStart = 4.dp,
                        topEnd = 16.dp,
                        bottomStart = 4.dp,
                        bottomEnd = 16.dp
                    )
                )
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .container(
                        shape = RoundedCornerShape(28.dp),
                        resultPadding = 0.dp
                    )
                    .padding(
                        horizontal = 12.dp
                    )
                    .padding(
                        top = 4.dp,
                        bottom = 8.dp
                    )
            ) {
                Text(
                    text = stringResource(R.string.base_64_actions),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(8.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    pasteTile(CircleShape)
                    importTile(CircleShape)

                    Tile(
                        onClick = {
                            val text = buildAnnotatedString {
                                append(component.base64String)
                            }
                            if (component.base64String.isBase64()) {
                                essentials.copyToClipboard(text)
                                essentials.showToast(
                                    message = context.getString(R.string.copied),
                                    icon = Icons.Rounded.CopyAll
                                )
                            } else {
                                essentials.showToast(
                                    message = context.getString(R.string.copy_not_a_valid_base_64),
                                    icon = Icons.Rounded.Base64
                                )
                            }
                        },
                        icon = Icons.Rounded.CopyAll,
                        textRes = R.string.copy_base_64
                    )

                    val shareText = {
                        component.shareText(essentials::showConfetti)
                    }

                    Tile(
                        onClick = shareText,
                        shape = RoundedCornerShape(
                            topStart = 4.dp,
                            topEnd = 4.dp,
                            bottomStart = 16.dp,
                            bottomEnd = 4.dp
                        ),
                        icon = Icons.Outlined.Share,
                        textRes = R.string.share_base_64
                    )

                    val saveLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.CreateDocument("text/plain")
                    ) { uri ->
                        uri?.let {
                            component.saveContentToTxt(
                                uri = uri,
                                onResult = essentials::parseFileSaveResult
                            )
                        }
                    }
                    val saveText: () -> Unit = {
                        saveLauncher.launch(component.generateTextFilename())
                    }

                    Tile(
                        onClick = saveText,
                        icon = Icons.Outlined.Save,
                        textRes = R.string.save_base_64
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.Tile(
    onClick: () -> Unit,
    shape: Shape = CircleShape,
    icon: ImageVector,
    textRes: Int,
    isButton: Boolean = true
) {
    if (isButton) {
        val tooltipState = rememberTooltipState()
        Box(
            modifier = Modifier.weight(1f)
        ) {
            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
                tooltip = {
                    PlainTooltip {
                        Text(stringResource(id = textRes))
                    }
                },
                state = tooltipState
            ) {
                EnhancedIconButton(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(0.5f),
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null
                    )
                }
            }
        }
    } else {
        PreferenceRow(
            title = stringResource(id = textRes),
            onClick = onClick,
            shape = shape,
            titleFontStyle = PreferenceItemDefaults.TitleFontStyleCenteredSmall.copy(
                fontSize = 13.sp
            ),
            startIcon = icon,
            drawStartIconContainer = false,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(0.5f),
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}