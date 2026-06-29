package de.engel.flashdrive.feature.auto

import android.speech.tts.TextToSpeech
import androidx.car.app.CarContext
import java.util.Locale

/**
 * Manages Text-To-Speech output for the Android Auto experience.
 * Uses Locale.GERMAN for all spoken output.
 *
 * Android Auto does not support Compose TTS; this wraps the platform TextToSpeech
 * engine configured for the car context.
 */
class AutoTtsManager(
    private val carContext: CarContext,
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isReady = false
    private var pendingText: String? = null

    init {
        tts = TextToSpeech(carContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.GERMAN
            isReady = true
            pendingText?.let {
                speak(it)
                pendingText = null
            }
        }
    }

    /**
     * Speaks the given text. If TTS is not yet initialized,
     * the text is queued and spoken once ready.
     */
    fun speak(text: String) {
        if (!isReady) {
            pendingText = text
            return
        }
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "flashdrive_${System.currentTimeMillis()}")
    }

    /**
     * Speaks a question prompt.
     */
    fun speakQuestion(text: String) {
        speak("Frage: $text")
    }

    /**
     * Speaks an answer reveal.
     */
    fun speakAnswer(text: String) {
        speak("Antwort: $text")
    }

    /**
     * Speaks session completion stats.
     */
    fun speakSessionComplete(cardsStudied: Int, correctCount: Int) {
        val accuracy = if (cardsStudied > 0) (correctCount * 100) / cardsStudied else 0
        speak("Session beendet. $cardsStudied Karten gelernt. $correctCount richtig. Genauigkeit: $accuracy Prozent.")
    }

    /**
     * Stops speaking and releases TTS resources.
     */
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady = false
    }
}
