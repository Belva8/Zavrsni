package com.example.maturalniradbrojackoraka

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class RezultatiAktivnost  : AppCompatActivity() {

    private lateinit var mTotalStepsTextView: TextView
    private lateinit var mTotalDistanceTextView: TextView
    private lateinit var mTotalDurationTextView: TextView
    private lateinit var mAverageSpeedTextView: TextView
    private lateinit var mAveragFrequencyTextView: TextView
    private lateinit var mTotalCalorieBurnedTextView: TextView
    private lateinit var mPhysicalActivityTypeTextView: TextView
    private lateinit var mStepsTrackerDBHelper: PracenjeKorakaBP

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.statistika_layout)

        mStepsTrackerDBHelper = PracenjeKorakaBP(this)

        mTotalStepsTextView = findViewById(R.id.UkupniKoraci)
        mTotalDistanceTextView = findViewById(R.id.UkupnaUdaljenost)
        mTotalDurationTextView = findViewById(R.id.UkupnoTrajanje)
        mAverageSpeedTextView = findViewById(R.id.ProsjecnaBrzina)
        mAveragFrequencyTextView = findViewById(R.id.ProsjecnaFrekvencija)
        mTotalCalorieBurnedTextView = findViewById(R.id.KalorijaPotroseno)
        mPhysicalActivityTypeTextView = findViewById(R.id.TipFizickeAktivnosti)

        val stepsAnalysisIntent = Intent(applicationContext, UslugaPracenjeKoraka::class.java)
        startService(stepsAnalysisIntent)

        calculateDataMatrix()
    }

    private fun calculateDataMatrix() {
        val calendar = Calendar.getInstance()
        val todayDate = "${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(
            Calendar.YEAR)}"
        val stepType = mStepsTrackerDBHelper.DohvatiKorakePoDatumu(todayDate)
        val walkingSteps = stepType[0]
        val joggingSteps = stepType[1]
        val runningSteps = stepType[2]

        // Racunanje Ukupnih Koraka
        val totalStepTaken = walkingSteps + joggingSteps + runningSteps
        mTotalStepsTextView.text = "$totalStepTaken Steps"

        //Racunanje Ukupne Prehodane udaljenosti
        val totalDistance = walkingSteps * 0.5f + joggingSteps * 1.0f + runningSteps * 1.5f
        mTotalDistanceTextView.text = "$totalDistance meters"

        // Racunanje ukupnog trajanja
        val totalDuration = walkingSteps * 1.0f + joggingSteps * 0.7f + runningSteps * 0.4f
        val hours = totalDuration / 3600
        val minutes = (totalDuration % 3600) / 60
        val seconds = totalDuration % 60
        mTotalDurationTextView.text = "${hours.toInt()} hrs ${minutes.toInt()} mins ${seconds.toInt()} secs"

        // Racunanje prosječne brzine
        mAverageSpeedTextView.text =
            if (totalDistance > 0) "${"%.2f".format(totalDistance / totalDuration)} meter per seconds"
            else "0 meter per seconds"

        // Racunanje frekvencije koraka
        mAveragFrequencyTextView.text =
            if (totalStepTaken > 0) "${"%.0f".format(totalStepTaken / minutes)} steps per minute"
            else "0 steps per minute"

        // Racunanje potrosenih kalorija
        val totalCaloriesBurned = walkingSteps * 0.05f + joggingSteps * 0.1f + runningSteps * 0.2f
        mTotalCalorieBurnedTextView.text = "${totalCaloriesBurned.toInt()} Calories"

        // Racunanje Vrstu fizicke aktivnosti
        mPhysicalActivityTypeTextView.text =
            "$walkingSteps Walking Steps\n$joggingSteps Jogging Steps\n$runningSteps Running Steps"
    }
}
