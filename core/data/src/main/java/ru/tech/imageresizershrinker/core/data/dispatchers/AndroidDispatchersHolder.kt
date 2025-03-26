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

package ru.tech.imageresizershrinker.core.data.dispatchers

import ru.tech.imageresizershrinker.core.di.DecodingDispatcher
import ru.tech.imageresizershrinker.core.di.DefaultDispatcher
import ru.tech.imageresizershrinker.core.di.EncodingDispatcher
import ru.tech.imageresizershrinker.core.di.IoDispatcher
import ru.tech.imageresizershrinker.core.di.UiDispatcher
import ru.tech.imageresizershrinker.core.domain.dispatchers.DispatchersHolder
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

internal data class AndroidDispatchersHolder @Inject constructor(
    @UiDispatcher override val uiDispatcher: CoroutineContext,
    @IoDispatcher override val ioDispatcher: CoroutineContext,
    @EncodingDispatcher override val encodingDispatcher: CoroutineContext,
    @DecodingDispatcher override val decodingDispatcher: CoroutineContext,
    @DefaultDispatcher override val defaultDispatcher: CoroutineContext
) : DispatchersHolder