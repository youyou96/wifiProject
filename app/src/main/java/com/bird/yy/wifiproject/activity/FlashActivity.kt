package com.bird.yy.wifiproject.activity

import android.os.Bundle
import android.os.CountDownTimer
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bird.yy.wifiproject.MainActivity
import com.bird.yy.wifiproject.R
import com.bird.yy.wifiproject.base.BaseActivity
import com.bird.yy.wifiproject.base.BaseApplication
import com.bird.yy.wifiproject.databinding.ActivityFlashBinding
import com.bird.yy.wifiproject.entity.AdBean
import com.bird.yy.wifiproject.entity.MessageEvent
import com.bird.yy.wifiproject.manager.AdManage
import com.bird.yy.wifiproject.utils.Constant
import com.bird.yy.wifiproject.utils.EntityUtils
import com.bird.yy.wifiproject.utils.InterNetUtil
import com.bird.yy.wifiproject.utils.SPUtils
import com.bird.yy.wifiproject.viewModel.FlashViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import org.greenrobot.eventbus.EventBus
import timber.log.Timber


private const val COUNTER_TIME = 10L

class FlashActivity : BaseActivity<ActivityFlashBinding>() {
    private var timer: CountDownTimer? = null
    private lateinit var flashViewModel: FlashViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        flashViewModel = ViewModelProvider(this)[FlashViewModel::class.java]
        countDownTimer()
        flashViewModel.progress.observe(this) {
            binding.viewModel = flashViewModel
        }
        setData()
        InterNetUtil().getIpByServer(this)
        if (Constant.coldStart) {
            getRemoteConfig()
        }
        if (!Constant.coldStart) {
            loadAd()
            Timber.tag("RemoteConfig").d(" flash00 put adResourceBean")
        }
    }


    private fun countDownTimer() {
        timer = object : CountDownTimer(COUNTER_TIME * 1000, 1000L) {
            override fun onTick(p0: Long) {
                val process = 100 - (p0 * 100 / COUNTER_TIME / 1000)
                flashViewModel.progress.postValue(process.toInt())
                if (!Constant.coldStart && process >= 20) {
                    showAd()
                }
                if (Constant.coldStart && process >= 50) {
                    showAd()
                }

            }

            override fun onFinish() {
                jumpActivityFinish(MainActivity::class.java)
            }

        }
        (timer as CountDownTimer).start()
    }

    override fun onRestart() {
        super.onRestart()
        if (timer != null) {
            timer?.cancel()
            countDownTimer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }


    private fun setData() {
        if (SPUtils.get().getString(Constant.smart, "")?.isEmpty() == true) {
            val smartJson = EntityUtils().obtainNativeJsonData(this, "city.json")
            SPUtils.get().putString(Constant.smart, smartJson.toString())
        }
        if (SPUtils.get().getString(Constant.service, "")?.isEmpty() == true) {
            val serviceJson = EntityUtils().obtainNativeJsonData(this, "service.json")
            SPUtils.get().putString(Constant.service, serviceJson.toString())
        }
    }

    private fun showAd() {
        Timber.tag("RemoteConfig").d(" flash11 put adResourceBean")

        val adBean = Constant.AdMap[Constant.adOpen_wifi]
        val adManage = AdManage()

        if (adBean?.ad != null) {
            timer?.cancel()
            adManage.showAd(
                this@FlashActivity,
                Constant.adOpen_wifi,
                adBean,
                null,
                object : AdManage.OnShowAdCompleteListener {
                    override fun onShowAdComplete() {
                        jumpActivityFinish(MainActivity::class.java)
                    }

                    override fun isMax() {
                        jumpActivityFinish(MainActivity::class.java)
                    }

                })
        } else {
            adManage.loadAd(Constant.adOpen_wifi, this, object : AdManage.OnLoadAdCompleteListener {
                override fun onLoadAdComplete(ad: AdBean?) {
                }

                override fun isMax() {
                    jumpActivityFinish(MainActivity::class.java)
                }

            })
        }
    }

    private fun loadAd() {
        var adBean = Constant.AdMap[Constant.adOpen_wifi]
        val adManage = AdManage()
        if (adBean?.ad == null) {
            adManage.loadAd(Constant.adOpen_wifi, this, object : AdManage.OnLoadAdCompleteListener {
                override fun onLoadAdComplete(ad: AdBean?) {
                }

                override fun isMax() {
                }

            })
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
    }

    private var remoteConfigJob: Job? = null
    private fun getRemoteConfig() {
        remoteConfigJob = GlobalScope.launch(Dispatchers.Main) {
            flow {
                (0 until 8).forEach {
                    delay(500)
                    emit(it)
                }
            }.onStart {
                Timber.tag("RemoteConfig").d("start get remote config")
                //start
            }.onCompletion {
                //finish
                var adResourceBeanJson = SPUtils.get().getString(Constant.adResourceBean, "")
                if (adResourceBeanJson == null || adResourceBeanJson.isEmpty()) {
                    Timber.tag("RemoteConfig").d(" get remote config fail")
                    adResourceBeanJson =
                        EntityUtils().obtainNativeJsonData(this@FlashActivity, "ad.json")
                            .toString()
                    SPUtils.get().putString(Constant.adResourceBean, adResourceBeanJson)
                    Timber.tag("RemoteConfig").d(" flash put adResourceBean")
                    loadingData()
                }


            }.collect {
                //process
                var adResourceBeanJson = SPUtils.get().getString(Constant.adResourceBean, "")
                if (adResourceBeanJson != null && adResourceBeanJson!!.isNotEmpty()) {
                    remoteConfigJob?.cancel()
                    Timber.tag("RemoteConfig")
                        .d(" get remote config successful ${adResourceBeanJson.toString()}")
                }else{
                    Timber.tag("RemoteConfig")
                        .d(" get remote config fail num}")
                }
            }
        }

    }
    private fun loadingData() {
        //load ad
        Timber.tag("RemoteConfig").d(" application put adResourceBean")
        val adBeanNativeWifiH = Constant.AdMap[Constant.adNative_wifi_h]
        if (adBeanNativeWifiH != null) {
            val time = System.currentTimeMillis() - adBeanNativeWifiH.saveTime
            if (time > Constant.timeOut || adBeanNativeWifiH.ad == null) {
                AdManage().loadAd(
                    Constant.adNative_wifi_h,
                    this,
                    object : AdManage.OnLoadAdCompleteListener {
                        override fun onLoadAdComplete(ad: AdBean?) {
                            if(ad?.ad != null){
                                EventBus.getDefault().post(MessageEvent(Constant.adNative_wifi_h,ad))
                            }

                        }

                        override fun isMax() {
                        }

                    })
            }
        } else {
            AdManage().loadAd(Constant.adNative_wifi_h, this,
                object : AdManage.OnLoadAdCompleteListener {
                    override fun onLoadAdComplete(ad: AdBean?) {
                        if(ad?.ad != null){
                            EventBus.getDefault().post(MessageEvent(Constant.adNative_wifi_h,ad))
                        }
                    }

                    override fun isMax() {
                    }

                })
        }


        val adBeanNativeWifiPwd = Constant.AdMap[Constant.adNative_wifi_p]
        if (adBeanNativeWifiPwd != null) {
            val time = System.currentTimeMillis() - adBeanNativeWifiPwd.saveTime
            if (time > Constant.timeOut || adBeanNativeWifiPwd.ad == null) {
                AdManage().loadAd(Constant.adNative_wifi_p, this,
                    object : AdManage.OnLoadAdCompleteListener {
                        override fun onLoadAdComplete(ad: AdBean?) {
                            if(ad?.ad != null){
                                EventBus.getDefault().post(MessageEvent(Constant.adNative_wifi_p,ad))
                            }
                        }

                        override fun isMax() {
                        }

                    })
            }
        } else {
            AdManage().loadAd(Constant.adNative_wifi_p, this,
                object : AdManage.OnLoadAdCompleteListener {
                    override fun onLoadAdComplete(ad: AdBean?) {
                        if(ad?.ad != null){
                            EventBus.getDefault().post(MessageEvent(Constant.adNative_wifi_p,ad))
                        }
                    }

                    override fun isMax() {
                    }

                })
        }

        val adBeanNativeWifiHistory = Constant.AdMap[Constant.adNative_wifi_history]
        if (adBeanNativeWifiHistory != null) {
            val time = System.currentTimeMillis() - adBeanNativeWifiHistory.saveTime
            if (time > Constant.timeOut || adBeanNativeWifiHistory.ad == null) {
                AdManage().loadAd(Constant.adNative_wifi_history, this,
                    object : AdManage.OnLoadAdCompleteListener {
                        override fun onLoadAdComplete(ad: AdBean?) {
                            if(ad?.ad != null){
                                EventBus.getDefault().post(MessageEvent(Constant.adNative_wifi_history,ad))
                            }
                        }

                        override fun isMax() {
                        }

                    })
            }
        } else {
            AdManage().loadAd(Constant.adNative_wifi_history, this,
                object : AdManage.OnLoadAdCompleteListener {
                    override fun onLoadAdComplete(ad: AdBean?) {
                        if(ad?.ad != null){
                            EventBus.getDefault().post(MessageEvent(Constant.adNative_wifi_history,ad))
                        }
                    }

                    override fun isMax() {
                    }

                })
        }
        val adBeanNativeWifiS = Constant.AdMap[Constant.adNative_wifi_s]
        if (adBeanNativeWifiS != null) {
            val time = System.currentTimeMillis() - adBeanNativeWifiS.saveTime
            if (time > Constant.timeOut || adBeanNativeWifiS.ad == null) {
                AdManage().loadAd(Constant.adNative_wifi_s, this,
                    object : AdManage.OnLoadAdCompleteListener {
                        override fun onLoadAdComplete(ad: AdBean?) {
                            if(ad?.ad != null){
                                EventBus.getDefault().post(MessageEvent(Constant.adNative_wifi_s,ad))
                            }
                        }

                        override fun isMax() {
                        }

                    })
            }
        } else {
            AdManage().loadAd(Constant.adNative_wifi_s, this,
                object : AdManage.OnLoadAdCompleteListener {
                    override fun onLoadAdComplete(ad: AdBean?) {
                        if(ad?.ad != null){
                            EventBus.getDefault().post(MessageEvent(Constant.adNative_wifi_s,ad))
                        }
                    }

                    override fun isMax() {
                    }

                })
        }

        val adBeanInter = Constant.AdMap[Constant.adInterstitial_wifi]
        if (adBeanInter != null) {
            val time = System.currentTimeMillis() - adBeanInter.saveTime
            if (time > Constant.timeOut || adBeanInter.ad == null) {
                AdManage().loadAd(Constant.adInterstitial_wifi, this)
            }
        } else {
            AdManage().loadAd(Constant.adInterstitial_wifi, this)
        }

    }
}


