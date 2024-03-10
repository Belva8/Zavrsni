package com.example.maturalniradbrojackoraka

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.os.SystemClock
import java.util.Calendar
import java.util.Collections
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.math.pow

class UslugaPracenjeKoraka  : Service() {
    private var mSensorManager: SensorManager? = null
    private var mStepDetectorSensor: Sensor? = null
    private var mAccelerometerSensor: Sensor? = null
    private var mAccelerometerListener: AccelerometerListener? = null
    private var mStepDetectorListener: StepDetectorListener? = null
    private var mStepsTrackerDBHelper: PracenjeKorakaBP? = null

    override fun onCreate() {
        super.onCreate()
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        if (mSensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null) {
            mStepDetectorSensor = mSensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
            mStepDetectorListener = StepDetectorListener()
            mSensorManager!!.registerListener(
                mStepDetectorListener,
                mStepDetectorSensor,
                SensorManager.SENSOR_DELAY_FASTEST
            )
        }
        if (mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            mAccelerometerSensor = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        }
        mStepsTrackerDBHelper = PracenjeKorakaBP(this)
    }

    private val mScheduledExecutorService = Executors.newScheduledThreadPool(2)
    private var mScheduledUnregisterAccelerometerTask: ScheduledFuture<*>? = null
    private lateinit var mScheduledProcessDataTask: ScheduledFuture<*>
    private var mUnregisterAcceleromterTask: UnregisterAcceleromterTask? = null
    private lateinit var mProcessDataTask: ProcessDataTask
    private var isScheduleUnregistered = false
    private var isAccelerometerRegistered = false
    private var sessionId: String? = null

    inner class StepDetectorListener : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (!isAccelerometerRegistered && mAccelerometerSensor != null) {
                mAccelerometerListener = AccelerometerListener()
                mSensorManager!!.registerListener(
                    mAccelerometerListener,
                    mAccelerometerSensor,
                    SensorManager.SENSOR_DELAY_FASTEST
                )
                sessionId = Calendar.getInstance().time.toString()
                isAccelerometerRegistered = true
            }
            if (isScheduleUnregistered) {
                mScheduledUnregisterAccelerometerTask!!.cancel(true)
            }
            mUnregisterAcceleromterTask = UnregisterAcceleromterTask()
            mScheduledUnregisterAccelerometerTask = mScheduledExecutorService.schedule(
                mUnregisterAcceleromterTask,
                20000,
                TimeUnit.MILLISECONDS
            )
            isScheduleUnregistered = true
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    inner class UnregisterAcceleromterTask : Runnable {
        override fun run() {
            isAccelerometerRegistered = false
            mSensorManager!!.unregisterListener(mAccelerometerListener)
            isScheduleUnregistered = false
            mScheduledProcessDataTask.cancel(false)
        }
    }

    private var timeOffsetValue: Long = 0
    var mAccelerometerDataList: ArrayList<Akcelerometar> = ArrayList()
    var mRawDataList: ArrayList<Akcelerometar> = ArrayList()
    var mAboveThresholdValuesList: ArrayList<Akcelerometar> = ArrayList()
    var mHighestPeakList: ArrayList<Akcelerometar> = ArrayList()

