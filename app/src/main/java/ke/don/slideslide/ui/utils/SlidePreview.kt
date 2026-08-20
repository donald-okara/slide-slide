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

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ke.don.slideslide.ui.theme.SlideSlideTheme

/**
 * Multi-preview annotation for Slide Slide app.
 * Includes Light and Dark modes.
 */
@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class SlidePreview

/**
 * Multi-preview annotation for full screens.
 */
@Preview(name = "Phone", device = "spec:width=411dp,height=891dp")
@Preview(name = "Tablet", device = "spec:width=1280dp,height=800dp,dpi=240")
@SlidePreview
annotation class SlideScreenPreview

/**
 * A wrapper for Compose previews that applies the app theme and a background surface.
 *
 * @param withPadding Whether to add padding around the content. Default is true for components.
 * @param content The composable content to preview.
 */
@Composable
fun SlidePreviewContent(
    withPadding: Boolean = true,
    content: @Composable () -> Unit,
) {
    SlideSlideTheme {
        Surface {
            if (withPadding) {
                Box(modifier = Modifier.padding(16.dp)) {
                    content()
                }
            } else {
                content()
            }
        }
    }
}
