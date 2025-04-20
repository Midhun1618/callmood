package com.project.myapplication

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import org.tensorflow.lite.support.audio.TensorAudio
import org.tensorflow.lite.task.audio.classifier.AudioClassifier
import java.util.Timer
import java.util.TimerTask
import org.tensorflow.lite.support.label.Category

class AudioRecorderHelper {
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var recordingThread: Thread? = null
    private lateinit var audioClassifier: AudioClassifier
    private lateinit var tensorAudio: TensorAudio
    private var timer: Timer? = null

    private val sampleRate = 16000
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    fun loadModel(context: Context) {
        audioClassifier = AudioClassifier.createFromFile(context, "mood_model.tflite")
        tensorAudio = audioClassifier.createInputTensorAudio()
    }

    fun startRecording(onAudioBufferReceived: (ShortArray) -> Unit) {
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        audioRecord?.startRecording()
        isRecording = true

        recordingThread = Thread {
            val buffer = ShortArray(bufferSize)
            while (isRecording) {
                val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (read > 0) {
                    onAudioBufferReceived(buffer.copyOf(read))
                }
            }
        }.also { it.start() }
    }

    fun stopRecording() {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        recordingThread = null
    }

    fun startClassification(onResult: (List<Category>) -> Unit) {
        val audioRecord = audioClassifier.createAudioRecord()
        audioRecord.startRecording()

        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                tensorAudio.load(audioRecord)
                val results = audioClassifier.classify(tensorAudio)
                val categories = results[0].categories
                onResult(categories)
            }
        }, 0, 1000) // Every second
    }

    fun stopClassification() {
        timer?.cancel()
        timer = null
        try {
            audioClassifier.createAudioRecord().stop()
        } catch (e: Exception) {
            Log.e("AudioRecorderHelper", "Error stopping AudioRecord: ${e.message}")
        }
    }
}
