package com.example.kotlinzavrsni

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.util.Calendar

class StepsTrackerDBHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_STEPS_SUMMARY)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_STEPS_SUMMARY")
        onCreate(db)
    }

    fun createStepsEntry(timeStamp: Long, stepType: Int, sessionId: String): Boolean {
        var createSuccessful = false
        val mCalendar = Calendar.getInstance()
        val todayDate =
            "${mCalendar.get(Calendar.MONTH) + 1}/${mCalendar.get(Calendar.DAY_OF_MONTH)}/${mCalendar.get(Calendar.YEAR)}"
        try {
            val db = writableDatabase
            val values = ContentValues().apply {
                put(STEP_TIME, timeStamp)
                put(STEP_DATE, todayDate)
                put(STEP_TYPE, stepType)
                put(SESSION_ID, sessionId)
            }
            val row = db.insert(TABLE_STEPS_SUMMARY, null, values)
            if (row != -1L) {
                createSuccessful = true
            }
            db.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return createSuccessful
    }

    fun getStepsByDate(date: String): IntArray {
        val stepType = IntArray(3)
        val selectQuery =
            "SELECT $STEP_TYPE FROM $TABLE_STEPS_SUMMARY WHERE $STEP_DATE = ?"
        var db: SQLiteDatabase? = null
        var c: Cursor? = null
        try {
            db = this.readableDatabase
            c = db.rawQuery(selectQuery, arrayOf(date))
            if (c != null && c.moveToFirst()) {
                val stepTypeIndex = c.getColumnIndex(STEP_TYPE)
                if (stepTypeIndex != -1) {
                    do {
                        when (c.getInt(stepTypeIndex)) {
                            WALKING -> ++stepType[0]
                            JOGGING -> ++stepType[1]
                            RUNNING -> ++stepType[2]
                        }
                    } while (c.moveToNext())
                } else {
                    // Log pogreska ako je indeks -1
                    Log.e("StepsTrackerDBHelper", "getColumnIndex returned -1 for STEP_TYPE column")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            // Zatvorit kursos i bazu podataka
            c?.close()
            db?.close()
        }
        return stepType
    }

    @SuppressLint("Range")
    fun getTotalStepsDuration(): Long {
        var totalDuration: Long = 0
        var db: SQLiteDatabase? = null
        var c: Cursor? = null
        try {
            val selectQuery = "SELECT DISTINCT $SESSION_ID FROM $TABLE_STEPS_SUMMARY"
            db = this.readableDatabase
            c = db.rawQuery(selectQuery, null)
            if (c != null && c.moveToFirst()) {
                do {
                    val sessionId = c.getString(c.getColumnIndex(SESSION_ID))
                    val stepTimeSessionList = ArrayList<Int>()
                    val selectTimeQuery =
                        "SELECT $STEP_TIME FROM $TABLE_STEPS_SUMMARY WHERE $SESSION_ID = ?"
                    val cTime = db.rawQuery(selectTimeQuery, arrayOf(sessionId))
                    if (cTime != null && cTime.moveToFirst()) {
                        do {
                            stepTimeSessionList.add(cTime.getInt(cTime.getColumnIndex(STEP_TIME)))
                        } while (cTime.moveToNext())
                        cTime.close()
                    }
                    val sizeStepTimeSessionList = stepTimeSessionList.size
                    for (j in sizeStepTimeSessionList - 1 downTo 1) {
                        totalDuration += (stepTimeSessionList[j] - stepTimeSessionList[j - 1])
                    }
                } while (c.moveToNext())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            c?.close()
            db?.close()
        }
        return totalDuration
    }

    @SuppressLint("Range")
    fun getAvailableDates(): ArrayList<String> {
        val dateList = ArrayList<String>()
        var db: SQLiteDatabase? = null
        var c: Cursor? = null
        try {
            val selectQuery = "SELECT DISTINCT $STEP_DATE FROM $TABLE_STEPS_SUMMARY"
            db = this.readableDatabase
            c = db.rawQuery(selectQuery, null)
            if (c != null && c.moveToFirst()) {
                do {
                    dateList.add(c.getString(c.getColumnIndex(STEP_DATE)))
                } while (c.moveToNext())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            c?.close()
            db?.close()
        }
        return dateList
    }

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "StepsTrackerDatabase"
        private const val TABLE_STEPS_SUMMARY = "StepsTrackerSummary"
        private const val ID = "id"
        private const val STEP_TYPE = "steptype"
        private const val STEP_TIME = "steptime"
        private const val STEP_DATE = "stepdate"
        private const val SESSION_ID = "sessionid"
        private const val CREATE_TABLE_STEPS_SUMMARY = "CREATE TABLE " +
                "$TABLE_STEPS_SUMMARY ($ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$STEP_DATE TEXT,$SESSION_ID TEXT,$STEP_TIME INTEGER,$STEP_TYPE TEXT)"
        private const val RUNNING = 3
        private const val JOGGING = 2
        private const val WALKING = 1
    }
}