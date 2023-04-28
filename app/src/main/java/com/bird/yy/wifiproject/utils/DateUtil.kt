package com.bird.yy.wifiproject.utils

import android.annotation.SuppressLint
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date

class DateUtil {
    @SuppressLint("SimpleDateFormat")
    fun getTime(): String {
        val simpleDateFormat = SimpleDateFormat("MM-dd \nhh:mm")
        val curDate = Date(System.currentTimeMillis())
        Log.d("xxxxxx",System.currentTimeMillis().toString()+"")
        return simpleDateFormat.format(curDate)
    }
    fun getTimeDay(): String{
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val curDate = Date(System.currentTimeMillis())
        return simpleDateFormat.format(curDate)
    }
}