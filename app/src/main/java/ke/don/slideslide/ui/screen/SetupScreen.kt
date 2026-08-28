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
package ke.don.slideslide.ui.screen

import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ke.don.slideslide.domain.model.Difficulty
import ke.don.slideslide.ui.component.DifficultySelector
import ke.don.slideslide.ui.state.PuzzleIntent
import ke.don.slideslide.ui.state.PuzzleUiState
import ke.don.slideslide.ui.utils.SlidePreviewContent
import ke.don.slideslide.ui.utils.SlideScreenPreview
import ke.don.slideslide.ui.viewmodel.PuzzleViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SetupScreen(
    viewModel: PuzzleViewModel,
    onStartGame: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val launcher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri: Uri? ->
            uri?.let { viewModel.onIntent(PuzzleIntent.SelectImage(it)) }
        }

    LaunchedEffect(uiState.selectedImageUri, uiState.difficulty) {
        val uri = uiState.selectedImageUri
        if (uri != null) {
            val bitmap =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
            viewModel.onIntent(PuzzleIntent.ProcessImage(bitmap, uiState.difficulty))
        }
    }

    SetupContent(
        uiState = uiState,
        onIntent = { viewModel.onIntent(it) },
        onPickImage = { launcher.launch("image/*") },
        onStartGame = onStartGame,
        onInteraction = { viewModel.playClickFeedback() }
    )

    if (uiState.isCropping) {
        ImageCropScreen(viewModel = viewModel)
    }
}

@Composable
fun SetupContent(
    uiState: PuzzleUiState,
    onIntent: (PuzzleIntent) -> Unit,
    onPickImage: () -> Unit,
    onStartGame: () -> Unit,
    onInteraction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { onIntent(PuzzleIntent.ToggleSound) }) {
                    Icon(
                        imageVector = if (uiState.isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Toggle Sound",
                        tint = if (uiState.isSoundEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                }
                IconButton(onClick = { onIntent(PuzzleIntent.ToggleVibration) }) {
                    Icon(
                        imageVector = Icons.Default.Vibration,
                        contentDescription = "Toggle Vibration",
                        tint = if (uiState.isVibrationEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Text(
                text = "Sliding Puzzle",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color = Color.White,
                modifier = Modifier.padding(top = 32.dp)
            )
            
            Text(
                text = "Choose an image and difficulty to start",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Image Picker Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(32.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(onClick = {
                        onInteraction()
                        onPickImage()
                    }),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.originalImage != null) {
                    Image(
                        bitmap = uiState.originalImage.asImageBitmap(),
                        contentDescription = "Selected Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Select an Image",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Difficulty Label
            Text(
                text = "DIFFICULTY",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 12.dp)
            )

            DifficultySelector(
                selectedDifficulty = uiState.difficulty,
                onDifficultySelected = { onIntent(PuzzleIntent.ChangeDifficulty(it)) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Start Game Button
            val isEnabled = uiState.selectedImageUri != null
            Button(
                onClick = {
                    onIntent(PuzzleIntent.Shuffle)
                    onStartGame()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isEnabled) MaterialTheme.colorScheme.primary else Color(0xFF3C393F),
                    contentColor = if (isEnabled) Color.Black else MaterialTheme.colorScheme.secondary
                ),
                enabled = isEnabled
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Start Game",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@SlideScreenPreview
@Composable
private fun SetupContentPreview() {
    SlidePreviewContent(withPadding = false) {
        SetupContent(
            uiState = PuzzleUiState(difficulty = Difficulty.EASY),
            onIntent = {},
            onPickImage = {},
            onStartGame = {},
            onInteraction = {},
        )
    }
}
