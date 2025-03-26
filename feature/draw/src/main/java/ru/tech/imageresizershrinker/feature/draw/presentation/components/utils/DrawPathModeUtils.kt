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

package ru.tech.imageresizershrinker.feature.draw.presentation.components.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckBoxOutlineBlank
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.ui.graphics.vector.ImageVector
import ru.tech.imageresizershrinker.core.resources.R
import ru.tech.imageresizershrinker.core.resources.icons.FreeArrow
import ru.tech.imageresizershrinker.core.resources.icons.FreeDoubleArrow
import ru.tech.imageresizershrinker.core.resources.icons.FreeDraw
import ru.tech.imageresizershrinker.core.resources.icons.Lasso
import ru.tech.imageresizershrinker.core.resources.icons.Line
import ru.tech.imageresizershrinker.core.resources.icons.LineArrow
import ru.tech.imageresizershrinker.core.resources.icons.LineDoubleArrow
import ru.tech.imageresizershrinker.core.resources.icons.Polygon
import ru.tech.imageresizershrinker.core.resources.icons.Square
import ru.tech.imageresizershrinker.core.resources.icons.Triangle
import ru.tech.imageresizershrinker.feature.draw.domain.DrawPathMode

internal fun DrawPathMode.saveState(
    value: DrawPathMode
): DrawPathMode = when {
    value is DrawPathMode.Rect && this is DrawPathMode.OutlinedRect -> {
        copy(
            rotationDegrees = value.rotationDegrees
        )
    }

    value is DrawPathMode.OutlinedRect && this is DrawPathMode.Rect -> {
        copy(
            rotationDegrees = value.rotationDegrees
        )
    }

    value is DrawPathMode.Polygon && this is DrawPathMode.OutlinedPolygon -> {
        copy(
            vertices = value.vertices,
            rotationDegrees = value.rotationDegrees,
            isRegular = value.isRegular
        )
    }

    value is DrawPathMode.OutlinedPolygon && this is DrawPathMode.Polygon -> {
        copy(
            vertices = value.vertices,
            rotationDegrees = value.rotationDegrees,
            isRegular = value.isRegular
        )
    }

    value is DrawPathMode.Star && this is DrawPathMode.OutlinedStar -> {
        copy(
            vertices = value.vertices,
            innerRadiusRatio = innerRadiusRatio,
            rotationDegrees = value.rotationDegrees,
            isRegular = value.isRegular
        )
    }

    value is DrawPathMode.OutlinedStar && this is DrawPathMode.Star -> {
        copy(
            vertices = value.vertices,
            innerRadiusRatio = innerRadiusRatio,
            rotationDegrees = value.rotationDegrees,
            isRegular = value.isRegular
        )
    }

    value is DrawPathMode.PointingArrow && this is DrawPathMode.LinePointingArrow -> {
        copy(
            sizeScale = value.sizeScale,
            angle = value.angle
        )
    }

    value is DrawPathMode.LinePointingArrow && this is DrawPathMode.PointingArrow -> {
        copy(
            sizeScale = value.sizeScale,
            angle = value.angle
        )
    }

    value is DrawPathMode.PointingArrow && this is DrawPathMode.DoublePointingArrow -> {
        copy(
            sizeScale = value.sizeScale,
            angle = value.angle
        )
    }

    value is DrawPathMode.DoublePointingArrow && this is DrawPathMode.PointingArrow -> {
        copy(
            sizeScale = value.sizeScale,
            angle = value.angle
        )
    }

    value is DrawPathMode.PointingArrow && this is DrawPathMode.DoubleLinePointingArrow -> {
        copy(
            sizeScale = value.sizeScale,
            angle = value.angle
        )
    }

    value is DrawPathMode.DoubleLinePointingArrow && this is DrawPathMode.PointingArrow -> {
        copy(
            sizeScale = value.sizeScale,
            angle = value.angle
        )
    }

    value is DrawPathMode.LinePointingArrow && this is DrawPathMode.DoublePointingArrow -> {
        copy(
            sizeScale = value.sizeScale,
            angle = value.angle
        )
    }

    value is DrawPathMode.DoublePointingArrow && this is DrawPathMode.LinePointingArrow -> {
        copy(
            sizeScale = value.sizeScale,
            angle = value.angle
        )
    }

    value is DrawPathMode.LinePointingArrow && this is DrawPathMode.DoubleLinePointingArrow -> {
        copy(
            sizeScale = value.sizeScale,
            angle = value.angle
        )
    }

    value is DrawPathMode.DoubleLinePointingArrow && this is DrawPathMode.LinePointingArrow -> {
        copy(
            sizeScale = value.sizeScale,
            angle = value.angle
        )
    }

    value is DrawPathMode.DoublePointingArrow && this is DrawPathMode.DoubleLinePointingArrow -> {
        copy(
            sizeScale = value.sizeScale,
            angle = value.angle
        )
    }

    value is DrawPathMode.DoubleLinePointingArrow && this is DrawPathMode.DoublePointingArrow -> {
        copy(
            sizeScale = value.sizeScale,
            angle = value.angle
        )
    }

    else -> this
}

