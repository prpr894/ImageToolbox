package ru.tech.imageresizershrinker.coreui.transformation.filter

import android.graphics.Bitmap
import ru.tech.imageresizershrinker.coreresources.R
import ru.tech.imageresizershrinker.coredomain.image.filters.Filter
import ru.tech.imageresizershrinker.coredomain.image.filters.FilterParam


class UiCrosshatchFilter(
    override val value: Pair<Float, Float> = 0.01f to 0.003f,
) : UiFilter<Pair<Float, Float>>(
    title = R.string.crosshatch,
    value = value,
    paramsInfo = listOf(
        FilterParam(title = R.string.spacing, valueRange = 0.001f..0.05f, roundTo = 4),
        FilterParam(title = R.string.line_width, valueRange = 0.001f..0.02f, roundTo = 4)
    )
), Filter.Crosshatch<Bitmap>