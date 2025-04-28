package com.project.myapplication

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

class MoodAnalyzer(private val context: Context) {

    private var tflite: Interpreter? = null

    init {
        // Load the TensorFlow Lite model from assets folder
        try {
            val model = loadModelFile(context, "mood_model.tflite")
            tflite = Interpreter(model)
        } catch (e: Exception) {
            Log.e("MoodAnalyzer", "Error loading model: ${e.message}")
        }
    }

    // Function to load model from assets
    private fun loadModelFile(context: Context, modelFileName: String): ByteBuffer {
        val assetManager = context.assets
        val fileDescriptor = assetManager.openFd(modelFileName)
        val inputStream: InputStream = fileDescriptor.createInputStream()
        val fileSize = fileDescriptor.length.toInt()

        val byteBuffer = ByteBuffer.allocateDirect(fileSize)
        byteBuffer.order(ByteOrder.nativeOrder())

        // Copy model into ByteBuffer
        val byteArray = ByteArray(fileSize)
        inputStream.read(byteArray)
        byteBuffer.put(byteArray)

        inputStream.close()
        return byteBuffer
    }

    // Function to process the audio file and get the mood
    fun analyzeMood(audioFilePath: String): String {
        val audioData = extractSpectrogram(audioFilePath)  // Process your audio to extract features
        val result = FloatArray(2)  // Assuming the model predicts two classes (positive/negative)

        // Run the inference
        tflite?.run(audioData, result)

        // Return the detected mood (positive or negative)
        return if (result[0] > result[1]) "positive" else "negative"
    }

    // Helper function to extract features (spectrogram/MFCC) from the audio
    private fun extractSpectrogram(audioFilePath: String): ByteBuffer {
        // Process the audio file to extract spectrogram or MFCC features
        val buffer = ByteBuffer.allocateDirect(4 * 64 * 64) // Assuming a 64x64 spectrogram
        buffer.order(ByteOrder.nativeOrder())

        // Add logic here to convert the audio to spectrogram and fill the buffer
        // For now, this is just a placeholder for feature extraction logic

        // Example: fill the buffer with random data (replace with actual feature extraction logic)
        for (i in 0 until 64 * 64) {
            buffer.putFloat(Math.random().toFloat()) // Placeholder, replace with actual audio feature values
        }

        return buffer
    }

    // Release the resources after use
    fun close() {
        tflite?.close()
    }
}
