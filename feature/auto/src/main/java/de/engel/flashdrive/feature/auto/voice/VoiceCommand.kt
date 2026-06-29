package de.engel.flashdrive.feature.auto.voice

/**
 * Sealed class representing voice commands that can be issued
 * during the Android Auto study experience.
 */
sealed class VoiceCommand {

    /** Flip the current card to reveal the answer. */
    data object Flip : VoiceCommand()

    /** Grade the current card as correct ("yes", "richtig", "wusst ich"). */
    data object GradeCorrect : VoiceCommand()

    /** Grade the current card as incorrect ("wrong", "falsch", "nicht gewusst"). */
    data object GradeWrong : VoiceCommand()

    /** Skip to the next card ("next", "nächste", "überspringen"). */
    data object Next : VoiceCommand()

    /** Cancel and end the current session ("cancel", "stop", "ende"). */
    data object Cancel : VoiceCommand()
}
