package com.project.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    lateinit var viewpager : ViewPager2
    lateinit var tab : TabLayout

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewpager =findViewById(R.id.viewpager)
        tab =findViewById(R.id.tablayout)
        viewpager.adapter = SwipeAdapter(this)

        TabLayoutMediator(tab,viewpager){tab,position ->
            val v = SwipeAdapter(this)
            tab.text = v.getTabTitle(position)
        }.attach()

        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        val permissionsNeeded = mutableListOf<String>()
        val permissions = arrayOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECORD_AUDIO
        )

        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(permission)
            }
        }

        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toTypedArray(), PERMISSION_REQUEST_CODE)
        } else {
            startAppFunctionality()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            val deniedPermissions = mutableListOf<String>()

            for (i in permissions.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(permissions[i])
                    Log.d("Permission", "Permission denied: ${permissions[i]}")
                }
            }

            if (deniedPermissions.isEmpty()) {
                startAppFunctionality()
            } else {
                for (permission in deniedPermissions) {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                        Log.d("Permission", "Permission permanently denied: $permission")
                        showPermissionSettingsDialog(permission)
                    }
                }

                if (!deniedPermissions.contains(Manifest.permission.RECORD_AUDIO)) {
                    startLimitedFunctionality()
                } else {
                    Toast.makeText(this, "App needs required permissions to work properly.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showPermissionSettingsDialog(permission: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Permission Required")
        builder.setMessage("The app needs $permission to work properly. Please enable it in settings.")

        builder.setPositiveButton("Go to Settings") { dialog, _ ->
            dialog.dismiss()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun startAppFunctionality() {
        Log.d("App", "Starting FULL functionality...")
        // Full features of your app (e.g., call monitoring + voice recording)
    }

    private fun startLimitedFunctionality() {
        Log.d("App", "Starting LIMITED functionality (no recording)...")
        // App works without RECORD_AUDIO (like only showing call info)
    }
}
