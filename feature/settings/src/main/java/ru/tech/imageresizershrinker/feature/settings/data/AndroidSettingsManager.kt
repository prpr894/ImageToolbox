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

package ru.tech.imageresizershrinker.feature.settings.data

import android.content.Context
import android.graphics.Typeface
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.t8rin.logger.Logger
import com.t8rin.logger.makeLog
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.tech.imageresizershrinker.core.data.utils.getFilename
import ru.tech.imageresizershrinker.core.data.utils.isInstalledFromPlayStore
import ru.tech.imageresizershrinker.core.domain.BackupFileExtension
import ru.tech.imageresizershrinker.core.domain.GlobalStorageName
import ru.tech.imageresizershrinker.core.domain.dispatchers.DispatchersHolder
import ru.tech.imageresizershrinker.core.domain.image.model.ImageScaleMode
import ru.tech.imageresizershrinker.core.domain.image.model.ResizeType
import ru.tech.imageresizershrinker.core.domain.model.ColorModel
import ru.tech.imageresizershrinker.core.domain.model.HashingType
import ru.tech.imageresizershrinker.core.domain.model.PerformanceClass
import ru.tech.imageresizershrinker.core.domain.model.SystemBarsVisibility
import ru.tech.imageresizershrinker.core.domain.utils.timestamp
import ru.tech.imageresizershrinker.core.settings.domain.SettingsManager
import ru.tech.imageresizershrinker.core.settings.domain.model.ColorHarmonizer
import ru.tech.imageresizershrinker.core.settings.domain.model.CopyToClipboardMode
import ru.tech.imageresizershrinker.core.settings.domain.model.DomainFontFamily
import ru.tech.imageresizershrinker.core.settings.domain.model.FastSettingsSide
import ru.tech.imageresizershrinker.core.settings.domain.model.NightMode
import ru.tech.imageresizershrinker.core.settings.domain.model.OneTimeSaveLocation
import ru.tech.imageresizershrinker.core.settings.domain.model.SettingsState
import ru.tech.imageresizershrinker.core.settings.domain.model.SliderType
import ru.tech.imageresizershrinker.core.settings.domain.model.SwitchType
import ru.tech.imageresizershrinker.feature.settings.data.keys.ADD_ORIGINAL_NAME_TO_FILENAME
import ru.tech.imageresizershrinker.feature.settings.data.keys.ADD_SEQ_NUM_TO_FILENAME
import ru.tech.imageresizershrinker.feature.settings.data.keys.ADD_SIZE_TO_FILENAME
import ru.tech.imageresizershrinker.feature.settings.data.keys.ADD_TIMESTAMP_TO_FILENAME
import ru.tech.imageresizershrinker.feature.settings.data.keys.ALLOW_ANALYTICS
import ru.tech.imageresizershrinker.feature.settings.data.keys.ALLOW_AUTO_PASTE
import ru.tech.imageresizershrinker.feature.settings.data.keys.ALLOW_BETAS
import ru.tech.imageresizershrinker.feature.settings.data.keys.ALLOW_CRASHLYTICS
import ru.tech.imageresizershrinker.feature.settings.data.keys.ALLOW_IMAGE_MONET
import ru.tech.imageresizershrinker.feature.settings.data.keys.AMOLED_MODE
import ru.tech.imageresizershrinker.feature.settings.data.keys.APP_COLOR_TUPLE
import ru.tech.imageresizershrinker.feature.settings.data.keys.APP_OPEN_COUNT
import ru.tech.imageresizershrinker.feature.settings.data.keys.AUTO_CACHE_CLEAR
import ru.tech.imageresizershrinker.feature.settings.data.keys.BACKGROUND_COLOR_FOR_NA_FORMATS
import ru.tech.imageresizershrinker.feature.settings.data.keys.BORDER_WIDTH
import ru.tech.imageresizershrinker.feature.settings.data.keys.CAN_ENTER_PRESETS_BY_TEXT_FIELD
import ru.tech.imageresizershrinker.feature.settings.data.keys.CENTER_ALIGN_DIALOG_BUTTONS
import ru.tech.imageresizershrinker.feature.settings.data.keys.CHECKSUM_TYPE_FOR_FILENAME
import ru.tech.imageresizershrinker.feature.settings.data.keys.COLOR_BLIND_TYPE
import ru.tech.imageresizershrinker.feature.settings.data.keys.COLOR_TUPLES
import ru.tech.imageresizershrinker.feature.settings.data.keys.CONFETTI_ENABLED
import ru.tech.imageresizershrinker.feature.settings.data.keys.CONFETTI_HARMONIZATION_LEVEL
import ru.tech.imageresizershrinker.feature.settings.data.keys.CONFETTI_HARMONIZER
import ru.tech.imageresizershrinker.feature.settings.data.keys.CONFETTI_TYPE
import ru.tech.imageresizershrinker.feature.settings.data.keys.COPY_TO_CLIPBOARD_MODE
import ru.tech.imageresizershrinker.feature.settings.data.keys.CUSTOM_FONTS
import ru.tech.imageresizershrinker.feature.settings.data.keys.DEFAULT_DRAW_COLOR
import ru.tech.imageresizershrinker.feature.settings.data.keys.DEFAULT_DRAW_LINE_WIDTH
import ru.tech.imageresizershrinker.feature.settings.data.keys.DEFAULT_DRAW_PATH_MODE
import ru.tech.imageresizershrinker.feature.settings.data.keys.DEFAULT_RESIZE_TYPE
import ru.tech.imageresizershrinker.feature.settings.data.keys.DONATE_DIALOG_OPEN_COUNT
import ru.tech.imageresizershrinker.feature.settings.data.keys.DRAG_HANDLE_WIDTH
import ru.tech.imageresizershrinker.feature.settings.data.keys.DRAW_APPBAR_SHADOWS
import ru.tech.imageresizershrinker.feature.settings.data.keys.DRAW_BUTTON_SHADOWS
import ru.tech.imageresizershrinker.feature.settings.data.keys.DRAW_CONTAINER_SHADOWS
import ru.tech.imageresizershrinker.feature.settings.data.keys.DRAW_FAB_SHADOWS
import ru.tech.imageresizershrinker.feature.settings.data.keys.DRAW_SLIDER_SHADOWS
import ru.tech.imageresizershrinker.feature.settings.data.keys.DRAW_SWITCH_SHADOWS
import ru.tech.imageresizershrinker.feature.settings.data.keys.DYNAMIC_COLORS
import ru.tech.imageresizershrinker.feature.settings.data.keys.EMOJI_COUNT
import ru.tech.imageresizershrinker.feature.settings.data.keys.ENABLE_TOOL_EXIT_CONFIRMATION
import ru.tech.imageresizershrinker.feature.settings.data.keys.EXIF_WIDGET_INITIAL_STATE
import ru.tech.imageresizershrinker.feature.settings.data.keys.FAB_ALIGNMENT
import ru.tech.imageresizershrinker.feature.settings.data.keys.FAST_SETTINGS_SIDE
import ru.tech.imageresizershrinker.feature.settings.data.keys.FAVORITE_COLORS
import ru.tech.imageresizershrinker.feature.settings.data.keys.FAVORITE_SCREENS
import ru.tech.imageresizershrinker.feature.settings.data.keys.FILENAME_PREFIX
import ru.tech.imageresizershrinker.feature.settings.data.keys.FILENAME_SUFFIX
import ru.tech.imageresizershrinker.feature.settings.data.keys.FONT_SCALE
import ru.tech.imageresizershrinker.feature.settings.data.keys.GENERATE_PREVIEWS
import ru.tech.imageresizershrinker.feature.settings.data.keys.GROUP_OPTIONS_BY_TYPE
import ru.tech.imageresizershrinker.feature.settings.data.keys.ICON_SHAPE
import ru.tech.imageresizershrinker.feature.settings.data.keys.IMAGE_PICKER_MODE
import ru.tech.imageresizershrinker.feature.settings.data.keys.IMAGE_SCALE_MODE
import ru.tech.imageresizershrinker.feature.settings.data.keys.INITIAL_OCR_CODES
import ru.tech.imageresizershrinker.feature.settings.data.keys.INITIAL_OCR_MODE
import ru.tech.imageresizershrinker.feature.settings.data.keys.INVERT_THEME
import ru.tech.imageresizershrinker.feature.settings.data.keys.IS_LINK_PREVIEW_ENABLED
import ru.tech.imageresizershrinker.feature.settings.data.keys.IS_SYSTEM_BARS_VISIBLE_BY_SWIPE
import ru.tech.imageresizershrinker.feature.settings.data.keys.IS_TELEGRAM_GROUP_OPENED
import ru.tech.imageresizershrinker.feature.settings.data.keys.LOCK_DRAW_ORIENTATION
import ru.tech.imageresizershrinker.feature.settings.data.keys.MAGNIFIER_ENABLED
import ru.tech.imageresizershrinker.feature.settings.data.keys.MAIN_SCREEN_TITLE
import ru.tech.imageresizershrinker.feature.settings.data.keys.NIGHT_MODE
import ru.tech.imageresizershrinker.feature.settings.data.keys.ONE_TIME_SAVE_LOCATIONS
import ru.tech.imageresizershrinker.feature.settings.data.keys.OPEN_EDIT_INSTEAD_OF_PREVIEW
import ru.tech.imageresizershrinker.feature.settings.data.keys.OVERWRITE_FILE
import ru.tech.imageresizershrinker.feature.settings.data.keys.PRESETS
import ru.tech.imageresizershrinker.feature.settings.data.keys.RANDOMIZE_FILENAME
import ru.tech.imageresizershrinker.feature.settings.data.keys.RECENT_COLORS
import ru.tech.imageresizershrinker.feature.settings.data.keys.SAVE_FOLDER_URI
import ru.tech.imageresizershrinker.feature.settings.data.keys.SCREENS_WITH_BRIGHTNESS_ENFORCEMENT
import ru.tech.imageresizershrinker.feature.settings.data.keys.SCREEN_ORDER
import ru.tech.imageresizershrinker.feature.settings.data.keys.SCREEN_SEARCH_ENABLED
import ru.tech.imageresizershrinker.feature.settings.data.keys.SECURE_MODE
import ru.tech.imageresizershrinker.feature.settings.data.keys.SELECTED_EMOJI_INDEX
import ru.tech.imageresizershrinker.feature.settings.data.keys.SELECTED_FONT
import ru.tech.imageresizershrinker.feature.settings.data.keys.SETTINGS_GROUP_VISIBILITY
import ru.tech.imageresizershrinker.feature.settings.data.keys.SHOW_SETTINGS_IN_LANDSCAPE
import ru.tech.imageresizershrinker.feature.settings.data.keys.SHOW_UPDATE_DIALOG
import ru.tech.imageresizershrinker.feature.settings.data.keys.SKIP_IMAGE_PICKING
import ru.tech.imageresizershrinker.feature.settings.data.keys.SLIDER_TYPE
import ru.tech.imageresizershrinker.feature.settings.data.keys.SWITCH_TYPE
import ru.tech.imageresizershrinker.feature.settings.data.keys.SYSTEM_BARS_VISIBILITY
import ru.tech.imageresizershrinker.feature.settings.data.keys.THEME_CONTRAST_LEVEL
import ru.tech.imageresizershrinker.feature.settings.data.keys.THEME_STYLE
import ru.tech.imageresizershrinker.feature.settings.data.keys.USE_COMPACT_SELECTORS_LAYOUT
import ru.tech.imageresizershrinker.feature.settings.data.keys.USE_EMOJI_AS_PRIMARY_COLOR
import ru.tech.imageresizershrinker.feature.settings.data.keys.USE_FORMATTED_TIMESTAMP
import ru.tech.imageresizershrinker.feature.settings.data.keys.USE_FULLSCREEN_SETTINGS
import ru.tech.imageresizershrinker.feature.settings.data.keys.USE_RANDOM_EMOJIS
import ru.tech.imageresizershrinker.feature.settings.data.keys.VIBRATION_STRENGTH
import ru.tech.imageresizershrinker.feature.settings.data.keys.toSettingsState
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import kotlin.random.Random

