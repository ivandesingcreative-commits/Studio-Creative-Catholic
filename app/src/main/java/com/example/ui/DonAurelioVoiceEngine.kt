package com.example.ui

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

sealed class PlaybackState {
    object Idle : PlaybackState()
    object Initializing : PlaybackState()
    data class Playing(val currentParagraphIndex: Int = 0) : PlaybackState()
    object Paused : PlaybackState()
    data class Stopped(val error: String? = null) : PlaybackState()
}

class DonAurelioVoiceEngine(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    val playbackState: StateFlow<PlaybackState> = _playbackState

    private var currentParagraphs: List<String> = emptyList()
    private var isInitialized = false

    init {
        _playbackState.value = PlaybackState.Initializing
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("es", "ES"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to generic Spanish
                tts?.setLanguage(Locale("es"))
            }

            // High-dignity adjustments for solemnity:
            tts?.setPitch(0.76f)       // Deep, low frequency (masculine, dignified)
            tts?.setSpeechRate(0.80f)  // Slow, reverent pace suited for traditional homilies

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    val index = utteranceId?.substringAfter("para_", "0")?.toIntOrNull() ?: 0
                    _playbackState.value = PlaybackState.Playing(index)
                }

                override fun onDone(utteranceId: String?) {
                    // Check if this is the absolute last utterance or silence in queue
                    if (utteranceId?.contains("silence_") == true) {
                        val index = utteranceId.substringAfter("silence_", "").toIntOrNull() ?: 0
                        if (index >= currentParagraphs.size - 1) {
                            _playbackState.value = PlaybackState.Idle
                        }
                    } else if (utteranceId?.startsWith("para_") == true) {
                        val index = utteranceId.substringAfter("para_", "").toIntOrNull() ?: 0
                        if (index >= currentParagraphs.size - 1 && currentParagraphs.size == 1) {
                            _playbackState.value = PlaybackState.Idle
                        }
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _playbackState.value = PlaybackState.Stopped("Error general de vocalización.")
                    Log.e("DonAurelioVoice", "Error speaking $utteranceId")
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _playbackState.value = PlaybackState.Stopped("Fallo en la locución. Código de error: $errorCode")
                    Log.e("DonAurelioVoice", "Error speaking $utteranceId, code: $errorCode")
                }
            })

            isInitialized = true
            _playbackState.value = PlaybackState.Idle
            Log.d("DonAurelioVoice", "Interprete Don Aurelio listo con modulación tradicionalista.")
        } else {
            isInitialized = false
            _playbackState.value = PlaybackState.Stopped("El motor de síntesis de voz no se pudo inicializar en el dispositivo.")
            Log.e("DonAurelioVoice", "Fallo inicial del TTS.")
        }
    }

    fun speak(text: String) {
        if (!isInitialized || tts == null) {
            _playbackState.value = PlaybackState.Stopped("El motor de Don Aurelio no está preparado.")
            return
        }

        // Split scripts into paragraphs to allow clean pauses between ideas and bypass size limits of TTS engine
        currentParagraphs = text.split("\n").map { it.trim() }.filter { it.isNotBlank() }
        
        tts?.stop()

        if (currentParagraphs.isEmpty()) {
            val params = Bundle()
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "para_0")
            _playbackState.value = PlaybackState.Playing(0)
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "para_0")
            return
        }

        _playbackState.value = PlaybackState.Playing(0)

        currentParagraphs.forEachIndexed { index, paragraph ->
            val pBundle = Bundle()
            pBundle.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "para_$index")
            val queueMode = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            
            // Narrate paragraph
            tts?.speak(paragraph, queueMode, pBundle, "para_$index")
            
            // Introduce deliberate solemn pausing of 1.7 seconds to let theological concepts resonate
            tts?.playSilentUtterance(1700, TextToSpeech.QUEUE_ADD, "silence_$index")
        }
    }

    fun pause() {
        stop()
        _playbackState.value = PlaybackState.Paused
    }

    fun stop() {
        tts?.stop()
        _playbackState.value = PlaybackState.Idle
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
