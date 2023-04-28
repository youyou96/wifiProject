package com.bird.yy.wifiproject.activity

import android.os.Bundle
import com.bird.yy.wifiproject.adapter.HistoryAdapter
import com.bird.yy.wifiproject.base.BaseActivity
import com.bird.yy.wifiproject.databinding.ActivityHistoryBinding
import com.bird.yy.wifiproject.entity.AdBean
import com.bird.yy.wifiproject.entity.HistoryEntity
import com.bird.yy.wifiproject.entity.MessageEvent
import com.bird.yy.wifiproject.manager.AdManage
import com.bird.yy.wifiproject.utils.Constant
import com.bird.yy.wifiproject.utils.SPUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
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
        EventBus.getDefault().register(this)
        loadNativeAd()
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

    private var adManage = AdManage()
    private fun loadNativeAd() {
        val adBean = Constant.AdMap[Constant.adNative_wifi_history]
        if (adBean == null) {
            loadAd()
        } else {
            val time = System.currentTimeMillis() - adBean.saveTime
            if (time > Constant.timeOut || adBean.ad == null) {
                loadAd()
            } else {
                showNativeAd(adBean)
            }
        }
    }

    private fun loadAd() {
        adManage.loadAd(
            Constant.adNative_wifi_history,
            this,
            object : AdManage.OnLoadAdCompleteListener {
                override fun onLoadAdComplete(ad: AdBean?) {
                    if (ad?.ad != null) {
                        showNativeAd(ad)
                    }
                }

                override fun isMax() {

                }
            })
    }

    fun showNativeAd(ad: AdBean) {
        adManage.showAd(
            this,
            Constant.adNative_wifi_history,
            ad,
            binding.adFrameLayout,
            object : AdManage.OnShowAdCompleteListener {
                override fun onShowAdComplete() {
                }

                override fun isMax() {
                }

            })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        if (event.type != Constant.adNative_wifi_s) {
            showNativeAd(event.adBean)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}