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
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CallReceiver : BroadcastReceiver() {

    private var recorder: MediaRecorder? = null
    private var outputFilePath: String = ""

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || context == null) return

        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)

        when (state) {
            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                startRecording(context)
            }
            TelephonyManager.EXTRA_STATE_IDLE -> {
                stopRecording()
            }
        }
    }

    private fun startRecording(context: Context) {
        val fileName = "call_recording_${System.currentTimeMillis()}.3gp"
        outputFilePath = "${context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.absolutePath}/$fileName"

        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(outputFilePath)

            try {
                prepare()
                start()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun stopRecording() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
    }
}
