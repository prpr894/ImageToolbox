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
@file:Suppress("NOTHING_TO_INLINE")

package ru.tech.imageresizershrinker.core.domain.utils

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive


inline fun <reified T> T?.notNullAnd(
    predicate: (T) -> Boolean
): Boolean = if (this != null) predicate(this)
else false

fun CharSequence.isBase64() = isNotEmpty() && BASE64_PATTERN.matches(this)

fun CharSequence.trimToBase64() = toString().filter { !it.isWhitespace() }.substringAfter(",")

private val BASE64_PATTERN = Regex(
    "^(?=(.{4})*\$)[A-Za-z0-9+/]*={0,2}\$"
)

inline fun <reified T, reified R> T.cast(): R = this as R

inline fun <reified T, reified R> T.safeCast(): R? = this as? R

inline fun <reified R> Any?.ifCasts(action: (R) -> Unit) = (this as? R)?.let(action)


inline operator fun CharSequence.times(
    count: Int
): CharSequence = repeat(count.coerceAtLeast(0))


suspend inline fun <T, R> T.runSuspendCatching(block: T.() -> R): Result<R> {
    currentCoroutineContext().ensureActive()

    return try {
        Result.success(block())
    } catch (e: Throwable) {
        currentCoroutineContext().ensureActive()
        Result.failure(e)
    }
}