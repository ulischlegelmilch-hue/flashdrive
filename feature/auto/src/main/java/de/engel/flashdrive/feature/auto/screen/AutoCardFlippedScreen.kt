package de.engel.flashdrive.feature.auto.screen

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.Template
import de.engel.flashdrive.feature.auto.AutoStudyViewModel
import de.engel.flashdrive.feature.auto.voice.VoiceCommand

/**
 * Screen that shows the answer (back side) of the current flashcard.
 * Provides grading actions: "Wusst ich" (correct) and "Nicht gewusst" (incorrect).
 */
class AutoCardFlippedScreen(
    carContext: CarContext,
    private val deckId: Long,
    private val deckName: String,
    private val viewModel: AutoStudyViewModel,
) : Screen(carContext) {

    override fun onGetTemplate(): Template {
        val state = viewModel.uiState.value
        val answer = state.currentAnswer
            ?: return MessageTemplate.Builder("Fehler: Keine Antwort verfügbar")
                .addAction(
                    Action.Builder()
                        .setTitle("Weiter")
                        .setOnClickListener {
                            viewModel.skipCard()
                        }
                        .build()
                )
                .build()

        return MessageTemplate.Builder(answer)
            .setHeaderAction(Action.BACK)
            .setTitle("Antwort • $deckName")
            .addAction(
                Action.Builder()
                    .setTitle("Wusst ich")
                    .setOnClickListener {
                        viewModel.gradeCorrect()
                        navigateNext()
                    }
                    .build()
            )
            .addAction(
                Action.Builder()
                    .setTitle("Nicht gewusst")
                    .setOnClickListener {
                        viewModel.gradeWrong()
                        navigateNext()
                    }
                    .build()
            )
            .build()
    }

    private fun navigateNext() {
        val state = viewModel.uiState.value
        if (state.isFinished) {
            screenManager.push(
                AutoSessionCompleteScreen(
                    carContext = carContext,
                    deckName = deckName,
                    cardsStudied = state.cardsStudied,
                    correctCount = state.correctCount,
                )
            )
        } else {
            screenManager.push(
                AutoStudyScreen(
                    carContext = carContext,
                    deckId = deckId,
                    deckName = deckName,
                    viewModel = viewModel,
                )
            )
        }
    }

    /**
     * Processes a voice command on the flipped card screen.
     */
    fun onVoiceCommand(command: VoiceCommand) {
        when (command) {
            is VoiceCommand.GradeCorrect -> {
                viewModel.gradeCorrect()
                navigateNext()
            }
            is VoiceCommand.GradeWrong -> {
                viewModel.gradeWrong()
                navigateNext()
            }
            is VoiceCommand.Next -> {
                viewModel.skipCard()
                navigateNext()
            }
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
            else -> { /* Flip not applicable here */ }
        }
    }
}
