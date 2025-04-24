package com.yourpackage.name

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.Environment
import android.telephony.TelephonyManager
import android.util.Log
import java.io.File
import java.io.IOException

class CallReceiver : BroadcastReceiver() {

    private var recorder: MediaRecorder? = null
    private var outputFilePath: String? = null

    override fun onReceive(context: Context, intent: Intent) {
        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        Log.d("CallReceiver", "Phone state changed: $state")

        when (state) {
            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                // Call started
                Log.d("CallReceiver", "Call started — starting mic recording")

                val dir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
                outputFilePath = "${dir?.absolutePath}/call_mood_record.3gp"

                recorder = MediaRecorder().apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC) // Only your voice
                    setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                    setOutputFile(outputFilePath)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                    try {
                        prepare()
                        start()
                        Log.d("CallReceiver", "Recording started at $outputFilePath")
                    } catch (e: IOException) {
                        Log.e("CallReceiver", "Recording error: ${e.message}")
                    }
                }
            }

            TelephonyManager.EXTRA_STATE_IDLE -> {
                // Call ended
                Log.d("CallReceiver", "Call ended — stopping recording")

                recorder?.apply {
                    try {
                        stop()
                        release()
                        Log.d("CallReceiver", "Recording stopped")
                        // You can now analyze the file at outputFilePath
                    } catch (e: Exception) {
                        Log.e("CallReceiver", "Stop recording error: ${e.message}")
                    }
                }
                recorder = null
            }
        }
    }
}
