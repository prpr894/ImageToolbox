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

package ru.tech.imageresizershrinker.core.filters.presentation.widget

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.tech.imageresizershrinker.core.domain.dispatchers.DispatchersHolder
import ru.tech.imageresizershrinker.core.domain.image.ImageGetter
import ru.tech.imageresizershrinker.core.domain.model.ImageModel
import ru.tech.imageresizershrinker.core.domain.model.IntegerSize
import ru.tech.imageresizershrinker.core.filters.domain.FavoriteFiltersInteractor
import ru.tech.imageresizershrinker.core.filters.domain.FilterProvider
import ru.tech.imageresizershrinker.core.filters.domain.model.TemplateFilter
import ru.tech.imageresizershrinker.core.filters.presentation.model.UiFilter
import ru.tech.imageresizershrinker.core.filters.presentation.model.toUiFilter
import ru.tech.imageresizershrinker.core.filters.presentation.widget.addFilters.AddFiltersSheet
import ru.tech.imageresizershrinker.core.filters.presentation.widget.addFilters.AddFiltersSheetComponent
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.ui.utils.BaseComponent
import ru.tech.imageresizershrinker.core.ui.utils.helper.isPortraitOrientationAsState
import ru.tech.imageresizershrinker.core.ui.utils.helper.toImageModel
import ru.tech.imageresizershrinker.core.ui.utils.provider.LocalComponentActivity
import ru.tech.imageresizershrinker.core.ui.utils.state.update
import ru.tech.imageresizershrinker.core.ui.widget.controls.selection.ImageSelector
import ru.tech.imageresizershrinker.core.ui.widget.dialogs.ExitWithoutSavingDialog
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedBottomSheetDefaults
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedButton
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedIconButton
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedModalBottomSheet
import ru.tech.imageresizershrinker.core.ui.widget.image.ImageHeaderState
import ru.tech.imageresizershrinker.core.ui.widget.image.SimplePicture
import ru.tech.imageresizershrinker.core.ui.widget.image.imageStickyHeader
import ru.tech.imageresizershrinker.core.ui.widget.modifier.container
import ru.tech.imageresizershrinker.core.ui.widget.modifier.drawHorizontalStroke
import ru.tech.imageresizershrinker.core.ui.widget.modifier.shimmer
import ru.tech.imageresizershrinker.core.ui.widget.other.LocalToastHostState
import ru.tech.imageresizershrinker.core.ui.widget.other.showFailureToast
import ru.tech.imageresizershrinker.core.ui.widget.text.RoundedTextField
import ru.tech.imageresizershrinker.core.ui.widget.text.TitleItem
import ru.tech.imageresizershrinker.core.ui.widget.utils.rememberAvailableHeight

