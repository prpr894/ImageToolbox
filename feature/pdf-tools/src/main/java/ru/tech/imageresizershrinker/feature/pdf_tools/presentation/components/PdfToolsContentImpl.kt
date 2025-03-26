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

package ru.tech.imageresizershrinker.feature.pdf_tools.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.ui.utils.animation.fancySlideTransition
import ru.tech.imageresizershrinker.core.ui.utils.helper.ContextUtils.getFilename
import ru.tech.imageresizershrinker.core.ui.utils.navigation.Screen
import ru.tech.imageresizershrinker.core.ui.utils.provider.LocalScreenSize
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedFloatingActionButton
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedIconButton
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedTopAppBar
import ru.tech.imageresizershrinker.core.ui.widget.enhanced.EnhancedTopAppBarType
import ru.tech.imageresizershrinker.core.ui.widget.modifier.container
import ru.tech.imageresizershrinker.core.ui.widget.modifier.drawHorizontalStroke
import ru.tech.imageresizershrinker.core.ui.widget.other.TopAppBarEmoji
import ru.tech.imageresizershrinker.core.ui.widget.preferences.PreferenceItem
import ru.tech.imageresizershrinker.core.ui.widget.text.marquee
import ru.tech.imageresizershrinker.feature.pdf_tools.presentation.screenLogic.PdfToolsComponent

