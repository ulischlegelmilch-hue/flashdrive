package de.engel.flashdrive.feature.auto.screen

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.Template

/**
 * Final screen that displays session statistics after a study session ends.
 * Shows cards reviewed, correct count, and accuracy percentage.
 * Provides a single "Fertig" action to return to the deck list.
 */
class AutoSessionCompleteScreen(
    carContext: CarContext,
    private val deckName: String,
    private val cardsStudied: Int,
    private val correctCount: Int,
) : Screen(carContext) {

    override fun onGetTemplate(): Template {
        val accuracy = if (cardsStudied > 0) {
            (correctCount * 100) / cardsStudied
        } else {
            0
        }

        val statsMessage = buildString {
            appendLine("Session beendet: $deckName")
            appendLine()
            appendLine("Gelernt: $cardsStudied Karten")
            appendLine("Richtig: $correctCount")
            appendLine("Genauigkeit: $accuracy%")
        }

        return MessageTemplate.Builder(statsMessage)
            .setTitle("FlashDrive – Fertig")
            .addAction(
                Action.Builder()
                    .setTitle("Fertig")
                    .setOnClickListener {
                        // Pop back to the deck list screen.
                        screenManager.popToRoot()
                    }
                    .build()
            )
            .build()
    }
}