@Composable
internal fun FilterTemplateCreationSheet(
    component: FilterTemplateCreationSheetComponent,
    visible: Boolean,
    onDismiss: () -> Unit,
    initialTemplateFilter: TemplateFilter? = null
) {
    val previewModel = component.previewModel

    val isPortrait by isPortraitOrientationAsState()

    var showAddFilterSheet by rememberSaveable { mutableStateOf(false) }

    val context = LocalComponentActivity.current
    val toastHostState = LocalToastHostState.current
    val scope = rememberCoroutineScope()

    var showExitDialog by remember { mutableStateOf(false) }

    var showReorderSheet by rememberSaveable { mutableStateOf(false) }

    val canSave = component.filterList.isNotEmpty()

    EnhancedModalBottomSheet(
        visible = visible,
        onDismiss = {
            if (!canSave) onDismiss()
            else showExitDialog = true
        },
        cancelable = false,
        title = {
            TitleItem(
                text = stringResource(id = R.string.create_template),
                icon = Icons.Outlined.Extension
            )
        },
        confirmButton = {
            EnhancedButton(
                enabled = canSave && component.templateName.isNotEmpty(),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                onClick = {
                    component.saveTemplate(initialTemplateFilter)
                    onDismiss()
                }
            ) {
                Text(stringResource(id = R.string.save))
            }
        },
        dragHandle = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawHorizontalStroke(autoElevation = 3.dp)
                    .zIndex(Float.MAX_VALUE)
                    .background(EnhancedBottomSheetDefaults.barContainerColor)
                    .padding(8.dp)
            ) {
                EnhancedIconButton(
                    onClick = {
                        if (!canSave) onDismiss()
                        else showExitDialog = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.exit)
                    )
                }
            }
        },
        enableBackHandler = !canSave
    ) {
        DisposableEffect(Unit) {
            onDispose {
                component.resetState()
            }
        }
        var imageState by remember { mutableStateOf(ImageHeaderState(2)) }

        var selectedUri by rememberSaveable {
            mutableStateOf<Uri?>(null)
        }

        LaunchedEffect(selectedUri) {
            component.setUri(selectedUri)
        }

        LaunchedEffect(initialTemplateFilter) {
            initialTemplateFilter?.let {
                component.setInitialTemplateFilter(it)
            }
        }

        if (visible) {
            BackHandler(enabled = canSave) {
                showExitDialog = true
            }
        }
        val preview: @Composable () -> Unit = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(
                        if (isPortrait) RoundedCornerShape(
                            bottomStart = 24.dp,
                            bottomEnd = 24.dp
                        )
                        else RectangleShape
                    )
                    .background(
                        color = MaterialTheme.colorScheme
                            .surfaceContainer
                            .copy(0.8f)
                    )
                    .shimmer(component.previewBitmap == null && component.isImageLoading),
                contentAlignment = Alignment.Center
            ) {
                SimplePicture(
                    enableContainer = false,
                    boxModifier = Modifier.padding(24.dp),
                    bitmap = component.previewBitmap,
                    loading = component.isImageLoading
                )
            }
        }
        Row {
            val backgroundColor = MaterialTheme.colorScheme.surfaceContainerLow
            if (!isPortrait) {
                Box(modifier = Modifier.weight(1.3f)) {
                    preview()
                }
            }
            val internalHeight = rememberAvailableHeight(imageState = imageState)
            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                imageStickyHeader(
                    visible = isPortrait,
                    internalHeight = internalHeight,
                    imageState = imageState,
                    onStateChange = {
                        imageState = it
                    },
                    padding = 0.dp,
                    backgroundColor = backgroundColor,
                    imageBlock = preview
                )
                item {
                    AnimatedContent(
                        targetState = component.filterList.isNotEmpty(),
                        transitionSpec = {
                            fadeIn() + expandVertically() togetherWith fadeOut() + shrinkVertically()
                        }
                    ) { notEmpty ->
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(Modifier.height(16.dp))
                            ImageSelector(
                                value = selectedUri ?: previewModel.data,
                                onValueChange = { selectedUri = it },
                                subtitle = stringResource(id = R.string.select_template_preview),
                                shape = RoundedCornerShape(16.dp),
                                color = Color.Unspecified
                            )
                            Spacer(Modifier.height(8.dp))
                            RoundedTextField(
                                modifier = Modifier
                                    .container(
                                        shape = MaterialTheme.shapes.large,
                                        resultPadding = 8.dp
                                    ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Text
                                ),
                                onValueChange = component::updateTemplateName,
                                value = component.templateName,
                                label = stringResource(R.string.template_name)
                            )
                            if (notEmpty) {
                                Spacer(Modifier.height(8.dp))
                                Column(
                                    modifier = Modifier
                                        .container(MaterialTheme.shapes.extraLarge)
                                ) {
                                    TitleItem(text = stringResource(R.string.filters))
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.padding(8.dp)
                                    ) {
                                        component.filterList.forEachIndexed { index, filter ->
                                            FilterItem(
                                                backgroundColor = MaterialTheme.colorScheme.surface,
                                                filter = filter,
                                                onFilterChange = {
                                                    component.updateFilter(
                                                        value = it,
                                                        index = index,
                                                        showError = {
                                                            scope.launch {
                                                                toastHostState.showFailureToast(
                                                                    context = context,
                                                                    throwable = it
                                                                )
                                                            }
                                                        }
                                                    )
                                                },
                                                onLongPress = {
                                                    showReorderSheet = true
                                                },
                                                showDragHandle = false,
                                                onRemove = {
                                                    component.removeFilterAtIndex(
                                                        index
                                                    )
                                                }
                                            )
                                        }
                                        AddFilterButton(
                                            onClick = {
                                                showAddFilterSheet = true
                                            },
                                            modifier = Modifier.padding(
                                                horizontal = 16.dp
                                            )
                                        )
                                    }
                                }
                                Spacer(Modifier.height(16.dp))
                            } else {
                                Spacer(Modifier.height(8.dp))
                                AddFilterButton(
                                    onClick = {
                                        showAddFilterSheet = true
                                    }
                                )
                                Spacer(Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    AddFiltersSheet(
        component = component.addFiltersSheetComponent,
        visible = showAddFilterSheet,
        onVisibleChange = { showAddFilterSheet = it },
        canAddTemplates = false,
        previewBitmap = component.previewBitmap,
        onFilterPicked = { component.addFilter(it.newInstance()) },
        onFilterPickedWithParams = { component.addFilter(it) },
        filterTemplateCreationSheetComponent = component
    )

    FilterReorderSheet(
        filterList = component.filterList,
        visible = showReorderSheet,
        onDismiss = {
            showReorderSheet = false
        },
        onReorder = component::updateFiltersOrder
    )

    ExitWithoutSavingDialog(
        onExit = onDismiss,
        onDismiss = { showExitDialog = false },
        visible = showExitDialog
    )
}

class FilterTemplateCreationSheetComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    private val imageGetter: ImageGetter<Bitmap>,
    private val favoriteFiltersInteractor: FavoriteFiltersInteractor,
    private val filterProvider: FilterProvider<Bitmap>,
    dispatchersHolder: DispatchersHolder,
    addFiltersSheetComponentFactory: AddFiltersSheetComponent.Factory
) : BaseComponent(dispatchersHolder, componentContext) {

    val addFiltersSheetComponent: AddFiltersSheetComponent = addFiltersSheetComponentFactory(
        componentContext = componentContext.childContext(
            key = "addFiltersTemplate"
        )
    )

    private val _previewModel: MutableState<ImageModel> = mutableStateOf(
        R.drawable.filter_preview_source.toImageModel()
    )
    val previewModel: ImageModel by _previewModel

    private val _filterList: MutableState<List<UiFilter<*>>> = mutableStateOf(emptyList())
    val filterList by _filterList

    private val _templateName: MutableState<String> = mutableStateOf("")
    val templateName by _templateName

    private var bitmapUri: Uri? by mutableStateOf(null)

    private val _previewBitmap: MutableState<Bitmap?> = mutableStateOf(null)
    val previewBitmap by _previewBitmap

    init {
        favoriteFiltersInteractor
            .getFilterPreviewModel().onEach { data ->
                _previewModel.update { data }
            }.launchIn(componentScope)
    }

    fun updateTemplateName(newName: String) {
        _templateName.update { newName.filter { it.isLetter() || it.isWhitespace() }.trim() }
    }

    private fun updatePreview() {
        debouncedImageCalculation {
            _previewBitmap.update {
                imageGetter.getImageWithTransformations(
                    data = bitmapUri ?: previewModel.data,
                    transformations = filterList.map { filterProvider.filterToTransformation(it) },
                    size = IntegerSize(1000, 1000)
                )
            }
        }
    }

    fun removeFilterAtIndex(index: Int) {
        _filterList.update {
            it.toMutableList().apply {
                removeAt(index)
            }
        }
        updatePreview()
    }

    fun <T : Any> updateFilter(
        value: T,
        index: Int,
        showError: (Throwable) -> Unit
    ) {
        val list = _filterList.value.toMutableList()
        runCatching {
            list[index] = list[index].copy(value)
            _filterList.update { list }
        }.exceptionOrNull()?.let { throwable ->
            showError(throwable)
            list[index] = list[index].newInstance()
            _filterList.update { list }
        }
        updatePreview()
    }

    fun updateFiltersOrder(uiFilters: List<UiFilter<*>>) {
        _filterList.update { uiFilters }
        updatePreview()
    }

    fun addFilter(filter: UiFilter<*>) {
        _filterList.update {
            it + filter
        }
        updatePreview()
    }

    fun saveTemplate(initialTemplateFilter: TemplateFilter?) {
        componentScope.launch {
            if (initialTemplateFilter != null) {
                favoriteFiltersInteractor.removeTemplateFilter(initialTemplateFilter)
            }
            favoriteFiltersInteractor.addTemplateFilter(
                TemplateFilter(
                    name = templateName,
                    filters = filterList
                )
            )
        }
    }

    fun setUri(selectedUri: Uri?) {
        bitmapUri = selectedUri
        updatePreview()
    }

    private var isInitialValueSetAlready: Boolean = false

    fun setInitialTemplateFilter(filter: TemplateFilter) {
        if (templateName.isEmpty() && filterList.isEmpty() && !isInitialValueSetAlready) {
            _templateName.update { filter.name }
            _filterList.update { filter.filters.map { it.toUiFilter() } }
            isInitialValueSetAlready = true
        }
    }

    override fun resetState() {
        _filterList.update { emptyList() }
        _templateName.update { "" }
        cancelImageLoading()
        _previewBitmap.update { null }
        bitmapUri = null
        addFiltersSheetComponent.resetState()
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext
        ): FilterTemplateCreationSheetComponent
    }

}