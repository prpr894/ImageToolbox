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

package ru.tech.imageresizershrinker.core.data.saving

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.tech.imageresizershrinker.core.data.utils.computeFromByteArray
import ru.tech.imageresizershrinker.core.data.utils.getFilename
import ru.tech.imageresizershrinker.core.domain.dispatchers.DispatchersHolder
import ru.tech.imageresizershrinker.core.domain.resource.ResourceManager
import ru.tech.imageresizershrinker.core.domain.saving.FilenameCreator
import ru.tech.imageresizershrinker.core.domain.saving.RandomStringGenerator
import ru.tech.imageresizershrinker.core.domain.saving.model.ImageSaveTarget
import ru.tech.imageresizershrinker.core.domain.utils.timestamp
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.settings.domain.SettingsManager
import ru.tech.imageresizershrinker.core.settings.domain.model.SettingsState
import java.util.Date
import javax.inject.Inject
import kotlin.random.Random

internal class AndroidFilenameCreator @Inject constructor(
    private val randomStringGenerator: RandomStringGenerator,
    @ApplicationContext private val context: Context,
    settingsManager: SettingsManager,
    dispatchersHolder: DispatchersHolder,
    resourceManager: ResourceManager,
) : FilenameCreator,
    DispatchersHolder by dispatchersHolder,
    ResourceManager by resourceManager {

    private var _settingsState: SettingsState = SettingsState.Default

    private val settingsState get() = _settingsState

    init {
        settingsManager
            .getSettingsStateFlow()
            .onEach { state ->
                _settingsState = state
            }.launchIn(CoroutineScope(defaultDispatcher))
    }


    override fun constructImageFilename(
        saveTarget: ImageSaveTarget,
        oneTimePrefix: String?,
        forceNotAddSizeInFilename: Boolean
    ): String {
        val extension = saveTarget.extension

        val checksumType = settingsState.hashingTypeForFilename
        if (checksumType != null && saveTarget.data.isNotEmpty()) {
            val name = checksumType.computeFromByteArray(saveTarget.data)

            if (name.isNotEmpty()) return "$name.$extension"
        }

        if (settingsState.randomizeFilename) return "${randomStringGenerator.generate(32)}.$extension"

        val wh =
            "(" + (if (saveTarget.originalUri.toUri() == Uri.EMPTY) getString(R.string.width)
                .split(" ")[0] else saveTarget.imageInfo.width) + ")x(" + (if (saveTarget.originalUri.toUri() == Uri.EMPTY) getString(
                R.string.height
            ).split(" ")[0] else saveTarget.imageInfo.height) + ")"

        var prefix = oneTimePrefix ?: settingsState.filenamePrefix
        var suffix = settingsState.filenameSuffix

        if (prefix.isNotEmpty()) prefix = "${prefix}_"
        if (suffix.isNotEmpty()) suffix = "_$suffix"

        if (settingsState.addOriginalFilename) {
            prefix += if (saveTarget.originalUri.toUri() != Uri.EMPTY) {
                saveTarget.originalUri.toUri()
                    .getFilename(context)
                    ?.dropLastWhile { it != '.' }
                    ?.removeSuffix(".") ?: ""
            } else {
                getString(R.string.original_filename)
            }
        }
        if (settingsState.addSizeInFilename && !forceNotAddSizeInFilename) prefix += wh

        val randomNumber: () -> String = {
            Random(Random.nextInt()).hashCode().toString().take(4)
        }

        val timeStamp = if (settingsState.useFormattedFilenameTimestamp) {
            "${timestamp()}_${randomNumber()}"
        } else Date().time.toString()

        var body = if (settingsState.addSequenceNumber && saveTarget.sequenceNumber != null) {
            if (settingsState.addOriginalFilename) {
                saveTarget.sequenceNumber.toString()
            } else {
                val timeStampPart = if (settingsState.addTimestampToFilename) {
                    timeStamp.dropLastWhile { it != '_' }
                } else ""

                timeStampPart + saveTarget.sequenceNumber
            }
        } else if (settingsState.addTimestampToFilename) {
            timeStamp
        } else ""

        if (body.isEmpty()) {
            if (prefix.endsWith("_")) prefix = prefix.dropLast(1)
            if (suffix.startsWith("_")) suffix = suffix.drop(1)
            if (prefix.isEmpty() && suffix.isEmpty()) body = "image${randomNumber()}"
        }

        return "$prefix$body$suffix.$extension"
    }

    override fun constructRandomFilename(
        extension: String,
        length: Int
    ): String = "${randomStringGenerator.generate(length)}.${extension}"

    override fun getFilename(uri: String): String = uri.toUri().getFilename(context) ?: ""

}