internal fun DrawPathMode.sizeScale(): Float = when (this) {
    is DrawPathMode.PointingArrow -> sizeScale
    is DrawPathMode.LinePointingArrow -> sizeScale
    is DrawPathMode.DoublePointingArrow -> sizeScale
    is DrawPathMode.DoubleLinePointingArrow -> sizeScale
    else -> 1f
}

internal fun DrawPathMode.angle(): Float = when (this) {
    is DrawPathMode.PointingArrow -> angle
    is DrawPathMode.LinePointingArrow -> angle
    is DrawPathMode.DoublePointingArrow -> angle
    is DrawPathMode.DoubleLinePointingArrow -> angle
    else -> 0f
}

internal fun DrawPathMode.vertices(): Int = when (this) {
    is DrawPathMode.Polygon -> vertices
    is DrawPathMode.OutlinedPolygon -> vertices
    is DrawPathMode.Star -> vertices
    is DrawPathMode.OutlinedStar -> vertices
    else -> 0
}

internal fun DrawPathMode.rotationDegrees(): Int = when (this) {
    is DrawPathMode.Polygon -> rotationDegrees
    is DrawPathMode.OutlinedPolygon -> rotationDegrees
    is DrawPathMode.Star -> rotationDegrees
    is DrawPathMode.OutlinedStar -> rotationDegrees
    is DrawPathMode.Rect -> rotationDegrees
    is DrawPathMode.OutlinedRect -> rotationDegrees
    else -> 0
}

internal fun DrawPathMode.isRegular(): Boolean = when (this) {
    is DrawPathMode.Polygon -> isRegular
    is DrawPathMode.OutlinedPolygon -> isRegular
    is DrawPathMode.Star -> isRegular
    is DrawPathMode.OutlinedStar -> isRegular
    else -> false
}

internal fun DrawPathMode.innerRadiusRatio(): Float = when (this) {
    is DrawPathMode.Star -> innerRadiusRatio
    is DrawPathMode.OutlinedStar -> innerRadiusRatio
    else -> 0.5f
}

internal fun DrawPathMode.updatePolygon(
    vertices: Int? = null,
    rotationDegrees: Int? = null,
    isRegular: Boolean? = null
) = when (this) {
    is DrawPathMode.Polygon -> {
        copy(
            vertices = vertices ?: this.vertices,
            rotationDegrees = rotationDegrees ?: this.rotationDegrees,
            isRegular = isRegular ?: this.isRegular
        )
    }

    is DrawPathMode.OutlinedPolygon -> {
        copy(
            vertices = vertices ?: this.vertices,
            rotationDegrees = rotationDegrees ?: this.rotationDegrees,
            isRegular = isRegular ?: this.isRegular
        )
    }

    else -> this
}

internal fun DrawPathMode.updateStar(
    vertices: Int? = null,
    innerRadiusRatio: Float? = null,
    rotationDegrees: Int? = null,
    isRegular: Boolean? = null
) = when (this) {
    is DrawPathMode.Star -> {
        copy(
            vertices = vertices ?: this.vertices,
            innerRadiusRatio = innerRadiusRatio ?: this.innerRadiusRatio,
            rotationDegrees = rotationDegrees ?: this.rotationDegrees,
            isRegular = isRegular ?: this.isRegular
        )
    }

    is DrawPathMode.OutlinedStar -> {
        copy(
            vertices = vertices ?: this.vertices,
            innerRadiusRatio = innerRadiusRatio ?: this.innerRadiusRatio,
            rotationDegrees = rotationDegrees ?: this.rotationDegrees,
            isRegular = isRegular ?: this.isRegular
        )
    }

    else -> this
}

internal fun DrawPathMode.updateRect(
    rotationDegrees: Int? = null
) = when (this) {
    is DrawPathMode.Rect -> {
        copy(
            rotationDegrees = rotationDegrees ?: this.rotationDegrees
        )
    }

    is DrawPathMode.OutlinedRect -> {
        copy(
            rotationDegrees = rotationDegrees ?: this.rotationDegrees
        )
    }

    else -> this
}

internal fun DrawPathMode.updateArrow(
    sizeScale: Float? = null,
    angle: Float? = null
) = when (this) {
    is DrawPathMode.PointingArrow -> {
        copy(
            sizeScale = sizeScale ?: this.sizeScale,
            angle = angle ?: this.angle
        )
    }

    is DrawPathMode.LinePointingArrow -> {
        copy(
            sizeScale = sizeScale ?: this.sizeScale,
            angle = angle ?: this.angle
        )
    }

    is DrawPathMode.DoublePointingArrow -> {
        copy(
            sizeScale = sizeScale ?: this.sizeScale,
            angle = angle ?: this.angle
        )
    }

    is DrawPathMode.DoubleLinePointingArrow -> {
        copy(
            sizeScale = sizeScale ?: this.sizeScale,
            angle = angle ?: this.angle
        )
    }

    else -> this
}

