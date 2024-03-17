package com.example.kotlinzavrsni

import android.annotation.SuppressLint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class napredak : AppCompatActivity(), SensorEventListener {
    private var mSensorManager: SensorManager? = null
    private var stepSensor: Sensor? = null
    private var totalSteps = 0
    private var previewsTotalSteps = 0
    private var progressBar: ProgressBar? = null
    private var steps: TextView? = null
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.napredak)
        progressBar = findViewById(R.id.progressBar)
        steps = findViewById(R.id.Steps)
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = mSensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    }

    override fun onResume() {
        super.onResume()
        if (stepSensor == null) {
            Toast.makeText(this, "This Device has no sensor", Toast.LENGTH_SHORT).show()
        } else {
            mSensorManager!!.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        mSensorManager!!.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            totalSteps = event.values[0].toInt()
            val currentSteps = totalSteps - previewsTotalSteps
            steps!!.text = currentSteps.toString()
            progressBar!!.progress = currentSteps
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, i: Int) {}
    private fun resetSteps() {
        steps!!.setOnClickListener {
            Toast.makeText(
                this@napredak,
                "Long press to reset steps",
                Toast.LENGTH_SHORT
            ).show()
        }
        steps!!.setOnLongClickListener {
            previewsTotalSteps = totalSteps
            steps!!.text = "0"
            progressBar!!.progress = 0
            saveData()
            true
        }
    }

    private fun saveData() {
        val sharedPef = getSharedPreferences("mypref", MODE_PRIVATE)
        val editor = sharedPef.edit()
        editor.putString("key1", previewsTotalSteps.toString())
        editor.apply()
    }

    private fun loadData() {
        val sharedPref = getSharedPreferences("mypref", MODE_PRIVATE)
        val savedNumber = sharedPref.getFloat("key1", 0f).toInt()
        previewsTotalSteps = savedNumber
    }
}
//2. pokusaj Aktivnost za brojanje koraka akcelerometra