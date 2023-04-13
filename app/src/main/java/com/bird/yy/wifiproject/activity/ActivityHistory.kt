package com.bird.yy.wifiproject.activity

import android.os.Bundle
import com.bird.yy.wifiproject.adapter.HistoryAdapter
import com.bird.yy.wifiproject.base.BaseActivity
import com.bird.yy.wifiproject.databinding.ActivityHistoryBinding
import com.bird.yy.wifiproject.entity.HistoryEntity
import com.bird.yy.wifiproject.utils.SPUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class ActivityHistory : BaseActivity<ActivityHistoryBinding>() {
    private val historyAdapter = HistoryAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.historyRv.adapter = historyAdapter
        initData()
        binding.arrowBack.setOnClickListener {
            finish()
        }
    }

    private fun initData() {
        val historyEntityLisJson = SPUtils.get().getString("history", "")
        var historyEntityList = arrayListOf<HistoryEntity>()
        if (historyEntityLisJson != null && historyEntityLisJson.isNotEmpty()) {
            val type: Type = object : TypeToken<List<HistoryEntity?>?>() {}.type
            historyEntityList = Gson().fromJson(historyEntityLisJson.toString(), type)
        }
        historyAdapter.setNewData(historyEntityList)
    }
}