internal class AndroidSettingsManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>,
    dispatchersHolder: DispatchersHolder,
) : DispatchersHolder by dispatchersHolder, SettingsManager {

    init {
        CoroutineScope(ioDispatcher).launch {
            registerAppOpen()
        }
    }

    private val default = SettingsState.Default
    private var currentSettings: SettingsState = default

    override suspend fun getSettingsState(): SettingsState = getSettingsStateFlow().first()

    override fun getSettingsStateFlow(): Flow<SettingsState> = dataStore.data.map {
        it.toSettingsState(default)
    }.onEach { currentSettings = it }

    override fun getNeedToShowTelegramGroupDialog(): Flow<Boolean> = getSettingsStateFlow().map {
        it.appOpenCount % 6 == 0 && it.appOpenCount != 0 && (dataStore.data.first()[IS_TELEGRAM_GROUP_OPENED] != true)
    }

    override suspend fun toggleAddSequenceNumber() = toggle(
        key = ADD_SEQ_NUM_TO_FILENAME,
        defaultValue = default.addSequenceNumber
    )

    override suspend fun toggleAddOriginalFilename() = toggle(
        key = ADD_ORIGINAL_NAME_TO_FILENAME,
        defaultValue = default.addOriginalFilename
    )

    override suspend fun setEmojisCount(count: Int) = edit {
        it[EMOJI_COUNT] = count
    }

    override suspend fun setImagePickerMode(mode: Int) = edit {
        it[IMAGE_PICKER_MODE] = mode
    }

    override suspend fun toggleAddFileSize() = toggle(
        key = ADD_SIZE_TO_FILENAME,
        defaultValue = default.addSizeInFilename
    )

    override suspend fun setEmoji(emoji: Int) = edit {
        it[SELECTED_EMOJI_INDEX] = emoji
    }

    override suspend fun setFilenamePrefix(name: String) = edit {
        it[FILENAME_PREFIX] = name
    }

    override suspend fun toggleShowUpdateDialogOnStartup() = toggle(
        key = SHOW_UPDATE_DIALOG,
        defaultValue = default.showUpdateDialogOnStartup
    )


    override suspend fun setColorTuple(colorTuple: String) = edit {
        it[APP_COLOR_TUPLE] = colorTuple
    }

    override suspend fun setPresets(newPresets: List<Int>) = edit {
        if (newPresets.size > 3) {
            it[PRESETS] = newPresets
                .map { it.coerceIn(10..500) }
                .toSortedSet()
                .toList()
                .reversed()
                .joinToString("*")
        }
    }

    override suspend fun toggleDynamicColors() = edit {
        it.toggle(
            key = DYNAMIC_COLORS,
            defaultValue = default.isDynamicColors
        )
        if (it[DYNAMIC_COLORS] == true) {
            it[ALLOW_IMAGE_MONET] = false
        }
    }

    override suspend fun setBorderWidth(width: Float) = edit {
        it[BORDER_WIDTH] = if (width > 0) width else -1f
    }

    override suspend fun toggleAllowImageMonet() = toggle(
        key = ALLOW_IMAGE_MONET,
        defaultValue = default.allowChangeColorByImage
    )

    override suspend fun toggleAmoledMode() = toggle(
        key = AMOLED_MODE,
        defaultValue = default.isAmoledMode
    )

    override suspend fun setNightMode(nightMode: NightMode) = edit {
        it[NIGHT_MODE] = nightMode.ordinal
    }

    override suspend fun setSaveFolderUri(uri: String?) = edit {
        it[SAVE_FOLDER_URI] = uri ?: ""
    }

    override suspend fun setColorTuples(colorTuples: String) = edit {
        it[COLOR_TUPLES] = colorTuples
    }

    override suspend fun setAlignment(align: Int) = edit {
        it[FAB_ALIGNMENT] = align
    }

    override suspend fun setScreenOrder(data: String) = edit {
        it[SCREEN_ORDER] = data
    }

    override suspend fun toggleClearCacheOnLaunch() = toggle(
        key = AUTO_CACHE_CLEAR,
        defaultValue = default.clearCacheOnLaunch
    )

    override suspend fun toggleGroupOptionsByTypes() = toggle(
        key = GROUP_OPTIONS_BY_TYPE,
        defaultValue = default.groupOptionsByTypes
    )

    override suspend fun toggleRandomizeFilename() = toggle(
        key = RANDOMIZE_FILENAME,
        defaultValue = default.randomizeFilename
    )

    override suspend fun createBackupFile(): ByteArray =
        context.obtainDatastoreData(GlobalStorageName)

    override suspend fun restoreFromBackupFile(
        backupFileUri: String,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit,
    ) = withContext(ioDispatcher) {
        context.restoreDatastore(
            fileName = GlobalStorageName,
            backupUri = backupFileUri.toUri(),
            onFailure = onFailure,
            onSuccess = {
                onSuccess()
                setSaveFolderUri(null)
            }
        )
        toggleClearCacheOnLaunch()
        toggleClearCacheOnLaunch()
    }

    override suspend fun resetSettings() = withContext(defaultDispatcher) {
        context.resetDatastore(GlobalStorageName)
        registerAppOpen()
    }

    override fun createBackupFilename(): String =
        "image_toolbox_${timestamp()}.$BackupFileExtension"

    override suspend fun setFont(font: DomainFontFamily) = edit {
        it[SELECTED_FONT] = font.asString()
    }

    override suspend fun setFontScale(scale: Float) = edit {
        it[FONT_SCALE] = scale
    }

    override suspend fun toggleAllowCrashlytics() = toggle(
        key = ALLOW_CRASHLYTICS,
        defaultValue = default.allowCollectCrashlytics
    )

    override suspend fun toggleAllowAnalytics() = toggle(
        key = ALLOW_ANALYTICS,
        defaultValue = default.allowCollectAnalytics
    )

    override suspend fun toggleAllowBetas() = toggle(
        key = ALLOW_BETAS,
        defaultValue = default.allowBetas
    )

    override suspend fun toggleDrawContainerShadows() = toggle(
        key = DRAW_CONTAINER_SHADOWS,
        defaultValue = default.drawContainerShadows
    )

    override suspend fun toggleDrawButtonShadows() = toggle(
        key = DRAW_BUTTON_SHADOWS,
        defaultValue = default.drawButtonShadows
    )

    override suspend fun toggleDrawSliderShadows() = toggle(
        key = DRAW_SLIDER_SHADOWS,
        defaultValue = default.drawSliderShadows
    )

    override suspend fun toggleDrawSwitchShadows() = toggle(
        key = DRAW_SWITCH_SHADOWS,
        defaultValue = default.drawSwitchShadows
    )

    override suspend fun toggleDrawFabShadows() = toggle(
        key = DRAW_FAB_SHADOWS,
        defaultValue = default.drawFabShadows
    )

    private suspend fun registerAppOpen() = edit {
        val v = it[APP_OPEN_COUNT] ?: default.appOpenCount
        it[APP_OPEN_COUNT] = v + 1
    }

    override suspend fun toggleLockDrawOrientation() = toggle(
        key = LOCK_DRAW_ORIENTATION,
        defaultValue = default.lockDrawOrientation
    )

    override suspend fun setThemeStyle(value: Int) = edit {
        it[THEME_STYLE] = value
    }

    override suspend fun setThemeContrast(value: Double) = edit {
        it[THEME_CONTRAST_LEVEL] = value
    }

    override suspend fun toggleInvertColors() = toggle(
        key = INVERT_THEME,
        defaultValue = default.isInvertThemeColors
    )

    override suspend fun toggleScreensSearchEnabled() = toggle(
        key = SCREEN_SEARCH_ENABLED,
        defaultValue = default.screensSearchEnabled
    )

    override suspend fun toggleDrawAppBarShadows() = toggle(
        key = DRAW_APPBAR_SHADOWS,
        defaultValue = default.drawAppBarShadows
    )

    override suspend fun setCopyToClipboardMode(
        copyToClipboardMode: CopyToClipboardMode
    ) = edit {
        it[COPY_TO_CLIPBOARD_MODE] = copyToClipboardMode.value
    }

    override suspend fun setVibrationStrength(strength: Int) = edit {
        it[VIBRATION_STRENGTH] = strength
    }

    override suspend fun toggleOverwriteFiles() = edit {
        it.toggle(
            key = OVERWRITE_FILE,
            defaultValue = default.overwriteFiles
        )

        it[IMAGE_PICKER_MODE] = 2
    }

    override suspend fun setFilenameSuffix(name: String) = edit {
        it[FILENAME_SUFFIX] = name
    }

    override suspend fun setDefaultImageScaleMode(imageScaleMode: ImageScaleMode) = edit {
        it[IMAGE_SCALE_MODE] = imageScaleMode.value
    }

    override suspend fun toggleMagnifierEnabled() = toggle(
        key = MAGNIFIER_ENABLED,
        defaultValue = default.magnifierEnabled
    )

    override suspend fun toggleExifWidgetInitialState() = toggle(
        key = EXIF_WIDGET_INITIAL_STATE,
        defaultValue = default.exifWidgetInitialState
    )

    override suspend fun setInitialOCRLanguageCodes(list: List<String>) = edit {
        it[INITIAL_OCR_CODES] = list.joinToString(separator = "+")
    }

    override suspend fun getInitialOCRLanguageCodes(): List<String> = dataStore.data.first().let {
        it[INITIAL_OCR_CODES]?.split("+") ?: default.initialOcrCodes
    }

    override suspend fun getInitialOcrMode(): Int = dataStore.data.first().let {
        it[INITIAL_OCR_MODE] ?: 1
    }

    override suspend fun createLogsExport(): ByteArray = withContext(ioDispatcher) {
        "Start Logs Export".makeLog("SettingsManager")

        val logsFile = Logger.getLogsFile().toFile()
        val settingsFile = createBackupFile()

        val out = ByteArrayOutputStream()

        ZipOutputStream(out).use { zipOut ->
            FileInputStream(logsFile).use { fis ->
                val zipEntry = ZipEntry(logsFile.name)
                zipOut.putNextEntry(zipEntry)
                fis.copyTo(zipOut)
                zipOut.closeEntry()
            }
            ByteArrayInputStream(settingsFile).use { bis ->
                val zipEntry = ZipEntry(createBackupFilename())
                zipOut.putNextEntry(zipEntry)
                bis.copyTo(zipOut)
                zipOut.closeEntry()
            }
        }

        out.toByteArray()
    }

    override fun createLogsFilename(): String = "image_toolbox_logs_${timestamp()}.zip"

    override suspend fun setScreensWithBrightnessEnforcement(data: String) = edit {
        it[SCREENS_WITH_BRIGHTNESS_ENFORCEMENT] = data
    }

    override suspend fun toggleConfettiEnabled() = toggle(
        key = CONFETTI_ENABLED,
        defaultValue = default.isConfettiEnabled
    )

    override suspend fun toggleSecureMode() = toggle(
        key = SECURE_MODE,
        defaultValue = default.isSecureMode
    )

    override suspend fun toggleUseRandomEmojis() = toggle(
        key = USE_RANDOM_EMOJIS,
        defaultValue = default.useRandomEmojis
    )

    override suspend fun setIconShape(iconShape: Int) = edit {
        it[ICON_SHAPE] = iconShape
    }

    override suspend fun toggleUseEmojiAsPrimaryColor() = toggle(
        key = USE_EMOJI_AS_PRIMARY_COLOR,
        defaultValue = default.useEmojiAsPrimaryColor
    )

    override suspend fun setDragHandleWidth(width: Int) = edit {
        it[DRAG_HANDLE_WIDTH] = width
    }

    override suspend fun setConfettiType(type: Int) = edit {
        it[CONFETTI_TYPE] = type
    }

    override suspend fun toggleAllowAutoClipboardPaste() = toggle(
        key = ALLOW_AUTO_PASTE,
        defaultValue = default.allowAutoClipboardPaste
    )

    override suspend fun setConfettiHarmonizer(colorHarmonizer: ColorHarmonizer) = edit {
        it[CONFETTI_HARMONIZER] = colorHarmonizer.ordinal
    }

    override suspend fun setConfettiHarmonizationLevel(level: Float) = edit {
        it[CONFETTI_HARMONIZATION_LEVEL] = level
    }

    override suspend fun toggleGeneratePreviews() = toggle(
        key = GENERATE_PREVIEWS,
        defaultValue = default.generatePreviews
    )

    override suspend fun toggleSkipImagePicking() = toggle(
        key = SKIP_IMAGE_PICKING,
        defaultValue = default.skipImagePicking
    )

    override suspend fun toggleShowSettingsInLandscape() = toggle(
        key = SHOW_SETTINGS_IN_LANDSCAPE,
        defaultValue = default.showSettingsInLandscape
    )

    override suspend fun toggleUseFullscreenSettings() = toggle(
        key = USE_FULLSCREEN_SETTINGS,
        defaultValue = default.useFullscreenSettings
    )

    override suspend fun setSwitchType(type: SwitchType) = edit {
        it[SWITCH_TYPE] = type.ordinal
    }

    override suspend fun setDefaultDrawLineWidth(value: Float) = edit {
        it[DEFAULT_DRAW_LINE_WIDTH] = value
    }

    override suspend fun setOneTimeSaveLocations(
        value: List<OneTimeSaveLocation>
    ) = edit {
        it[ONE_TIME_SAVE_LOCATIONS] = value.filter {
            it.uri.isNotEmpty() && it.date != null
        }.distinctBy { it.uri }.joinToString(", ")
    }

    override suspend fun toggleRecentColor(
        color: ColorModel,
        forceExclude: Boolean,
    ) = edit {
        val current = currentSettings.recentColors
        val newColors = if (color in current) {
            if (forceExclude) {
                current - color
            } else {
                listOf(color) + (current - color)
            }
        } else {
            listOf(color) + current
        }

        it[RECENT_COLORS] = newColors.take(30).map { it.colorInt.toString() }.toSet()
    }

    override suspend fun toggleFavoriteColor(
        color: ColorModel,
        forceExclude: Boolean
    ) = edit {
        val current = currentSettings.favoriteColors
        val newColors = if (color in current) {
            if (forceExclude) {
                current - color
            } else {
                listOf(color) + (current - color)
            }
        } else {
            listOf(color) + current
        }

        it[FAVORITE_COLORS] = newColors.map { it.colorInt.toString() }.joinToString("/")
    }

    override suspend fun toggleOpenEditInsteadOfPreview() = toggle(
        key = OPEN_EDIT_INSTEAD_OF_PREVIEW,
        defaultValue = default.openEditInsteadOfPreview
    )

    override suspend fun toggleCanEnterPresetsByTextField() = toggle(
        key = CAN_ENTER_PRESETS_BY_TEXT_FIELD,
        defaultValue = default.canEnterPresetsByTextField
    )

    override suspend fun adjustPerformance(performanceClass: PerformanceClass) = edit {
        when (performanceClass) {
            PerformanceClass.Low -> {
                it[CONFETTI_ENABLED] = false
                it[DRAW_BUTTON_SHADOWS] = false
                it[DRAW_SWITCH_SHADOWS] = false
                it[DRAW_SLIDER_SHADOWS] = false
                it[DRAW_CONTAINER_SHADOWS] = false
                it[DRAW_APPBAR_SHADOWS] = false
            }

            PerformanceClass.Average -> {
                it[CONFETTI_ENABLED] = true
                it[DRAW_BUTTON_SHADOWS] = false
                it[DRAW_SWITCH_SHADOWS] = true
                it[DRAW_SLIDER_SHADOWS] = false
                it[DRAW_CONTAINER_SHADOWS] = false
                it[DRAW_APPBAR_SHADOWS] = true
            }

            PerformanceClass.High -> {
                it[CONFETTI_ENABLED] = true
                it[DRAW_BUTTON_SHADOWS] = true
                it[DRAW_SWITCH_SHADOWS] = true
                it[DRAW_SLIDER_SHADOWS] = true
                it[DRAW_CONTAINER_SHADOWS] = true
                it[DRAW_APPBAR_SHADOWS] = true
            }
        }
    }

    override suspend fun registerDonateDialogOpen() = edit {
        val value = it[DONATE_DIALOG_OPEN_COUNT] ?: default.donateDialogOpenCount

        if (value != -1) {
            it[DONATE_DIALOG_OPEN_COUNT] = value + 1
        }
    }

    override suspend fun setNotShowDonateDialogAgain() = edit {
        it[DONATE_DIALOG_OPEN_COUNT] = -1
    }

    override suspend fun setColorBlindType(value: Int?) = edit {
        it[COLOR_BLIND_TYPE] = value ?: -1
    }

    override suspend fun toggleFavoriteScreen(screenId: Int) = edit {
        val current = currentSettings.favoriteScreenList
        val newScreens = if (screenId in current) {
            current - screenId
        } else {
            current + screenId
        }

        it[FAVORITE_SCREENS] = newScreens.joinToString("/") { it.toString() }
    }

    override suspend fun toggleIsLinkPreviewEnabled() = toggle(
        key = IS_LINK_PREVIEW_ENABLED,
        defaultValue = default.isLinkPreviewEnabled
    )

    override suspend fun setDefaultDrawColor(color: ColorModel) = edit {
        it[DEFAULT_DRAW_COLOR] = color.colorInt
    }

    override suspend fun setDefaultDrawPathMode(modeOrdinal: Int) = edit {
        it[DEFAULT_DRAW_PATH_MODE] = modeOrdinal
    }

    override suspend fun toggleAddTimestampToFilename() = toggle(
        key = ADD_TIMESTAMP_TO_FILENAME,
        defaultValue = default.addTimestampToFilename
    )

    override suspend fun toggleUseFormattedFilenameTimestamp() = toggle(
        key = USE_FORMATTED_TIMESTAMP,
        defaultValue = default.useFormattedFilenameTimestamp
    )

    override suspend fun registerTelegramGroupOpen() = edit {
        it[IS_TELEGRAM_GROUP_OPENED] = true
    }

    override suspend fun setDefaultResizeType(resizeType: ResizeType) = edit {
        it[DEFAULT_RESIZE_TYPE] = ResizeType.entries.indexOfFirst {
            it::class.isInstance(resizeType)
        }
    }

    override suspend fun setSystemBarsVisibility(
        systemBarsVisibility: SystemBarsVisibility
    ) = edit {
        it[SYSTEM_BARS_VISIBILITY] = systemBarsVisibility.ordinal
    }

    override suspend fun toggleIsSystemBarsVisibleBySwipe() = toggle(
        key = IS_SYSTEM_BARS_VISIBLE_BY_SWIPE,
        defaultValue = default.isSystemBarsVisibleBySwipe
    )

    override suspend fun setInitialOcrMode(mode: Int) = edit {
        it[INITIAL_OCR_MODE] = mode
    }

    override suspend fun toggleUseCompactSelectorsLayout() = toggle(
        key = USE_COMPACT_SELECTORS_LAYOUT,
        defaultValue = default.isCompactSelectorsLayout
    )

    override suspend fun setMainScreenTitle(title: String) = edit {
        it[MAIN_SCREEN_TITLE] = title
    }

    override suspend fun setSliderType(type: SliderType) = edit {
        it[SLIDER_TYPE] = type.ordinal
    }

    override suspend fun toggleIsCenterAlignDialogButtons() = toggle(
        key = CENTER_ALIGN_DIALOG_BUTTONS,
        defaultValue = default.isCenterAlignDialogButtons
    )

    override fun isInstalledFromPlayStore(): Boolean = context.isInstalledFromPlayStore()

    override suspend fun toggleSettingsGroupVisibility(
        key: Int,
        value: Boolean
    ) = edit {
        it[SETTINGS_GROUP_VISIBILITY] =
            currentSettings.settingGroupsInitialVisibility.toMutableMap().run {
                this[key] = value
                map {
                    "${it.key}:${it.value}"
                }.toSet()
            }
    }

    override suspend fun clearRecentColors() = edit {
        it[RECENT_COLORS] = emptySet()
    }

    override suspend fun updateFavoriteColors(
        colors: List<ColorModel>
    ) = edit {
        it[FAVORITE_COLORS] = colors.map { it.colorInt.toString() }.joinToString("/")
    }

    override suspend fun setBackgroundColorForNoAlphaFormats(
        color: ColorModel
    ) = edit {
        it[BACKGROUND_COLOR_FOR_NA_FORMATS] = color.colorInt
    }

    override suspend fun setFastSettingsSide(side: FastSettingsSide) = edit {
        it[FAST_SETTINGS_SIDE] = side.ordinal
    }

    override suspend fun setChecksumTypeForFilename(type: HashingType?) = edit {
        it[CHECKSUM_TYPE_FOR_FILENAME] = type?.digest ?: ""
    }

    override suspend fun setCustomFonts(fonts: List<DomainFontFamily.Custom>) = edit {
        it[CUSTOM_FONTS] = fonts.map(DomainFontFamily::asString).toSet()
    }

    override suspend fun importCustomFont(
        uri: String
    ): DomainFontFamily.Custom? = withContext(ioDispatcher) {
        val font = context.contentResolver.openInputStream(uri.toUri())?.use {
            it.buffered().readBytes()
        } ?: ByteArray(0)
        val filename = uri.toUri().getFilename(context) ?: "font${Random.nextInt()}.ttf"

        val directory = File(context.filesDir, "customFonts").apply {
            mkdir()
        }
        val file = File(directory, filename).apply {
            if (exists()) {
                val fontToRemove = DomainFontFamily.Custom(
                    name = nameWithoutExtension.replace("[:\\-_.,]".toRegex(), " "),
                    filePath = absolutePath
                )
                removeCustomFont(fontToRemove)
            }
            delete()
            createNewFile()

            outputStream().use {
                writeBytes(font)
            }
        }

        val typeface = runCatching {
            Typeface.createFromFile(file)
        }.getOrNull()

        if (typeface == null) {
            file.delete()
            return@withContext null
        }

        DomainFontFamily.Custom(
            name = file.nameWithoutExtension.replace("[:\\-_.,]".toRegex(), " "),
            filePath = file.absolutePath
        ).also {
            setCustomFonts(currentSettings.customFonts + it)
        }
    }

    override suspend fun removeCustomFont(
        font: DomainFontFamily.Custom
    ) = withContext(ioDispatcher) {
        File(font.filePath).delete()

        setCustomFonts(currentSettings.customFonts - font)
    }

    override suspend fun createCustomFontsExport(): ByteArray = withContext(ioDispatcher) {
        val out = ByteArrayOutputStream()

        ZipOutputStream(out).use { zipOut ->
            val dir = File(context.filesDir, "customFonts")
            dir.listFiles()?.forEach { file ->
                FileInputStream(file).use { fis ->
                    val zipEntry = ZipEntry(file.name)
                    zipOut.putNextEntry(zipEntry)
                    fis.copyTo(zipOut)
                    zipOut.closeEntry()
                }
            }
        }

        out.toByteArray()
    }

    override suspend fun toggleEnableToolExitConfirmation() = toggle(
        key = ENABLE_TOOL_EXIT_CONFIRMATION,
        defaultValue = default.enableToolExitConfirmation
    )

    private fun MutablePreferences.toggle(
        key: Preferences.Key<Boolean>,
        defaultValue: Boolean,
    ) {
        val value = this[key] ?: defaultValue
        this[key] = !value
    }

    suspend fun toggle(
        key: Preferences.Key<Boolean>,
        defaultValue: Boolean,
    ) = edit {
        it.toggle(
            key = key,
            defaultValue = defaultValue
        )
    }

    suspend fun edit(
        transform: suspend (MutablePreferences) -> Unit
    ) {
        dataStore.edit(transform)
    }
}