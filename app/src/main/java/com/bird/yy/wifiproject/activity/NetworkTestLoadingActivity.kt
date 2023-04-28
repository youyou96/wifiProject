package com.bird.yy.wifiproject.activity

import android.content.Context
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import androidx.lifecycle.lifecycleScope
import com.bird.yy.wifiproject.R
import com.bird.yy.wifiproject.base.BaseActivity
import com.bird.yy.wifiproject.databinding.ActivitySecurityLoadingBinding
import com.bird.yy.wifiproject.entity.AdBean
import com.bird.yy.wifiproject.entity.HistoryEntity
import com.bird.yy.wifiproject.manager.AdManage
import com.bird.yy.wifiproject.utils.Constant
import com.bird.yy.wifiproject.utils.DateUtil
import com.bird.yy.wifiproject.utils.InterNetUtil
import com.bird.yy.wifiproject.utils.SPUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import fr.bmartel.speedtest.SpeedTestReport
import fr.bmartel.speedtest.SpeedTestSocket
import fr.bmartel.speedtest.inter.ISpeedTestListener
import fr.bmartel.speedtest.model.SpeedTestError
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import java.lang.reflect.Type

class NetworkTestLoadingActivity : BaseActivity<ActivitySecurityLoadingBinding>() {
    private var isSpeed = false
    private var wifiInfo: WifiInfo? = null
    private lateinit var wifiManager: WifiManager
    private var isShowInter = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiInfo = wifiManager.connectionInfo
        isSpeed = "security" != Constant.securityOrSpeed
        initView()
        initListener()
        showTime()
    }


    private fun initView() {
        if (isSpeed) {
            binding.animationView.setBackgroundResource(R.mipmap.speed_backgroud)
            binding.animationViewSrc.setImageResource(R.mipmap.speed_src)
        } else {
            binding.animationView.setBackgroundResource(R.mipmap.security_background_icon)
            binding.animationViewSrc.setImageResource(R.mipmap.security_src_icon)
        }
        val rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate)
        binding.animationView.startAnimation(rotateAnimation)
    }

    private fun getPing() {
        runBlocking {
            val ping1 = InterNetUtil().delayTest(Constant.ping1, 1)
            Constant.pingInt1 = ping1
            val ping2 = InterNetUtil().delayTest(Constant.ping2, 1)
            Constant.pingInt2 = ping2
            val ping3 = InterNetUtil().delayTest(Constant.ping3, 1)
            Constant.pingInt3 = ping3
        }
    }

    private fun initListener() {
        binding.arrowBack.setOnClickListener {
            connectionJob?.cancel()
            finish()
        }
    }

    private fun getNetSpeed() {
        lifecycleScope.launch(Dispatchers.IO) {
            val speedTestSocket = SpeedTestSocket()
            speedTestSocket.addSpeedTestListener(object : ISpeedTestListener {
                override fun onCompletion(report: SpeedTestReport?) {
                    Constant.report = report
                }

                override fun onProgress(percent: Float, report: SpeedTestReport?) {
                    if (num >= 5) {
                        Constant.report = report
                    }

                }

                override fun onError(speedTestError: SpeedTestError?, errorMessage: String?) {

                }

            })

            speedTestSocket.startDownload("http://ipv4.appliwave.testdebit.info/5M/5M.zip")
        }
    }

    private var connectionJob: Job? = null
    private var num: Int = 1
    private fun showTime() {
        connectionJob = lifecycleScope.launch(Dispatchers.IO) {
            flow {
                (0 until 10).forEach {
                    delay(1000)
                    num += 1
                    emit(it)
                }
            }.onStart {
                //start
                Constant.report = null
                Constant.pingInt1 = 0
                Constant.pingInt2 = 0
                Constant.pingInt3 = 0
                getNetSpeed()
                getPing()

            }.onCompletion {
                //finish
                Log.d("AdManage", "xxxxxxx")
                if (!isShowInter) {
                    jumpActivity()
                }
            }.collect {
                //process
                if (num >= 3) {
                    if (Constant.report != null) {
                        if (Constant.pingInt1 != 0 && Constant.pingInt2 != 0 && Constant.pingInt3 != 0 && Constant.report != null) {
                            lifecycleScope.launch(Dispatchers.Main) {
                                loadInterAd()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        connectionJob?.cancel()
    }

    private fun loadInterAd() {
        val adBeanInter = Constant.AdMap[Constant.adInterstitial_wifi]
        if (adBeanInter != null) {
            val time = System.currentTimeMillis() - adBeanInter.saveTime
            if (time > Constant.timeOut || adBeanInter.ad == null) {
                AdManage().loadAd(
                    Constant.adInterstitial_wifi,
                    this,
                    object : AdManage.OnLoadAdCompleteListener {
                        override fun onLoadAdComplete(ad: AdBean?) {
                            if (ad?.ad != null) {
                                connectionJob?.cancel()
                                showInterAd(ad)
                            }
                        }

                        override fun isMax() {
                            connectionJob?.cancel()
                            isShowInter = true
                            Log.d("AdManage", "xxxxxxx111111")
                            jumpActivity()
                        }

                    })
            } else {
                connectionJob?.cancel()
                showInterAd(adBeanInter)
            }
        } else {
            AdManage().loadAd(
                Constant.adInterstitial_wifi,
                this,
                object : AdManage.OnLoadAdCompleteListener {
                    override fun onLoadAdComplete(ad: AdBean?) {
                        if (ad?.ad != null) {
                            connectionJob?.cancel()
                            showInterAd(ad)
                        }
                    }

                    override fun isMax() {
                        isShowInter = true
                        connectionJob?.cancel()
                        Log.d("AdManage", "xxxxxxx222222")
                        jumpActivity()
                    }

                })
        }
    }

    private fun showInterAd(adBean: AdBean) {
        isShowInter = true
        AdManage().showAd(
            this,
            Constant.adInterstitial_wifi,
            adBean,
            null,
            object : AdManage.OnShowAdCompleteListener {
                override fun onShowAdComplete() {
                    Log.d("AdManage", "xxxxxxx333333")
                    jumpActivity()
                }

                override fun isMax() {
                    Log.d("AdManage", "xxxxxxx555555")
                    jumpActivity()
                }

            })
    }

    private fun jumpActivity() {
        lifecycleScope.launch(Dispatchers.Main) {
            if (isSpeed) {
                jumpActivityFinish(NetworkTestActivity::class.java)

            } else {
                jumpActivityFinish(SecurityActivity::class.java)
            }
        }
    }
}