/*
 * Copyright (C) 2026 Donald Isoe.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ke.don.slideslide.ui.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Clock

@RequiresApi(Build.VERSION_CODES.O)
fun calculateElapsedSeconds(
    startTime: Long?,
    endTime: Long?,
    clock: Clock,
): Long {
    if (startTime == null) return 0L

    val effectiveEndTime = endTime ?: clock.millis()
    return ((effectiveEndTime - startTime) / MILLIS_PER_SECOND).coerceAtLeast(0L)
}

fun formatSeconds(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "%02d:%02d".format(mins, secs)
}

private const val MILLIS_PER_SECOND = 1_000L
