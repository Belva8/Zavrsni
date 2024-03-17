package com.example.kotlinzavrsni

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class ListAdapter(private val mStepCountList: ArrayList<DateStepsModel>, private val mContext: Context) : BaseAdapter() {

    private val mLayoutInflater: LayoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return mStepCountList.size
    }

    override fun getItem(position: Int): Any {
        return mStepCountList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.list_rows, parent, false)
        }

        val mDateStepCountText = convertView!!.findViewById<TextView>(R.id.sensor_name)
        mDateStepCountText.text = "${mStepCountList[position].mDate} - Total Steps: ${mStepCountList[position].mStepCount}"

        return convertView
    }
}