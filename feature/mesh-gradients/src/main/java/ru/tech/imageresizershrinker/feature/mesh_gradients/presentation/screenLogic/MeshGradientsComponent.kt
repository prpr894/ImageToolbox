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

package ru.tech.imageresizershrinker.feature.mesh_gradients.presentation.screenLogic

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import com.arkivanov.decompose.ComponentContext
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import ru.tech.imageresizershrinker.core.domain.dispatchers.DispatchersHolder
import ru.tech.imageresizershrinker.core.domain.image.ImageGetter
import ru.tech.imageresizershrinker.core.domain.image.ShareProvider
import ru.tech.imageresizershrinker.core.domain.remote.RemoteResources
import ru.tech.imageresizershrinker.core.domain.remote.RemoteResourcesDownloadProgress
import ru.tech.imageresizershrinker.core.domain.remote.RemoteResourcesStore
import ru.tech.imageresizershrinker.core.ui.utils.BaseComponent
import ru.tech.imageresizershrinker.core.ui.utils.navigation.Screen

class MeshGradientsComponent @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    private val shareProvider: ShareProvider<Bitmap>,
    private val imageGetter: ImageGetter<Bitmap>,
    dispatchersHolder: DispatchersHolder,
    remoteResourcesStore: RemoteResourcesStore
) : BaseComponent(dispatchersHolder, componentContext) {

    private val _meshGradientUris = mutableStateOf(emptyList<Uri>())
    val meshGradientUris by _meshGradientUris

    private val _meshGradientDownloadProgress: MutableState<RemoteResourcesDownloadProgress?> =
        mutableStateOf(null)
    val meshGradientDownloadProgress by _meshGradientDownloadProgress

    init {
        componentScope.launch {
            delay(200)
            val resources = remoteResourcesStore
                .getResources(
                    name = RemoteResources.MESH_GRADIENTS,
                    forceUpdate = true,
                    onDownloadRequest = {
                        remoteResourcesStore.downloadResources(
                            name = RemoteResources.MESH_GRADIENTS,
                            onProgress = { _meshGradientDownloadProgress.value = it },
                            onFailure = {},
                            downloadOnlyNewData = true
                        )
                    }
                )

            _meshGradientUris.value = resources?.list?.map { it.uri.toUri() } ?: emptyList()
        }
    }

    fun shareImages(
        uriList: List<Uri>,
        onComplete: () -> Unit
    ) = componentScope.launch {
        shareProvider.shareImages(
            uris = uriList.map { it.toString() },
            imageLoader = {
                imageGetter.getImage(it)?.run { image to imageInfo }
            },
            onProgressChange = {}
        )
        onComplete()
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit
        ): MeshGradientsComponent
    }

}