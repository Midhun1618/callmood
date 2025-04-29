package com.project.myapplication

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import org.tensorflow.lite.support.audio.TensorAudio
import org.tensorflow.lite.task.audio.classifier.AudioClassifier
import org.tensorflow.lite.support.label.Category
import java.util.Timer
import java.util.TimerTask

class AudioRecorderHelper {
    private var isRecording = false
    private var isModelLoaded = false  // To track if the model is loaded
    private lateinit var audioClassifier: AudioClassifier
    private lateinit var tensorAudio: TensorAudio
    private var classificationTimer: Timer? = null
    private var tfliteAudioRecord: AudioRecord? = null

    private val sampleRate = 16000
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    // Load the model and prepare the audio classifier and tensor audio
    fun loadModel(context: Context) {
        try {
            // Load the model from the assets directory asynchronously
            audioClassifier = AudioClassifier.createFromFile(context, "mood_model.tflite")

            // Initialize tensorAudio and AudioRecord
            tensorAudio = audioClassifier.createInputTensorAudio()
            tfliteAudioRecord = audioClassifier.createAudioRecord()

            // Mark the model as loaded
            isModelLoaded = true
            Log.d("AudioRecorderHelper", "Model loaded successfully")
        } catch (e: Exception) {
            Log.e("AudioRecorderHelper", "Model loading failed: ${e.message}")
            e.printStackTrace()
        }
    }

    // Start recording audio
    fun startRecording(onAudioBufferReceived: (ShortArray) -> Unit) {
        if (!isModelLoaded) {
            Log.e("AudioRecorderHelper", "Model is not loaded. Please load the model first.")
            return
        }

        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        isRecording = true
        audioRecord.startRecording()

        Thread {
            val buffer = ShortArray(bufferSize)
            while (isRecording) {
                val read = audioRecord.read(buffer, 0, buffer.size)
                if (read > 0) {
                    onAudioBufferReceived(buffer.copyOf(read))
                }
            }
            audioRecord.stop()
            audioRecord.release()
        }.start()
    }

    // Stop the recording process
    fun stopRecording() {
        isRecording = false
    }

    // Start classification of the audio
    fun startClassification(onResult: (List<Category>) -> Unit) {
        if (!isModelLoaded) {
            Log.e("AudioRecorderHelper", "Model is not loaded. Please load the model first.")
            return
        }

        try {
            // Start the audio recording using the AudioRecord instance
            tfliteAudioRecord?.startRecording()

            // Set up a timer to classify audio every second
            classificationTimer = Timer()
            classificationTimer?.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    try {
                        // Load audio data into the TensorAudio object
                        tensorAudio.load(tfliteAudioRecord)

                        // Classify the audio data
                        val results = audioClassifier.classify(tensorAudio)
                        val categories = results[0].categories

                        // Return the classification result via the onResult callback
                        onResult(categories)
                    } catch (e: Exception) {
                        Log.e("AudioRecorderHelper", "Classification error: ${e.message}")
                    }
                }
            }, 0, 1000) // Classification every second
        } catch (e: Exception) {
            Log.e("AudioRecorderHelper", "Error starting classification: ${e.message}")
        }
    }

    // Stop classification of the audio
    fun stopClassification() {
        classificationTimer?.cancel()
        classificationTimer = null
        try {
            tfliteAudioRecord?.stop()
        } catch (e: Exception) {
            Log.e("AudioRecorderHelper", "Error stopping classification: ${e.message}")
        }
    }

    // Function to check if the model is loaded
    fun isModelLoaded(): Boolean {
        return isModelLoaded
    }
}
