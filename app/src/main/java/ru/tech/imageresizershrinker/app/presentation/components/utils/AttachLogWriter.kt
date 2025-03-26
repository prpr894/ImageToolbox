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

package ru.tech.imageresizershrinker.app.presentation.components.utils

import android.app.Application
import com.t8rin.logger.Logger
import com.t8rin.logger.attachLogWriter
import ru.tech.imageresizershrinker.core.crash.presentation.components.DeviceInfo
import ru.tech.imageresizershrinker.core.resources.R

internal fun Application.attachLogWriter() {
    Logger.attachLogWriter(
        context = this@attachLogWriter,
        fileProvider = getString(R.string.file_provider),
        logsFilename = "image_toolbox_logs.txt",
        startupLog = Logger.Log(
            tag = "Device Info",
            message = "--${DeviceInfo.get()}--",
            level = Logger.Level.Info
        ),
        isSyncCreate = false,
        maxFileSize = null
    )
}