package com.project.myapplication

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class MoodAnalyzer(context: Context) {
    private val interpreter: Interpreter

    init {
        val model = loadModelFile(context, "mood_model.tflite")
        interpreter = Interpreter(model)
    }

    private fun loadModelFile(context: Context, filename: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(filename)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun predictMood(features: FloatArray): String {
        val input = arrayOf(features)
        val output = Array(1) { FloatArray(3) }
        interpreter.run(input, output)

        val maxIndex = output[0].indices.maxByOrNull { output[0][it] } ?: -1
        return when (maxIndex) {
            0 -> "Happy"
            1 -> "Sad"
            2 -> "Angry"
            else -> "Unknown"
        }
    }

}
