package com.example.maturalniradbrojackoraka

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder

class UslugaKoraci : Service(), SensorEventListener {

    private lateinit var mSensorManager: SensorManager
    private var mStepDetectorSensor: Sensor? = null
    private lateinit var mStepsDBHelper: KoraciBP

    override fun onCreate() {
        super.onCreate()

        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null) {
            mStepDetectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
            mSensorManager.registerListener(this, mStepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL)
            mStepsDBHelper = KoraciBP(applicationContext)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            mStepsDBHelper.KreirajUnosKoraka(System.currentTimeMillis())
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        private const val WALKING = 1
        // Druge vrste koraka
    }
}

