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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ke.don.slideslide.domain.model.Difficulty
import ke.don.slideslide.ui.utils.SlidePreview
import ke.don.slideslide.ui.utils.SlidePreviewContent

@Composable
fun DifficultySelector(
    selectedDifficulty: Difficulty,
    onDifficultySelected: (Difficulty) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Difficulty.entries.forEach { difficulty ->
            val isSelected = difficulty == selectedDifficulty
            val contentColor = if (isSelected) Color.Black else Color.White
            val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
            val borderColor = if (isSelected) Color.Transparent else Color(0xFF49454F)

            OutlinedButton(
                onClick = { onDifficultySelected(difficulty) },
                modifier = Modifier.weight(1f),
                shape = CircleShape,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = containerColor,
                    contentColor = contentColor
                ),
                border = BorderStroke(1.dp, borderColor)
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp).padding(end = 4.dp)
                    )
                }
                Text(
                    text = "${difficulty.size} x ${difficulty.size}",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@SlidePreview
@Composable
private fun DifficultySelectorPreview() {
    SlidePreviewContent {
        DifficultySelector(
            selectedDifficulty = Difficulty.EASY,
            onDifficultySelected = {},
        )
    }
}
