package com.example.kotlinzavrsni

import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class StepsHistoryActivity : AppCompatActivity() {

    private lateinit var mStepsDBHelper: StepsDBHelper
    private lateinit var mSensorListView: ListView
    private lateinit var mListAdapter: ListAdapter
    private lateinit var mStepCountList: ArrayList<DateStepsModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pedometerlist_layout)
        mSensorListView = findViewById(R.id.steps_list)

        getDataForList()

        mListAdapter = ListAdapter(mStepCountList, this)
        mSensorListView.adapter = mListAdapter

        val stepsIntent = Intent(applicationContext, StepsService::class.java)
        startService(stepsIntent)
    }

    private fun getDataForList() {
        mStepsDBHelper = StepsDBHelper(this)
        mStepCountList = mStepsDBHelper.readStepsEntries()
    }
}