package com.example.kotlinzavrsni

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlin.math.sqrt

class StepsCounterActivity : AppCompatActivity(), SensorEventListener {
    private var previousTotalSteps = 0f
    private var totalSteps = 0f
    private var running: Boolean = false
    private var sensorManager: SensorManager? = null
    private val ACTIVITY_RECOGNITION_REQUEST_CODE: Int = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stepcounter)

        if (!isPermissionGranted()) {
            requestPermission()
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACTIVITY_RECOGNITION), ACTIVITY_RECOGNITION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            ACTIVITY_RECOGNITION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // dozvola
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        running = true
        sensorManager?.let { sensorManager ->
            val countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            val detectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
            val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

            when {
                countSensor != null -> sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI)
                detectorSensor != null -> sensorManager.registerListener(this, detectorSensor, SensorManager.SENSOR_DELAY_UI)
                accelerometer != null -> sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
                else -> Toast.makeText(this, "Your device is not compatible", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        running = false
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (running) {
                totalSteps = it.values[0]
                val currentSteps = totalSteps.toInt() - previousTotalSteps.toInt()
                findViewById<TextView>(R.id.StepCounterTV).text = currentSteps.toString()

                if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    val xaccel: Float = it.values[0]
                    val yaccel: Float = it.values[1]
                    val zaccel: Float = it.values[2]
                    val magnitude: Double = sqrt((xaccel * xaccel + yaccel * yaccel + zaccel * zaccel).toDouble())
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not implemented
    }
}
