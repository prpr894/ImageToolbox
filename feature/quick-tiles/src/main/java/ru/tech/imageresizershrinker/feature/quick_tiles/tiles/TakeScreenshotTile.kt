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

package ru.tech.imageresizershrinker.feature.quick_tiles.tiles

import android.os.Build
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import ru.tech.imageresizershrinker.core.ui.utils.helper.ScreenshotAction
import ru.tech.imageresizershrinker.feature.quick_tiles.utils.startActivityAndCollapse

@RequiresApi(Build.VERSION_CODES.N)
class TakeScreenshotTile : TileService() {

    override fun onClick() {
        super.onClick()
        startActivityAndCollapse(ScreenshotAction)
    }

}