@Composable
internal fun PdfToolsContentImpl(
    component: PdfToolsComponent,
    scrollBehavior: TopAppBarScrollBehavior,
    onGoBack: () -> Unit,
    actionButtons: @Composable RowScope.(pdfType: Screen.PdfTools.Type?) -> Unit,
    onPickContent: (Screen.PdfTools.Type) -> Unit,
    onSelectPdf: () -> Unit,
    buttons: @Composable (pdfType: Screen.PdfTools.Type) -> Unit,
    controls: @Composable (pdfType: Screen.PdfTools.Type) -> Unit,
    isPortrait: Boolean,
) {
    val selectAllToggle = remember { mutableStateOf(false) }
    val deselectAllToggle = remember { mutableStateOf(false) }

    val context = LocalContext.current

    Column(Modifier.fillMaxSize()) {
        EnhancedTopAppBar(
            type = EnhancedTopAppBarType.Large,
            scrollBehavior = scrollBehavior,
            title = {
                AnimatedContent(
                    targetState = component.pdfType to component.pdfPreviewUri,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    modifier = Modifier.marquee()
                ) { (pdfType, previewUri) ->
                    Text(
                        text = previewUri?.let {
                            context.getFilename(it)
                        } ?: stringResource(pdfType?.title ?: R.string.pdf_tools),
                        textAlign = TextAlign.Center
                    )
                }
            },
            navigationIcon = {
                EnhancedIconButton(
                    onClick = onGoBack
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.exit)
                    )
                }
            },
            actions = {
                if (!isPortrait) {
                    actionButtons(component.pdfType)
                }
                if (component.pdfType == null) {
                    TopAppBarEmoji()
                } else {
                    val selectedPagesSize = component.pdfToImageState?.selectedPages?.size
                    val visible by remember(
                        component.pdfToImageState?.selectedPages,
                        component.pdfType
                    ) {
                        derivedStateOf {
                            (selectedPagesSize != 0 && component.pdfType is Screen.PdfTools.Type.PdfToImages)
                        }
                    }
                    AnimatedVisibility(
                        visible = component.pdfType is Screen.PdfTools.Type.PdfToImages && selectedPagesSize != component.pdfToImageState?.pagesCount,
                        enter = fadeIn() + scaleIn() + expandHorizontally(),
                        exit = fadeOut() + scaleOut() + shrinkHorizontally()
                    ) {
                        EnhancedIconButton(
                            onClick = {
                                selectAllToggle.value = true
                            },
                            enabled = component.pdfType != null
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.SelectAll,
                                contentDescription = "Select All"
                            )
                        }
                    }
                    AnimatedVisibility(
                        modifier = Modifier
                            .padding(8.dp)
                            .container(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                resultPadding = 0.dp
                            ),
                        visible = visible
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            selectedPagesSize?.takeIf { it != 0 }?.let {
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = selectedPagesSize.toString(),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            EnhancedIconButton(
                                onClick = {
                                    deselectAllToggle.value = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = stringResource(R.string.close)
                                )
                            }
                        }
                    }
                }
            }
        )

        val screenWidth = LocalScreenSize.current.widthPx

        AnimatedContent(
            transitionSpec = {
                fancySlideTransition(
                    isForward = targetState != null,
                    screenWidthPx = screenWidth
                )
            },
            targetState = component.pdfType
        ) { pdfType ->
            when (pdfType) {
                null -> {
                    Column {
                        val cutout = WindowInsets.displayCutout.asPaddingValues()
                        LazyVerticalStaggeredGrid(
                            modifier = Modifier.weight(1f),
                            columns = StaggeredGridCells.Adaptive(300.dp),
                            horizontalArrangement = Arrangement.spacedBy(
                                space = 12.dp,
                                alignment = Alignment.CenterHorizontally
                            ),
                            verticalItemSpacing = 12.dp,
                            contentPadding = PaddingValues(
                                bottom = 12.dp + WindowInsets
                                    .navigationBars
                                    .asPaddingValues()
                                    .calculateBottomPadding(),
                                top = 12.dp,
                                end = 12.dp + cutout.calculateEndPadding(
                                    LocalLayoutDirection.current
                                ),
                                start = 12.dp + cutout.calculateStartPadding(
                                    LocalLayoutDirection.current
                                )
                            ),
                        ) {
                            Screen.PdfTools.Type.entries.forEach {
                                item {
                                    PreferenceItem(
                                        onClick = {
                                            onPickContent(it)
                                        },
                                        startIcon = it.icon,
                                        title = stringResource(it.title),
                                        subtitle = stringResource(it.subtitle),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .drawHorizontalStroke(true)
                                .background(
                                    MaterialTheme.colorScheme.surfaceContainer
                                ),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            EnhancedFloatingActionButton(
                                onClick = onSelectPdf,
                                modifier = Modifier
                                    .navigationBarsPadding()
                                    .padding(16.dp),
                                content = {
                                    Spacer(Modifier.width(16.dp))
                                    Icon(
                                        imageVector = Icons.Rounded.FileOpen,
                                        contentDescription = stringResource(R.string.pick_file)
                                    )
                                    Spacer(Modifier.width(16.dp))
                                    Text(stringResource(R.string.pick_file))
                                    Spacer(Modifier.width(16.dp))
                                }
                            )
                        }
                    }
                }

                else -> {
                    Column {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (pdfType is Screen.PdfTools.Type.Preview || pdfType is Screen.PdfTools.Type.PdfToImages) {
                                val direction = LocalLayoutDirection.current
                                Box(
                                    modifier = Modifier
                                        .container(
                                            shape = RectangleShape,
                                            resultPadding = 0.dp,
                                            color = if (pdfType is Screen.PdfTools.Type.Preview || !isPortrait) {
                                                MaterialTheme.colorScheme.surfaceContainerLow
                                            } else MaterialTheme.colorScheme.surface
                                        )
                                        .weight(1.2f)
                                        .clipToBounds(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (pdfType is Screen.PdfTools.Type.Preview) {
                                        PdfViewer(
                                            modifier = Modifier.fillMaxWidth(),
                                            uriState = component.pdfPreviewUri,
                                            contentPadding = PaddingValues(
                                                start = 20.dp + WindowInsets.displayCutout
                                                    .asPaddingValues()
                                                    .calculateStartPadding(direction),
                                                end = 20.dp
                                            )
                                        )
                                    } else {
                                        Column(
                                            modifier = if (isPortrait) {
                                                Modifier
                                                    .fillMaxSize()
                                                    .verticalScroll(rememberScrollState())
                                            } else Modifier,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            var pagesCount by remember {
                                                mutableIntStateOf(1)
                                            }
                                            PdfViewer(
                                                modifier = if (isPortrait) {
                                                    Modifier
                                                        .height(
                                                            (130.dp * pagesCount).coerceAtMost(
                                                                420.dp
                                                            )
                                                        )
                                                        .fillMaxWidth()
                                                } else {
                                                    Modifier.fillMaxWidth()
                                                }.padding(
                                                    start = WindowInsets
                                                        .displayCutout
                                                        .asPaddingValues()
                                                        .calculateStartPadding(direction)
                                                ),
                                                onGetPagesCount = { pagesCount = it },
                                                uriState = component.pdfToImageState?.uri,
                                                orientation = PdfViewerOrientation.Grid,
                                                enableSelection = true,
                                                selectAllToggle = selectAllToggle,
                                                deselectAllToggle = deselectAllToggle,
                                                selectedPages = component.pdfToImageState?.selectedPages
                                                    ?: emptyList(),
                                                updateSelectedPages = component::updatePdfToImageSelection,
                                                spacing = 4.dp
                                            )
                                            if (isPortrait) {
                                                controls(pdfType)
                                            }
                                        }
                                    }
                                }
                            }

                            if (pdfType !is Screen.PdfTools.Type.Preview && !isPortrait || pdfType is Screen.PdfTools.Type.ImagesToPdf) {
                                val direction = LocalLayoutDirection.current
                                Box(
                                    modifier = Modifier
                                        .weight(0.7f)
                                        .fillMaxHeight()
                                        .clipToBounds()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .verticalScroll(rememberScrollState())
                                            .then(
                                                if (pdfType is Screen.PdfTools.Type.ImagesToPdf) {
                                                    Modifier.padding(
                                                        start = WindowInsets
                                                            .displayCutout
                                                            .asPaddingValues()
                                                            .calculateStartPadding(
                                                                direction
                                                            )
                                                    )
                                                } else Modifier
                                            )
                                    ) {
                                        controls(pdfType)
                                    }
                                }
                            }
                            if (!isPortrait) {
                                val direction = LocalLayoutDirection.current
                                Column(
                                    Modifier
                                        .container(RectangleShape)
                                        .fillMaxHeight()
                                        .padding(horizontal = 16.dp)
                                        .navigationBarsPadding()
                                        .padding(
                                            end = WindowInsets.displayCutout
                                                .asPaddingValues()
                                                .calculateEndPadding(direction)
                                        ),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    buttons(pdfType)
                                }
                            }
                        }
                        if (isPortrait) {
                            BottomAppBar(
                                actions = {
                                    actionButtons(pdfType)
                                },
                                floatingActionButton = {
                                    Row {
                                        buttons(pdfType)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}