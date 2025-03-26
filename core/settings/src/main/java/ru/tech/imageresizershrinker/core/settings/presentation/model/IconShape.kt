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

package ru.tech.imageresizershrinker.core.settings.presentation.model

import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialShapes
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import ru.tech.imageresizershrinker.core.resources.shapes.ArrowShape
import ru.tech.imageresizershrinker.core.resources.shapes.BookmarkShape
import ru.tech.imageresizershrinker.core.resources.shapes.BurgerShape
import ru.tech.imageresizershrinker.core.resources.shapes.CloverShape
import ru.tech.imageresizershrinker.core.resources.shapes.DropletShape
import ru.tech.imageresizershrinker.core.resources.shapes.EggShape
import ru.tech.imageresizershrinker.core.resources.shapes.ExplosionShape
import ru.tech.imageresizershrinker.core.resources.shapes.HeartShape
import ru.tech.imageresizershrinker.core.resources.shapes.MapShape
import ru.tech.imageresizershrinker.core.resources.shapes.MaterialStarShape
import ru.tech.imageresizershrinker.core.resources.shapes.OctagonShape
import ru.tech.imageresizershrinker.core.resources.shapes.OvalShape
import ru.tech.imageresizershrinker.core.resources.shapes.PentagonShape
import ru.tech.imageresizershrinker.core.resources.shapes.PillShape
import ru.tech.imageresizershrinker.core.resources.shapes.ShieldShape
import ru.tech.imageresizershrinker.core.resources.shapes.ShurikenShape
import ru.tech.imageresizershrinker.core.resources.shapes.SimpleHeartShape
import ru.tech.imageresizershrinker.core.resources.shapes.SmallMaterialStarShape
import ru.tech.imageresizershrinker.core.resources.shapes.SquircleShape
import ru.tech.imageresizershrinker.core.settings.presentation.utils.toShape

data class IconShape(
    val shape: Shape,
    val padding: Dp = 4.dp,
    val iconSize: Dp = 24.dp
) {
    fun takeOrElseFrom(
        iconShapesList: List<IconShape>
    ): IconShape = if (this == Random) iconShapesList
        .filter { it != Random }
        .random()
    else this

    companion object {
        val Random by lazy {
            IconShape(
                shape = RectangleShape,
                padding = 0.dp,
                iconSize = 0.dp
            )
        }

        val entries: ImmutableList<IconShape> by lazy {
            listOf(
                IconShape(SquircleShape),
                IconShape(RoundedCornerShape(15)),
                IconShape(RoundedCornerShape(25)),
                IconShape(RoundedCornerShape(35)),
                IconShape(RoundedCornerShape(45)),
                IconShape(CutCornerShape(25)),
                IconShape(CutCornerShape(35), 8.dp, 22.dp),
                IconShape(CutCornerShape(50), 10.dp, 18.dp),
                IconShape(CloverShape),
                IconShape(MaterialStarShape, 6.dp, 22.dp),
                IconShape(SmallMaterialStarShape, 6.dp, 22.dp),
                IconShape(BookmarkShape, 8.dp, 22.dp),
                IconShape(PillShape, 10.dp, 22.dp),
                IconShape(BurgerShape, 6.dp, 22.dp),
                IconShape(OvalShape, 6.dp),
                IconShape(ShieldShape, 8.dp, 20.dp),
                IconShape(EggShape, 8.dp, 20.dp),
                IconShape(DropletShape, 6.dp, 22.dp),
                IconShape(ArrowShape, 10.dp, 20.dp),
                IconShape(PentagonShape, 6.dp, 22.dp),
                IconShape(OctagonShape, 6.dp, 22.dp),
                IconShape(ShurikenShape, 8.dp, 22.dp),
                IconShape(ExplosionShape, 6.dp),
                IconShape(MapShape, 10.dp, 22.dp),
                IconShape(HeartShape, 10.dp, 18.dp),
                IconShape(SimpleHeartShape, 12.dp, 16.dp),
            ).toMutableList().apply {
                val shapes = listOf(
                    MaterialShapes.Slanted,
                    MaterialShapes.Arch,
                    MaterialShapes.SemiCircle,
                    MaterialShapes.Oval,
                    MaterialShapes.Diamond,
                    MaterialShapes.ClamShell,
                    MaterialShapes.Gem,
                    MaterialShapes.Sunny,
                    MaterialShapes.VerySunny,
                    MaterialShapes.Cookie6Sided,
                    MaterialShapes.Cookie9Sided,
                    MaterialShapes.Ghostish,
                    MaterialShapes.Clover4Leaf,
                    MaterialShapes.Clover8Leaf,
                    MaterialShapes.Burst,
                    MaterialShapes.SoftBurst,
                    MaterialShapes.Boom,
                    MaterialShapes.SoftBoom,
                    MaterialShapes.Flower,
                    MaterialShapes.Puffy,
                    MaterialShapes.PuffyDiamond,
                    MaterialShapes.PixelCircle,
                    MaterialShapes.Bun
                ).map {
                    IconShape(it.toShape(), 10.dp, 20.dp)
                }
                addAll(shapes)
                add(Random)
            }.toPersistentList()
        }
    }
}