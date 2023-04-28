package com.bird.yy.wifiproject.activity

import android.content.Context
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import com.bird.yy.wifiproject.R
import com.bird.yy.wifiproject.base.BaseActivity
import com.bird.yy.wifiproject.databinding.ActivityNetworkTestBinding
import com.bird.yy.wifiproject.entity.AdBean
import com.bird.yy.wifiproject.entity.HistoryEntity
import com.bird.yy.wifiproject.entity.MessageEvent
import com.bird.yy.wifiproject.manager.AdManage
import com.bird.yy.wifiproject.utils.Constant
import com.bird.yy.wifiproject.utils.DateUtil
import com.bird.yy.wifiproject.utils.SPUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.lang.reflect.Type

class NetworkTestActivity : BaseActivity<ActivityNetworkTestBinding>() {
    private lateinit var wifiManager: WifiManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (Constant.pingInt1 > 1000) {
            binding.routerSpeed.text = ">1000ms"

        } else {
            binding.routerSpeed.text = "${Constant.pingInt1}ms"

        }
        if (Constant.pingInt2 > 1000) {
            binding.totalGameSpeed.text = ">1000ms"

        } else {
            binding.totalGameSpeed.text = "${Constant.pingInt2}ms"
        }
        if (Constant.pingInt3 > 1000) {
            binding.cnSpeed.text = ">1000ms"
        } else {
            binding.cnSpeed.text = "${Constant.pingInt3}ms"

        }
        initView()
        binding.arrowBack.setOnClickListener {
            finish()
        }
        EventBus.getDefault().register(this)
        loadNativeAd()
    }

    private fun initView() {
        if (((Constant.pingInt1 + Constant.pingInt2 + Constant.pingInt3) / 3) < 50) {
            binding.gameType.text = "The game can be played excellent"
        } else if (((Constant.pingInt1 + Constant.pingInt2 + Constant.pingInt3) / 3) < 100) {
            binding.gameType.text = "The game can be played flunet"
        } else {
            binding.gameType.text = "The game can be played normally"
        }
        if (Constant.report != null) {
            if (Constant.report!!.transferRateBit.toLong() < 2 * 1024 * 1024) {
                binding.videoTitle.text = "Accordng to your speed,We recommend \n 360P videos"
                binding.tv360.setTextColor(resources.getColor(R.color.color34CC32))
                binding.tv720.setTextColor(resources.getColor(R.color.colorB7C3FF))
                binding.tv1080.setTextColor(resources.getColor(R.color.colorB7C3FF))
                binding.tv4k.setTextColor(resources.getColor(R.color.colorB7C3FF))
                binding.iv360.setBackgroundResource(R.drawable.network_green)
                binding.iv720.setBackgroundResource(R.drawable.network_gray)
                binding.iv1080.setBackgroundResource(R.drawable.network_gray)
                binding.iv4k.setBackgroundResource(R.drawable.network_gray)
                binding.line1.setBackgroundResource(R.color.colorB7C3FF)
                binding.line2.setBackgroundResource(R.color.colorB7C3FF)
                binding.line3.setBackgroundResource(R.color.colorB7C3FF)
            } else if (Constant.report!!.transferRateBit.toLong() < 5 * 1024 * 1024) {
                binding.videoTitle.text = "Accordng to your speed,We recommend \n 720P videos"
                binding.tv360.setTextColor(resources.getColor(R.color.color34CC32))
                binding.tv720.setTextColor(resources.getColor(R.color.color34CC32))
                binding.tv1080.setTextColor(resources.getColor(R.color.colorB7C3FF))
                binding.tv4k.setTextColor(resources.getColor(R.color.colorB7C3FF))
                binding.iv360.setBackgroundResource(R.drawable.network_green)
                binding.iv720.setBackgroundResource(R.drawable.network_green)
                binding.iv1080.setBackgroundResource(R.drawable.network_gray)
                binding.iv4k.setBackgroundResource(R.drawable.network_gray)
                binding.line1.setBackgroundResource(R.color.color34CC32)
                binding.line2.setBackgroundResource(R.color.colorB7C3FF)
                binding.line3.setBackgroundResource(R.color.colorB7C3FF)

            } else if (Constant.report!!.transferRateBit.toLong() < 10 * 1024 * 1024) {
                binding.videoTitle.text = "Accordng to your speed,We recommend \n 1080P videos"
                binding.tv360.setTextColor(resources.getColor(R.color.color34CC32))
                binding.tv720.setTextColor(resources.getColor(R.color.color34CC32))
                binding.tv1080.setTextColor(resources.getColor(R.color.color34CC32))
                binding.tv4k.setTextColor(resources.getColor(R.color.colorB7C3FF))
                binding.iv360.setBackgroundResource(R.drawable.network_green)
                binding.iv720.setBackgroundResource(R.drawable.network_green)
                binding.iv1080.setBackgroundResource(R.drawable.network_green)
                binding.iv4k.setBackgroundResource(R.drawable.network_gray)
                binding.line1.setBackgroundResource(R.color.color34CC32)
                binding.line2.setBackgroundResource(R.color.color34CC32)
                binding.line3.setBackgroundResource(R.color.colorB7C3FF)

            } else {
                binding.videoTitle.text = "Accordng to your speed,We recommend \n 4k videos"
                binding.tv360.setTextColor(resources.getColor(R.color.color34CC32))
                binding.tv720.setTextColor(resources.getColor(R.color.color34CC32))
                binding.tv1080.setTextColor(resources.getColor(R.color.color34CC32))
                binding.tv4k.setTextColor(resources.getColor(R.color.color34CC32))
                binding.iv360.setBackgroundResource(R.drawable.network_green)
                binding.iv720.setBackgroundResource(R.drawable.network_green)
                binding.iv1080.setBackgroundResource(R.drawable.network_green)
                binding.iv4k.setBackgroundResource(R.drawable.network_green)
                binding.line1.setBackgroundResource(R.color.color34CC32)
                binding.line2.setBackgroundResource(R.color.color34CC32)
                binding.line3.setBackgroundResource(R.color.color34CC32)

            }
        } else {
            binding.videoTitle.text = "Accordng to your speed,We recommend \n 360P videos"
            binding.tv360.setTextColor(resources.getColor(R.color.color34CC32))
            binding.tv720.setTextColor(resources.getColor(R.color.colorB7C3FF))
            binding.tv1080.setTextColor(resources.getColor(R.color.colorB7C3FF))
            binding.tv4k.setTextColor(resources.getColor(R.color.colorB7C3FF))
            binding.iv360.setBackgroundResource(R.drawable.network_green)
            binding.iv720.setBackgroundResource(R.drawable.network_gray)
            binding.iv1080.setBackgroundResource(R.drawable.network_gray)
            binding.iv4k.setBackgroundResource(R.drawable.network_gray)
            binding.line1.setBackgroundResource(R.color.colorB7C3FF)
            binding.line2.setBackgroundResource(R.color.colorB7C3FF)
            binding.line3.setBackgroundResource(R.color.colorB7C3FF)
        }
        val historyEntityLisJson = SPUtils.get().getString("history", "")
        var historyEntityList = arrayListOf<HistoryEntity>()
        if (historyEntityLisJson != null && historyEntityLisJson.isNotEmpty()) {
            val type: Type = object : TypeToken<List<HistoryEntity?>?>() {}.type
            historyEntityList = Gson().fromJson(historyEntityLisJson.toString(), type)
        }
        val wifiInfo = wifiManager.connectionInfo
        if (wifiInfo != null) {
            val history =
                wifiInfo.ssid?.let {
                    HistoryEntity(
                        it,
                        DateUtil().getTime(),
                        Constant.pingInt1,
                        Constant.pingInt2,
                        Constant.pingInt3
                    )
                }
            if (history != null) {
                historyEntityList.add(history)
            }
            SPUtils.get().putString("history", Gson().toJson(historyEntityList))
        }
    }

    private var adManage = AdManage()
    private fun loadNativeAd() {
        val adBean = Constant.AdMap[Constant.adNative_wifi_s]
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
            Constant.adNative_wifi_s,
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
            Constant.adNative_wifi_s,
            ad,
            binding.adFl,
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