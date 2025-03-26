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

package ru.tech.imageresizershrinker.core.ui.utils

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import ru.tech.imageresizershrinker.core.domain.dispatchers.DispatchersHolder
import ru.tech.imageresizershrinker.core.domain.utils.smartJob
import ru.tech.imageresizershrinker.core.ui.utils.navigation.coroutineScope
import ru.tech.imageresizershrinker.core.ui.utils.state.update
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.launch as internalLaunch

@Stable
@Immutable
abstract class BaseComponent(
    private val dispatchersHolder: DispatchersHolder,
    private val componentContext: ComponentContext
) : ComponentContext by componentContext, DispatchersHolder by dispatchersHolder {

    val componentScope = coroutineScope

    protected open val _isImageLoading: MutableState<Boolean> = mutableStateOf(false)
    open val isImageLoading: Boolean by _isImageLoading

    private var imageCalculationJob: Job? by smartJob {
        _isImageLoading.update { false }
    }

    inline fun debounce(
        time: Long = 150,
        crossinline block: suspend () -> Unit
    ) {
        componentScope.launch {
            delay(time)
            block()
        }
    }

    protected open val _haveChanges: MutableState<Boolean> = mutableStateOf(false)
    open val haveChanges: Boolean by _haveChanges

    protected fun registerSave() {
        _haveChanges.update { false }
    }

    protected fun registerChangesCleared() {
        _haveChanges.update { false }
    }

    protected fun registerChanges() {
        _haveChanges.update { true }
    }

    protected open fun debouncedImageCalculation(
        onFinish: suspend () -> Unit = {},
        delay: Long = 600L,
        action: suspend () -> Unit
    ) {
        imageCalculationJob = componentScope.launch(
            CoroutineExceptionHandler { _, _ ->
                _isImageLoading.update { false }
            } + defaultDispatcher
        ) {
            _isImageLoading.update { true }
            delay(delay)

            ensureActive()

            action()

            ensureActive()

            _isImageLoading.update { false }

            onFinish()
        }
    }

    fun cancelImageLoading() {
        _isImageLoading.update { false }
        imageCalculationJob?.cancel()
        imageCalculationJob = null
    }

    open fun resetState(): Unit =
        throw IllegalAccessException("Cannot reset state of ${this::class.simpleName}")

    fun CoroutineScope.launch(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ): Job = internalLaunch(context, start) {
        delay(50L)
        block()
    }

}