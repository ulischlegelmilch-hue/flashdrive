package de.engel.flashdrive.feature.study

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Thin wrapper around Android's [TextToSpeech] engine configured for German output.
 *
 * Usage:
 * ```
 * val tts = TtsManager(context)
 * tts.speak("Hallo Welt")
 * // …
 * tts.shutdown() // call from onDispose / onDestroy
 * ```
 *
 * The manager is safe to call from any thread. [speak] will queue text if the
 * engine has not finished initialising yet.
 */
class TtsManager(context: Context) {

    private val tts: TextToSpeech
    private val isReady = AtomicBoolean(false)
    private val pendingQueue = ArrayDeque<String>()

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.language = Locale.GERMAN
                tts.setSpeechRate(0.9f)
                isReady.set(true)
                // Flush any text that arrived before initialisation completed.
                synchronized(pendingQueue) {
                    pendingQueue.forEach { text -> tts.speak(text, TextToSpeech.QUEUE_ADD, null, text.hashCode().toString()) }
                    pendingQueue.clear()
                }
            }
        }
    }

    /**
     * Speaks the given [text] in German.
     *
     * If the TTS engine is not ready yet the text is queued and spoken once
     * initialisation completes.
     */
    fun speak(text: String) {
        if (text.isBlank()) return
        if (!isReady.get()) {
            synchronized(pendingQueue) { pendingQueue.addLast(text) }
            return
        }
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, text.hashCode().toString())
    }

    /**
     * Stops any ongoing speech output immediately.
     */
    fun stop() {
        if (isReady.get()) {
            tts.stop()
        }
    }

    /**
     * Releases the TTS engine. Call this when the screen / component is
     * destroyed to free resources.
     */
    fun shutdown() {
        tts.stop()
        tts.shutdown()
        isReady.set(false)
    }

    /** Whether the TTS engine has finished initialising. */
    val ready: Boolean get() = isReady.get()
}
