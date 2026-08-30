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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ke.don.slideslide.ui.utils.SlidePreview
import ke.don.slideslide.ui.utils.SlidePreviewContent
import ke.don.slideslide.ui.utils.formatSeconds

@Suppress("MagicNumber")
private const val DIALOG_WIDTH_FRACTION = 0.9f

@Composable
fun VictoryDialog(
    moveCount: Int,
    timerSeconds: Long,
    onDismiss: () -> Unit,
    onPlayAgain: () -> Unit,
    onChooseAnotherImage: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth(DIALOG_WIDTH_FRACTION)
                    .padding(16.dp),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
        ) {
            VictoryDialogContent(
                moveCount = moveCount,
                timerSeconds = timerSeconds,
                onPlayAgain = onPlayAgain,
                onChooseAnotherImage = onChooseAnotherImage,
            )
        }
    }
}

@Composable
private fun VictoryDialogContent(
    moveCount: Int,
    timerSeconds: Long,
    onPlayAgain: () -> Unit,
    onChooseAnotherImage: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        VictoryHeader()

        Spacer(modifier = Modifier.height(32.dp))

        VictoryStats(moveCount, timerSeconds)

        Spacer(modifier = Modifier.height(40.dp))

        VictoryActions(
            onPlayAgain = onPlayAgain,
            onChooseAnotherImage = onChooseAnotherImage,
        )
    }
}

@Composable
private fun VictoryHeader() {
    Text(
        text = "🎉",
        fontSize = 48.sp,
        modifier = Modifier.padding(bottom = 16.dp),
    )

    Text(
        text = "Puzzle Solved!",
        style =
            MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            ),
        textAlign = TextAlign.Center,
    )

    Text(
        text = "Excellent work! You completed the puzzle.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.secondary,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun VictoryStats(
    moveCount: Int,
    timerSeconds: Long,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatSummaryItem(label = "MOVES", value = moveCount.toString())

        Box(
            modifier =
                Modifier
                    .padding(horizontal = 32.dp)
                    .width(1.dp)
                    .height(48.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant),
        )

        StatSummaryItem(label = "TIME", value = formatSeconds(timerSeconds))
    }
}

@Composable
private fun VictoryActions(
    onPlayAgain: () -> Unit,
    onChooseAnotherImage: () -> Unit,
) {
    Button(
        onClick = onPlayAgain,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
    ) {
        Text(
            text = "Play Again",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedButton(
        onClick = onChooseAnotherImage,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors =
            ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
    ) {
        Text(
            text = "Choose Another Image",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        )
    }
}

@Composable
private fun StatSummaryItem(
    label: String,
    value: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style =
                MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
        )
        Text(
            text = label,
            style =
                MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                ),
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Suppress("UnusedPrivateMember")
@SlidePreview
@Composable
private fun VictoryDialogPreview() {
    SlidePreviewContent {
        VictoryDialog(
            moveCount = 42,
            timerSeconds = 125,
            onDismiss = {},
            onPlayAgain = {},
            onChooseAnotherImage = {},
        )
    }
}
