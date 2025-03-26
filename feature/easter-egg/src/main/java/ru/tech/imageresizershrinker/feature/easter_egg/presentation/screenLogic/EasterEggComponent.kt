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

package ru.tech.imageresizershrinker.feature.easter_egg.presentation.screenLogic

import com.arkivanov.decompose.ComponentContext
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import ru.tech.imageresizershrinker.core.domain.dispatchers.DispatchersHolder
import ru.tech.imageresizershrinker.core.ui.utils.BaseComponent

class EasterEggComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    dispatchersHolder: DispatchersHolder
) : BaseComponent(dispatchersHolder, componentContext) {

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
        ): EasterEggComponent
    }

}