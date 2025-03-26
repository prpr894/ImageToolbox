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

package ru.tech.imageresizershrinker.feature.quick_tiles.screenshot

import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.ui.utils.helper.ContextUtils.buildIntent
import ru.tech.imageresizershrinker.core.ui.utils.helper.ContextUtils.postToast
import ru.tech.imageresizershrinker.core.ui.utils.helper.DataExtra
import ru.tech.imageresizershrinker.core.ui.utils.helper.ResultCode
import ru.tech.imageresizershrinker.core.ui.utils.helper.getTileScreenAction
import ru.tech.imageresizershrinker.core.ui.utils.helper.putTileScreenAction

class ScreenshotLauncher : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val projectionManager = getSystemService<MediaProjectionManager>()
        val captureIntent = projectionManager?.createScreenCaptureIntent()

        if (captureIntent == null) {
            onFailure(NullPointerException("No projection manager"))
        }

        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            runCatching {
                val resultCode = it.resultCode
                val data = it.data
                if (resultCode == RESULT_OK) {
                    val serviceIntent = buildIntent(ScreenshotService::class.java) {
                        putExtra(DataExtra, data)
                        putExtra(ResultCode, resultCode)
                        putTileScreenAction(intent.getTileScreenAction())
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(serviceIntent)
                    } else {
                        startService(serviceIntent)
                    }
                    finish()
                } else throw SecurityException()
            }.onFailure(::onFailure)
        }.launch(captureIntent!!)
    }

    private fun onFailure(throwable: Throwable) {
        postToast(
            textRes = R.string.smth_went_wrong,
            isLong = true,
            throwable.localizedMessage ?: ""
        )
        finish()
    }

}