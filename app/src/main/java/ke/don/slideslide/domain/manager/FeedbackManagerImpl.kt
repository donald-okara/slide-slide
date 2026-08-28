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
package ke.don.slideslide.domain.manager

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import ke.don.slideslide.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedbackManagerImpl
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) : FeedbackManager {
        companion object {
            private const val MAX_STREAMS = 3
            private const val MOVE_VIBRATION_MS = 50L
            private const val HINT_VIBRATION_MS = 100L
            private const val CLICK_VIBRATION_MS = 50L
            private const val GENERIC_VIBRATION_MS = 50L
            private val VICTORY_PATTERN = longArrayOf(0, 100, 50, 100, 50, 200)
        }

        private val audioAttributes =
            AudioAttributes
                .Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

        private val soundPool =
            SoundPool
                .Builder()
                .setMaxStreams(MAX_STREAMS)
                .setAudioAttributes(audioAttributes)
                .build()

        private val vibrator =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

        private val moveSoundId = soundPool.load(context, R.raw.swipe, 1)
        private val hintSoundId = soundPool.load(context, R.raw.glow, 1)
        private val victorySoundId = soundPool.load(context, R.raw.victory, 1)
        private val clickSoundId = soundPool.load(context, R.raw.click, 1)

        private var soundEnabled: Boolean = true
        private var vibrationEnabled: Boolean = true

        override fun setEnabled(
            soundEnabled: Boolean,
            vibrationEnabled: Boolean,
        ) {
            this.soundEnabled = soundEnabled
            this.vibrationEnabled = vibrationEnabled
        }

        override fun playMoveFeedback() {
            if (soundEnabled) soundPool.play(moveSoundId, 1f, 1f, 0, 0, 1f)
            vibrate(MOVE_VIBRATION_MS)
        }

        override fun playHintFeedback() {
            if (soundEnabled) soundPool.play(hintSoundId, 1f, 1f, 0, 0, 1f)
            vibrate(HINT_VIBRATION_MS)
        }

        override fun playVictoryFeedback() {
            if (soundEnabled) soundPool.play(victorySoundId, 1f, 1f, 0, 0, 1f)
            vibrate(VICTORY_PATTERN)
        }

        override fun playClickFeedback() {
            if (soundEnabled) soundPool.play(clickSoundId, 1f, 1f, 0, 0, 1f)
            vibrate(CLICK_VIBRATION_MS)
        }

        override fun playVibrate() {
            vibrate(GENERIC_VIBRATION_MS)
        }

        override fun release() {
            soundPool.release()
        }

        private fun vibrate(duration: Long) {
            if (!vibrationEnabled || !vibrator.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(duration)
            }
        }

        private fun vibrate(pattern: LongArray) {
            if (!vibrationEnabled || !vibrator.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
            }
        }
    }
