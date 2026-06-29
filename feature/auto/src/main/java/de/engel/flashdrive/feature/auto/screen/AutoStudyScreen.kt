package de.engel.flashdrive.feature.auto.screen

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.CarText
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.Template
import de.engel.flashdrive.feature.auto.AutoStudyViewModel
import de.engel.flashdrive.feature.auto.voice.VoiceCommand

/**
 * Screen that shows the current flashcard question during an auto study session.
 * Provides actions: "Zeigen" (reveal answer), "Skip" (skip card), "Ende" (end session).
 * Also supports voice commands via the VoiceCommandProcessor.
 */
class AutoStudyScreen(
    carContext: CarContext,
    private val deckId: Long,
    private val deckName: String,
    private val viewModel: AutoStudyViewModel = AutoStudyViewModel(),
) : Screen(carContext) {

    override fun onGetTemplate(): Template {
        val state = viewModel.uiState.value
        val question = state.currentQuestion
            ?: return MessageTemplate.Builder("Keine Karten verfügbar")
                .addAction(
                    Action.Builder()
                        .setTitle("Fertig")
                        .setOnClickListener {
                            screenManager.push(
                                AutoSessionCompleteScreen(
                                    carContext = carContext,
                                    deckName = deckName,
                                    cardsStudied = state.cardsStudied,
                                    correctCount = state.correctCount,
                                )
                            )
                        }
                        .build()
                )
                .build()

        return MessageTemplate.Builder(question)
            .setHeaderAction(Action.BACK)
            .setTitle("Frage • $deckName")
            .addAction(
                Action.Builder()
                    .setTitle("Zeigen")
                    .setOnClickListener {
                        viewModel.flipCard()
                        screenManager.push(
                            AutoCardFlippedScreen(
                                carContext = carContext,
                                deckId = deckId,
                                deckName = deckName,
                                viewModel = viewModel,
                            )
                        )
                    }
                    .build()
            )
            .addAction(
                Action.Builder()
                    .setTitle("Skip")
                    .setOnClickListener {
                        viewModel.skipCard()
                    }
                    .build()
            )
            .addAction(
                Action.Builder()
                    .setTitle("Ende")
                    .setOnClickListener {
                        viewModel.endSession()
                        screenManager.push(
                            AutoSessionCompleteScreen(
                                carContext = carContext,
                                deckName = deckName,
                                cardsStudied = viewModel.uiState.value.cardsStudied,
                                correctCount = viewModel.uiState.value.correctCount,
                            )
                        )
                    }
                    .build()
            )
            .build()
    }

    /**
     * Processes a voice command received from the car's voice input system.
     * Called by the hosting Activity/Service when voice input is detected.
     */
    fun onVoiceCommand(command: VoiceCommand) {
        when (command) {
            is VoiceCommand.Flip -> {
                viewModel.flipCard()
                screenManager.push(
                    AutoCardFlippedScreen(
                        carContext = carContext,
                        deckId = deckId,
                        deckName = deckName,
                        viewModel = viewModel,
                    )
                )
            }
            is VoiceCommand.Next -> viewModel.skipCard()
            is VoiceCommand.Cancel -> {
                viewModel.endSession()
                screenManager.push(
                    AutoSessionCompleteScreen(
                        carContext = carContext,
                        deckName = deckName,
                        cardsStudied = viewModel.uiState.value.cardsStudied,
                        correctCount = viewModel.uiState.value.correctCount,
                    )
                )
            }
            else -> { /* Grade commands not applicable on question screen */ }
        }
    }
}
