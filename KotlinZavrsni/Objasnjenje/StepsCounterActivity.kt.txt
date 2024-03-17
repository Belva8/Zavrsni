package com.example.maturalniradbrojackoraka

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class BrojacKorakaAktivnost : AppCompatActivity(), SensorEventListener {
    private var mSensorManager: SensorManager? = null
    private var mSensor: Sensor? = null
    private var isSensorPresent = false
    private var mKoraciOdResetiranja: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.brojac_koraka)
        mKoraciOdResetiranja = findViewById<View>(R.id.KoraciOdResetiranja) as TextView
        mSensorManager = this.getSystemService(SENSOR_SERVICE) as SensorManager
        if (mSensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null) {
            mSensor = mSensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
            isSensorPresent = true
        } else {
            isSensorPresent = false
        }
    }

    override fun onResume() {
        super.onResume()
        if (isSensorPresent) {
            mSensorManager!!.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        if (isSensorPresent) {
            mSensorManager!!.unregisterListener(this)
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        mKoraciOdResetiranja!!.text = "Koraci od resetiranja : " + event.values[0].toString()
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    override fun onDestroy() {
        super.onDestroy()
        mSensor = null
        mSensorManager = null
    }
}
