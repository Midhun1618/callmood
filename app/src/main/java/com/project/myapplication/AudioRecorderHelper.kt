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

    fun loadModel(context: Context) {
        try {
            audioClassifier = AudioClassifier.createFromFile(context, "mood_model.tflite")
            tensorAudio = audioClassifier.createInputTensorAudio()
            tfliteAudioRecord = audioClassifier.createAudioRecord()
        } catch (e: Exception) {
            Log.e("AudioRecorderHelper", "Model loading failed: ${e.message}")
        }
    }

    fun startRecording(onAudioBufferReceived: (ShortArray) -> Unit) {
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

    fun stopRecording() {
        isRecording = false
    }

    fun startClassification(onResult: (List<Category>) -> Unit) {
        try {
            tfliteAudioRecord?.startRecording()

            classificationTimer = Timer()
            classificationTimer?.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    try {
                        tensorAudio.load(tfliteAudioRecord)
                        val results = audioClassifier.classify(tensorAudio)
                        val categories = results[0].categories
                        onResult(categories)
                    } catch (e: Exception) {
                        Log.e("AudioRecorderHelper", "Classification error: ${e.message}")
                    }
                }
            }, 0, 1000) // Every second
        } catch (e: Exception) {
            Log.e("AudioRecorderHelper", "Error starting classification: ${e.message}")
        }
    }

    fun stopClassification() {
        classificationTimer?.cancel()
        classificationTimer = null
        try {
            tfliteAudioRecord?.stop()
        } catch (e: Exception) {
            Log.e("AudioRecorderHelper", "Error stopping classification: ${e.message}")
        }
    }
}
