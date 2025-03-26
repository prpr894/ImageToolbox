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

package ru.tech.imageresizershrinker.core.crash.presentation.screenLogic

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import ru.tech.imageresizershrinker.core.crash.presentation.components.CrashInfo
import ru.tech.imageresizershrinker.core.domain.dispatchers.DispatchersHolder
import ru.tech.imageresizershrinker.core.domain.image.ShareProvider
import ru.tech.imageresizershrinker.core.settings.domain.SettingsManager
import ru.tech.imageresizershrinker.core.settings.domain.model.SettingsState
import ru.tech.imageresizershrinker.core.ui.utils.BaseComponent

class CrashComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val crashInfo: CrashInfo,
    private val settingsManager: SettingsManager,
    private val shareProvider: ShareProvider<Bitmap>,
    dispatchersHolder: DispatchersHolder
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _settingsState = mutableStateOf(SettingsState.Default)
    val settingsState: SettingsState by _settingsState

    init {
        runBlocking {
            _settingsState.value = settingsManager.getSettingsState()
        }
        settingsManager.getSettingsStateFlow().onEach {
            _settingsState.value = it
        }.launchIn(componentScope)
    }

    fun shareLogs() {
        componentScope.launch {
            shareProvider.shareData(
                writeData = {
                    it.writeBytes(settingsManager.createLogsExport())
                },
                filename = settingsManager.createLogsFilename()
            )
        }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            crashInfo: CrashInfo
        ): CrashComponent
    }

}