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

/**
 * Interface for providing game feedback such as sounds and vibrations.
 */
interface FeedbackManager {
    /**
     * Sets whether sound and vibration are enabled.
     */
    fun setEnabled(soundEnabled: Boolean, vibrationEnabled: Boolean)

    /**
     * Plays feedback for a tile move.
     */
    fun playMoveFeedback()

    /**
     * Plays feedback for a hint being requested or highlighted.
     */
    fun playHintFeedback()

    /**
     * Plays feedback for winning the game.
     */
    fun playVictoryFeedback()
    
    /**
     * Plays feedback for a generic click or UI interaction.
     */
    fun playClickFeedback()
    
    /**
     * Triggers a vibration effect to provide tactile feedback.
     */
    fun playVibrate()

    /**
     * Releases resources used by the feedback manager.
     */
    fun release()
}
