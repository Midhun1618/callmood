package com.project.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.speech.RecognizerIntent
import android.speech.RecognitionListener
import android.content.Context
import android.widget.Toast
import android.widget.TextView
import android.widget.Button
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.util.Locale

class HomeFragment : Fragment() {
    lateinit var dialview: TextView
    lateinit var moodLabel: TextView
    lateinit var suggestion: TextView
    lateinit var d1: Button
    lateinit var d2: Button
    lateinit var d3: Button
    lateinit var d4: Button
    lateinit var d5: Button
    lateinit var d6: Button
    lateinit var d7: Button
    lateinit var d8: Button
    lateinit var d9: Button
    lateinit var ac_dial: Button
    lateinit var d0: Button
    lateinit var backspace: Button
    lateinit var dialnow: Button
    private var dialNumber = ""

    // SpeechRecognizer
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var recognitionListener: RecognitionListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        dialview = view.findViewById(R.id.dialview)
        moodLabel = view.findViewById(R.id.moodLabel)
        suggestion = view.findViewById(R.id.suggestion)

        d1 = view.findViewById(R.id.d1)
        d2 = view.findViewById(R.id.d2)
        d3 = view.findViewById(R.id.d3)
        d4 = view.findViewById(R.id.d4)
        d5 = view.findViewById(R.id.d5)
        d6 = view.findViewById(R.id.d6)
        d7 = view.findViewById(R.id.d7)
        d8 = view.findViewById(R.id.d8)
        d9 = view.findViewById(R.id.d9)
        ac_dial = view.findViewById(R.id.ac_dial)
        d0 = view.findViewById(R.id.d0)
        backspace = view.findViewById(R.id.backspace)
        dialnow = view.findViewById(R.id.dialnow)

        // Initialize SpeechRecognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        recognitionListener = object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val result = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spokenText = result?.get(0) ?: ""
                analyzeSentiment(spokenText)
            }

            override fun onError(error: Int) {
                Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
            }

            override fun onReadyForSpeech(params: Bundle?) {
                  }

            override fun onBeginningOfSpeech() {
                   }

            override fun onRmsChanged(rmsdB: Float) {
                    }

            override fun onBufferReceived(buffer: ByteArray?) {
                    }

            override fun onEndOfSpeech() {
                   }

            override fun onEvent(eventType: Int, params: Bundle?) {
                    }

            override fun onPartialResults(partialResults: Bundle?) {
                 }
        }
        speechRecognizer.setRecognitionListener(recognitionListener)

        d1.setOnClickListener { dialNumber += "1"; dialview.text = dialNumber }
        d2.setOnClickListener { dialNumber += "2"; dialview.text = dialNumber }
        d3.setOnClickListener { dialNumber += "3"; dialview.text = dialNumber }
        d4.setOnClickListener { dialNumber += "4"; dialview.text = dialNumber }
        d5.setOnClickListener { dialNumber += "5"; dialview.text = dialNumber }
        d6.setOnClickListener { dialNumber += "6"; dialview.text = dialNumber }
        d7.setOnClickListener { dialNumber += "7"; dialview.text = dialNumber }
        d8.setOnClickListener { dialNumber += "8"; dialview.text = dialNumber }
        d9.setOnClickListener { dialNumber += "9"; dialview.text = dialNumber }
        d0.setOnClickListener { dialNumber += "0"; dialview.text = dialNumber }

        ac_dial.setOnClickListener {
            dialNumber = ""
            dialview.text = dialNumber
        }

        backspace.setOnClickListener {
            if (dialNumber.isNotEmpty()) {
                dialNumber = dialNumber.substring(0, dialNumber.length - 1)
                dialview.text = dialNumber
            }
        }

        dialnow.setOnClickListener {
            // Start speech recognition
            val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            speechRecognizer.startListening(recognizerIntent)
        }

        return view
    }

    // Basic sentiment analysis
    private fun analyzeSentiment(text: String) {
        val sentimentResult = when {
            text.contains("happy", ignoreCase = true) || text.contains("good", ignoreCase = true) -> "Happy"
            text.contains("sad", ignoreCase = true) || text.contains("worried", ignoreCase = true) -> "Sad"
            text.contains("bored", ignoreCase = true) || text.contains("boring", ignoreCase = true) -> "Boredom"
            text.contains("Wow", ignoreCase = true) || text.contains("my god", ignoreCase = true) -> "Surprise"
            else -> "Neutral"
        }
        val suggestionRes = when {
            text.contains("happy", ignoreCase = true) || text.contains("good", ignoreCase = true) -> "You seem happy! Want to play a feel-good song?"
            text.contains("sad", ignoreCase = true) || text.contains("worried", ignoreCase = true) -> "Feeling low? How about a relaxing lo-fi track to calm you down?"
            text.contains("bored", ignoreCase = true) || text.contains("boring", ignoreCase = true) -> "Bored? Let’s play a quick game or check out something fun online!"
            text.contains("Wow", ignoreCase = true) || text.contains("my god", ignoreCase = true) -> "Whoa! That sounds surprising! Want to share it with a friend?"
            else -> "Neutral"
        }

        moodLabel.text = "Mood: $sentimentResult"
        suggestion.text = "$suggestionRes"
    }
}