internal fun DrawPathMode.isArrow(): Boolean =
    this is DrawPathMode.PointingArrow || this is DrawPathMode.LinePointingArrow
            || this is DrawPathMode.DoublePointingArrow || this is DrawPathMode.DoubleLinePointingArrow

internal fun DrawPathMode.isRect(): Boolean =
    this is DrawPathMode.Rect || this is DrawPathMode.OutlinedRect

internal fun DrawPathMode.isPolygon(): Boolean =
    this is DrawPathMode.Polygon || this is DrawPathMode.OutlinedPolygon

internal fun DrawPathMode.isStar(): Boolean =
    this is DrawPathMode.Star || this is DrawPathMode.OutlinedStar

internal fun DrawPathMode.getSubtitle(): Int = when (this) {
    is DrawPathMode.DoubleLinePointingArrow -> R.string.double_line_arrow_sub
    is DrawPathMode.DoublePointingArrow -> R.string.double_arrow_sub
    DrawPathMode.Free -> R.string.free_drawing_sub
    DrawPathMode.Line -> R.string.line_sub
    is DrawPathMode.LinePointingArrow -> R.string.line_arrow_sub
    is DrawPathMode.PointingArrow -> R.string.arrow_sub
    DrawPathMode.OutlinedOval -> R.string.outlined_oval_sub
    is DrawPathMode.OutlinedRect -> R.string.outlined_rect_sub
    DrawPathMode.Oval -> R.string.oval_sub
    is DrawPathMode.Rect -> R.string.rect_sub
    DrawPathMode.Lasso -> R.string.lasso_sub
    DrawPathMode.OutlinedTriangle -> R.string.outlined_triangle_sub
    DrawPathMode.Triangle -> R.string.triangle_sub
    is DrawPathMode.Polygon -> R.string.polygon_sub
    is DrawPathMode.OutlinedPolygon -> R.string.outlined_polygon_sub
    is DrawPathMode.OutlinedStar -> R.string.outlined_star_sub
    is DrawPathMode.Star -> R.string.star_sub
}

internal fun DrawPathMode.getTitle(): Int = when (this) {
    is DrawPathMode.DoubleLinePointingArrow -> R.string.double_line_arrow
    is DrawPathMode.DoublePointingArrow -> R.string.double_arrow
    DrawPathMode.Free -> R.string.free_drawing
    DrawPathMode.Line -> R.string.line
    is DrawPathMode.LinePointingArrow -> R.string.line_arrow
    is DrawPathMode.PointingArrow -> R.string.arrow
    DrawPathMode.OutlinedOval -> R.string.outlined_oval
    is DrawPathMode.OutlinedRect -> R.string.outlined_rect
    DrawPathMode.Oval -> R.string.oval
    is DrawPathMode.Rect -> R.string.rect
    DrawPathMode.Lasso -> R.string.lasso
    DrawPathMode.OutlinedTriangle -> R.string.outlined_triangle
    DrawPathMode.Triangle -> R.string.triangle
    is DrawPathMode.Polygon -> R.string.polygon
    is DrawPathMode.OutlinedPolygon -> R.string.outlined_polygon
    is DrawPathMode.OutlinedStar -> R.string.outlined_star
    is DrawPathMode.Star -> R.string.star
}

internal fun DrawPathMode.getIcon(): ImageVector = when (this) {
    is DrawPathMode.DoubleLinePointingArrow -> Icons.Rounded.LineDoubleArrow
    is DrawPathMode.DoublePointingArrow -> Icons.Rounded.FreeDoubleArrow
    DrawPathMode.Free -> Icons.Rounded.FreeDraw
    DrawPathMode.Line -> Icons.Rounded.Line
    is DrawPathMode.LinePointingArrow -> Icons.Rounded.LineArrow
    is DrawPathMode.PointingArrow -> Icons.Rounded.FreeArrow
    DrawPathMode.OutlinedOval -> Icons.Rounded.RadioButtonUnchecked
    is DrawPathMode.OutlinedRect -> Icons.Rounded.CheckBoxOutlineBlank
    DrawPathMode.Oval -> Icons.Rounded.Circle
    is DrawPathMode.Rect -> Icons.Rounded.Square
    DrawPathMode.Lasso -> Icons.Rounded.Lasso
    DrawPathMode.Triangle -> Icons.Rounded.Triangle
    DrawPathMode.OutlinedTriangle -> Icons.Outlined.Triangle
    is DrawPathMode.Polygon -> Icons.Rounded.Polygon
    is DrawPathMode.OutlinedPolygon -> Icons.Outlined.Polygon
    is DrawPathMode.OutlinedStar -> Icons.Rounded.StarOutline
    is DrawPathMode.Star -> Icons.Rounded.Star
}