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

package com.t8rin.imagetoolbox.core.ui.utils.helper

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

@Composable
fun PredictiveBackObserver(
    onProgress: (Float) -> Unit,
    onClean: suspend (isCompleted: Boolean) -> Unit,
    enabled: Boolean = true
) {
    val scope = rememberCoroutineScope()

    if (!enabled) return

    PredictiveBackHandler { progress ->
        try {
            progress.collect { event ->
                if (event.progress <= 0.05f) {
                    onClean(false)
                }
                onProgress(event.progress)
            }
            scope.launch {
                onClean(true)
            }
        } catch (_: CancellationException) {
            onClean(false)
        }
    }
}