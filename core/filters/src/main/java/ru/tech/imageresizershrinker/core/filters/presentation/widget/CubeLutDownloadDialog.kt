/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2025 T8RIN (Malik Mukhametzyanov)
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

package ru.tech.imageresizershrinker.core.filters.presentation.widget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.tech.imageresizershrinker.core.domain.remote.RemoteResourcesDownloadProgress
import ru.tech.imageresizershrinker.core.domain.utils.readableByteCount
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.BasicEnhancedAlertDialog
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedAlertDialog
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedButton
import ru.tech.imageresizershrinker.core.ui.widget.other.LoadingIndicator

@Composable
internal fun CubeLutDownloadDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onDownload: () -> Unit,
    downloadOnlyNewData: Boolean,
    cubeLutDownloadProgress: RemoteResourcesDownloadProgress?
) {
    EnhancedAlertDialog(
        visible = visible,
        icon = {
            Icon(
                imageVector = Icons.Outlined.TableChart,
                contentDescription = null
            )
        },
        title = { Text(stringResource(id = R.string.cube_lut)) },
        text = {
            Text(
                stringResource(
                    if (downloadOnlyNewData) R.string.lut_library_update_sub
                    else R.string.lut_library_sub
                )
            )
        },
        onDismissRequest = {},
        confirmButton = {
            EnhancedButton(
                onClick = onDownload
            ) {
                Text(stringResource(R.string.download))
            }
        },
        dismissButton = {
            EnhancedButton(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.close))
            }
        }
    )

    BasicEnhancedAlertDialog(
        onDismissRequest = {},
        visible = cubeLutDownloadProgress != null,
        modifier = Modifier.fillMaxSize()
    ) {
        LoadingIndicator(
            progress = (cubeLutDownloadProgress?.currentPercent ?: 0f) / 100,
            loaderSize = 72.dp
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Text(
                    text = cubeLutDownloadProgress?.run { "$itemsDownloaded/$itemsCount" }
                        ?: "",
                    maxLines = 1,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    lineHeight = 12.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = readableByteCount(cubeLutDownloadProgress?.currentTotalSize ?: 0),
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    fontSize = 10.sp,
                    lineHeight = 10.sp
                )
            }
        }
    }
}