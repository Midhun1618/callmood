package com.project.myapplication

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Environment
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CallReceiver : BroadcastReceiver() {

    private var recorder: MediaRecorder? = null

    override fun onReceive(context: Context, intent: Intent) {
        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)

        if (state == TelephonyManager.EXTRA_STATE_OFFHOOK) {
            Log.d("CallReceiver", "Call started")

            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                startRecording(context)
            } else {
                Log.d("CallReceiver", "RECORD_AUDIO permission not granted. Skipping recording.")
            }
        } else if (state == TelephonyManager.EXTRA_STATE_IDLE) {
            Log.d("CallReceiver", "Call ended")
            stopRecording()
        }
    }

    private fun startRecording(context: Context) {
        try {
            val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            if (outputDir != null && !outputDir.exists()) {
                outputDir.mkdirs()
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val outputFile = File(outputDir, "call_recording_$timestamp.3gp")

            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }

            Log.d("CallReceiver", "Recording started at ${outputFile.absolutePath}")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("CallReceiver", "Recording failed: ${e.message}")
        }
    }

    private fun stopRecording() {
        try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null
            Log.d("CallReceiver", "Recording stopped")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("CallReceiver", "Stop recording failed: ${e.message}")
        }
    }
}
