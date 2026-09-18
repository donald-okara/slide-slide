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
package ke.don.slideslide.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ke.don.slideslide.R
import ke.don.slideslide.ui.theme.SlideSlideTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlideTopAppBar(
    title: String,
    actions: SlideTopAppBarActions,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
) {
    TopAppBar(
        title = {
            Image(
                painter = painterResource(id = R.drawable.slide_slide_banner),
                contentDescription = title,
                modifier = Modifier.height(32.dp),
            )
        },
        navigationIcon = { navigationIcon?.invoke() },
        actions = {
            val soundIcon =
                if (actions.isSoundEnabled) {
                    Icons.AutoMirrored.Filled.VolumeUp
                } else {
                    Icons.AutoMirrored.Filled.VolumeOff
                }
            val soundTint =
                if (actions.isSoundEnabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.secondary
                }

            IconButton(onClick = actions.onToggleSound) {
                Icon(
                    imageVector = soundIcon,
                    contentDescription = "Toggle Sound",
                    tint = soundTint,
                )
            }

            val vibrationTint =
                if (actions.isVibrationEnabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.secondary
                }

            IconButton(onClick = actions.onToggleVibration) {
                Icon(
                    imageVector = Icons.Default.Vibration,
                    contentDescription = "Toggle Vibration",
                    tint = vibrationTint,
                )
            }
        },
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun SlideTopAppBarPreview() {
    SlideSlideTheme {
        SlideTopAppBar(
            title = "Slide Slide",
            actions = SlideTopAppBarActions(
                isSoundEnabled = true,
                isVibrationEnabled = false,
                onToggleSound = {},
                onToggleVibration = {},
            ),
        )
    }
}
