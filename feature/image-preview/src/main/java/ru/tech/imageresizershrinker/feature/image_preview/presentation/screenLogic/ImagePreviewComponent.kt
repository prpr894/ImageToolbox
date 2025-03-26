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

package ru.tech.imageresizershrinker.feature.image_preview.presentation.screenLogic

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import com.arkivanov.decompose.ComponentContext
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import ru.tech.imageresizershrinker.core.domain.dispatchers.DispatchersHolder
import ru.tech.imageresizershrinker.core.domain.image.ImageGetter
import ru.tech.imageresizershrinker.core.domain.image.ShareProvider
import ru.tech.imageresizershrinker.core.domain.image.model.ImageFrames
import ru.tech.imageresizershrinker.core.domain.saving.FileController
import ru.tech.imageresizershrinker.core.domain.utils.smartJob
import ru.tech.imageresizershrinker.core.ui.utils.BaseComponent
import ru.tech.imageresizershrinker.core.ui.utils.navigation.Screen
import ru.tech.imageresizershrinker.core.ui.utils.state.update

class ImagePreviewComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val initialUris: List<Uri>?,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    private val shareProvider: ShareProvider<Bitmap>,
    private val imageGetter: ImageGetter<Bitmap, ExifInterface>,
    private val fileController: FileController,
    dispatchersHolder: DispatchersHolder
) : BaseComponent(dispatchersHolder, componentContext) {

    init {
        debounce {
            initialUris?.let(::updateUris)
        }
    }

    private val _isLoadingImages = mutableStateOf(false)
    val isLoadingImages by _isLoadingImages

    private val _uris = mutableStateOf<List<Uri>?>(null)
    val uris by _uris

    private val _imageFrames: MutableState<ImageFrames> = mutableStateOf(
        ImageFrames.ManualSelection(
            emptyList()
        )
    )
    val imageFrames by _imageFrames

    fun updateUris(uris: List<Uri>?) {
        _uris.value = null
        _uris.value = uris
    }

    fun shareImages(
        uriList: List<Uri>?,
        onComplete: () -> Unit
    ) = componentScope.launch {
        uris?.let {
            shareProvider.shareUris(
                if (uriList.isNullOrEmpty()) {
                    getSelectedUris()!!
                } else {
                    uriList
                }.map { it.toString() }
            )
            onComplete()
        }
    }

    fun getSelectedUris(): List<Uri>? {
        val targetUris = uris ?: return null

        val positions = imageFrames.getFramePositions(targetUris.size)

        return targetUris.mapIndexedNotNull { index, uri ->
            if (index + 1 in positions) uri
            else null
        }
    }

    fun removeUri(
        uri: Uri
    ) = _uris.update { it?.minus(uri) }

    fun updateImageFrames(imageFrames: ImageFrames) {
        _imageFrames.update { imageFrames }
    }

    private var treeJob: Job? by smartJob {
        _isLoadingImages.update { false }
    }

    fun updateUrisFromTree(uri: Uri) {
        treeJob = componentScope.launch {
            _isLoadingImages.update { true }
            fileController.listFilesInDirectory(uri.toString()).mapNotNull { uri ->
                val excluded = listOf(
                    "xml", "mov", "zip", "apk", "mp4", "mp3", "pdf", "ldb", "ttf", "gz", "rar"
                )
                if (excluded.any { uri.endsWith(".$it", true) }) return@mapNotNull null

                if (imageGetter.getImage(uri, 10) != null) uri.toUri()
                else null
            }.let(::updateUris)
            _isLoadingImages.update { false }
        }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            initialUris: List<Uri>?,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): ImagePreviewComponent
    }

}