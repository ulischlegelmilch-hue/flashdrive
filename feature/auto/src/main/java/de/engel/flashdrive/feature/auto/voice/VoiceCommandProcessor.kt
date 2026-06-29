package de.engel.flashdrive.feature.auto.voice

import java.util.Locale

/**
 * Parses raw voice input text into structured [VoiceCommand] objects.
 * Supports both German and English voice commands for the car experience.
 */
object VoiceCommandProcessor {

    /**
     * Parses a voice input string into a [VoiceCommand].
     *
     * @param input The raw voice input text (case-insensitive).
     * @return The corresponding [VoiceCommand], or null if no match found.
     */
    fun parse(input: String): VoiceCommand? {
        val normalized = input.trim().lowercase(Locale.GERMAN)

        return when {
            // Flip / reveal commands
            normalized in FLIP_COMMANDS -> VoiceCommand.Flip

            // Correct / yes commands
            normalized in CORRECT_COMMANDS -> VoiceCommand.GradeCorrect

            // Wrong / no commands
            normalized in WRONG_COMMANDS -> VoiceCommand.GradeWrong

            // Next / skip commands
            normalized in NEXT_COMMANDS -> VoiceCommand.Next

            // Cancel / stop commands
            normalized in CANCEL_COMMANDS -> VoiceCommand.Cancel

            else -> null
        }
    }

    private val FLIP_COMMANDS = setOf(
        "flip",
        "zeigen",
        "zeig",
        "aufdecken",
        "decke auf",
        "reveal",
        "show",
    )

    private val CORRECT_COMMANDS = setOf(
        "yes",
        "ja",
        "richtig",
        "wusst ich",
        "correct",
        "right",
        "gut",
    )

    private val WRONG_COMMANDS = setOf(
        "wrong",
        "falsch",
        "nicht gewusst",
        "nein",
        "no",
        "incorrect",
        "falsch gewusst",
    )

    private val NEXT_COMMANDS = setOf(
        "next",
        "nächste",
        "nächste karte",
        "überspringen",
        "skip",
        "weiter",
    )

    private val CANCEL_COMMANDS = setOf(
        "cancel",
        "stop",
        "stopp",
        "ende",
        "beenden",
        "abbrechen",
    )
}
