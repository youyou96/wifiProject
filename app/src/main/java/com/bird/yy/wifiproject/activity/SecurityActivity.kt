package com.bird.yy.wifiproject.activity

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import com.bird.yy.wifiproject.R
import com.bird.yy.wifiproject.base.BaseActivity
import com.bird.yy.wifiproject.databinding.ActivitySecurityBinding
import com.bird.yy.wifiproject.entity.AdBean
import com.bird.yy.wifiproject.entity.MessageEvent
import com.bird.yy.wifiproject.manager.AdManage
import com.bird.yy.wifiproject.utils.Constant
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SecurityActivity : BaseActivity<ActivitySecurityBinding>() {
    private var wifiInfo: WifiInfo? = null
    private lateinit var wifiManager: WifiManager

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiInfo = wifiManager.connectionInfo
        initData()
        initListener()
        EventBus.getDefault().register(this)
        loadNativeAd()
    }

    @SuppressLint("HardwareIds")
    @RequiresApi(Build.VERSION_CODES.R)
    private fun initData() {
        Log.d("xxxxxx", Constant.report?.transferRateBit.toString())
        if (Constant.report != null && Constant.report?.transferRateBit?.toLong()!! > 10 * 1024 * 1024) {
            binding.statusSrc.setBackgroundResource(R.mipmap.home_connected)
            binding.homeConnectStatus.setBackgroundResource(R.drawable.connected)
        } else {
            binding.statusSrc.setBackgroundResource(R.mipmap.home_no_connect)
            binding.homeConnectStatus.setBackgroundResource(R.drawable.disconnected)
        }
        binding.wifiName.text = wifiInfo?.ssid
        binding.wifiSpeed.text = wifiInfo?.linkSpeed.toString() + "Mb"
        binding.wifiIp.text = wifiInfo?.ipAddress?.toLong()?.let { longToIp(it) }
        binding.wifiMac.text = wifiInfo?.macAddress
    }

    private fun initListener() {
        binding.arrowBack.setOnClickListener {
            finish()
        }
    }

    private fun longToIp(ip: Long): String {
        return ((ip and 0xFF).toString() + "."
                + (ip shr 8 and 0xFF) + "."
                + (ip shr 16 and 0xFF) + "."
                + (ip shr 24 and 0xFF).toString())
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
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
}