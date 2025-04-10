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

package ru.tech.imageresizershrinker.core.crash.presentation.components

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.tech.imageresizershrinker.core.crash.presentation.screenLogic.CrashComponent
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.settings.presentation.model.toUiState
import ru.tech.imageresizershrinker.core.ui.utils.helper.AppActivityClass
import ru.tech.imageresizershrinker.core.ui.utils.provider.ImageToolboxCompositionLocals
import ru.tech.imageresizershrinker.core.ui.utils.provider.LocalComponentActivity
import ru.tech.imageresizershrinker.core.ui.utils.provider.rememberLocalEssentials

@Composable
internal fun CrashRootContent(component: CrashComponent) {
    val context = LocalComponentActivity.current
    val crashInfo = component.crashInfo


    ImageToolboxCompositionLocals(
        settingsState = component.settingsState.toUiState()
    ) {
        val essentials = rememberLocalEssentials()
        val copyCrashInfo: () -> Unit = {
            essentials.copyToClipboard(crashInfo.textToSend)
            essentials.showToast(
                icon = Icons.Rounded.ContentCopy,
                message = context.getString(R.string.copied),
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 80.dp)
                .navigationBarsPadding()
                .displayCutoutPadding(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CrashAttentionCard()
            Spacer(modifier = Modifier.height(24.dp))
            CrashActionButtons(
                onCopyCrashInfo = copyCrashInfo,
                onShareLogs = component::shareLogs,
                githubLink = crashInfo.githubLink
            )
            Spacer(modifier = Modifier.height(24.dp))
            CrashInfoCard(crashInfo = crashInfo)
        }

        CrashBottomButtons(
            modifier = Modifier.align(Alignment.BottomCenter),
            onCopy = copyCrashInfo,
            onRestartApp = {
                context.startActivity(
                    Intent(context, AppActivityClass)
                )
            }
        )
    }
}