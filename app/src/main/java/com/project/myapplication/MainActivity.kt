package com.project.myapplication

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var audioRecorderHelper: AudioRecorderHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        audioRecorderHelper = AudioRecorderHelper()
        audioRecorderHelper.loadModel(this)

        audioRecorderHelper.startRecording { audioBuffer ->
            val features = extractFeatures(audioBuffer)
            val moodAnalyzer = MoodAnalyzer(this)

            val mood = moodAnalyzer.predictMood(features)

            runOnUiThread {
                updateMoodUI(mood)
            }
        }

        audioRecorderHelper.startClassification { categories ->
            val detectedMood = categories[0].label
            updateMoodUI(detectedMood)
        }
    }

    private fun updateMoodUI(mood: String) {
        when (mood) {
            "Happy" -> {
                Toast.makeText(this, "Happy mood detected!", Toast.LENGTH_SHORT).show()
            }
            "Sad" -> {
                Toast.makeText(this, "Sad mood detected!", Toast.LENGTH_SHORT).show()
            }
            "Angry" -> {
                Toast.makeText(this, "Angry mood detected!", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(this, "Unknown mood detected!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun extractFeatures(audioBuffer: ShortArray): FloatArray {

        val mfccFeatures = FloatArray(13)
        return mfccFeatures
    }
}