    inner class AccelerometerListener : SensorEventListener {
        init {
            mProcessDataTask = ProcessDataTask()
            mScheduledProcessDataTask = mScheduledExecutorService.scheduleWithFixedDelay(
                mProcessDataTask,
                10000,
                10000,
                TimeUnit.MILLISECONDS
            )
        }

        override fun onSensorChanged(event: SensorEvent) {
            val mAccelerometerData = Akcelerometar()
            mAccelerometerData.x = event.values[0]
            mAccelerometerData.y = event.values[1]
            mAccelerometerData.z = event.values[2]
            mAccelerometerData.time = event.timestamp
            mAccelerometerDataList.add(mAccelerometerData)
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    inner class ProcessDataTask : Runnable {
        override fun run() {

            // Kopira podatke Akcelerometara od glavnog senzora  polja u posebno polje za procesiranje
            mRawDataList.addAll(mAccelerometerDataList)
            mAccelerometerDataList.clear()

            //Izračunavanje veličine (kvadratni korijen zbroja kvadrata x, y, z) i pretvaranje vremena iz nano sekundi iz vremena pokretanja u vrijeme epoc
            timeOffsetValue = System.currentTimeMillis() - SystemClock.elapsedRealtime()
            val dataSize = mRawDataList.size
            for (i in 0 until dataSize) {
                mRawDataList[i].value = Math.sqrt(
                    mRawDataList[i].x.toDouble().pow(2.0) + mRawDataList[i].y.toDouble().pow(2.0) + mRawDataList[i].z.toDouble().pow(2.0)
                )
                mRawDataList[i].time = mRawDataList[i].time / 1000000L + timeOffsetValue
            }

            //Racuna vrhunce
            findHighPeaks()
            //Uklonite Vrhunce koji su blizu jedan drugome unutar raspona od 0,4 sekunde
            removeClosePeaks()
            //Pronađite vrstu koraka (trčanje, jogging, hodanje) i pohranite u bazu podataka
            findStepTypeAndStoreInDB()
            mRawDataList.clear()
            mAboveThresholdValuesList.clear()
            mHighestPeakList.clear()
        }

        fun findHighPeaks() {
            //Racuna Vrhunce
            var isAboveMeanLastValueTrue = false
            val dataSize = mRawDataList.size
            for (i in 0 until dataSize) {
                isAboveMeanLastValueTrue = if (mRawDataList[i].value > WALKINGPEAK) {
                    mAboveThresholdValuesList.add(mRawDataList[i])
                    false
                } else {
                    if (!isAboveMeanLastValueTrue && mAboveThresholdValuesList.size > 0) {
                        Collections.sort(mAboveThresholdValuesList, DataSorter())
                        mHighestPeakList.add(mAboveThresholdValuesList[mAboveThresholdValuesList.size - 1])
                        mAboveThresholdValuesList.clear()
                    }
                    true
                }
            }
        }

        fun removeClosePeaks() {
            for (i in 0 until mHighestPeakList.size - 1) {
                if (mHighestPeakList[i].isRealPeak) {
                    if (mHighestPeakList[i + 1].time - mHighestPeakList[i].time < 400) {
                        if (mHighestPeakList[i + 1].value > mHighestPeakList[i].value) {
                            mHighestPeakList[i].isRealPeak = false
                        } else {
                            mHighestPeakList[i + 1].isRealPeak = false
                        }
                    }
                }
            }
        }

        fun findStepTypeAndStoreInDB() {
            val size = mHighestPeakList.size
            for (i in 0 until size) {
                if (mHighestPeakList[i].isRealPeak) {
                    if (mHighestPeakList[i].value > RUNNINGPEAK) {
                        mStepsTrackerDBHelper?.kreirajUnosKoraka(
                            mHighestPeakList[i].time,
                            RUNNING,
                            sessionId!!
                        )
                    } else {
                        if (mHighestPeakList[i].value > JOGGINGPEAK) {
                            mStepsTrackerDBHelper?.kreirajUnosKoraka(
                                mHighestPeakList[i].time,
                                JOGGING,
                                sessionId!!
                            )
                        } else {
                            mStepsTrackerDBHelper?.kreirajUnosKoraka(
                                mHighestPeakList[i].time,
                                WALKING,
                                sessionId!!
                            )
                        }
                    }
                }
            }
        }

        inner class DataSorter : Comparator<Akcelerometar?> {
            override fun compare(obj1: Akcelerometar?, obj2: Akcelerometar?): Int {
                return if (obj1!!.value < obj2!!.value) {
                    -1
                } else if (obj1.value > obj2.value) {
                    1
                } else {
                    0
                }
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mScheduledExecutorService.shutdown()
    }

    companion object {
        private const val WALKINGPEAK = 18
        private const val JOGGINGPEAK = 25
        private const val RUNNINGPEAK = 32
        private const val RUNNING = 3
        private const val JOGGING = 2
        private const val WALKING = 1
    }
}

