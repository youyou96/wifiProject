package com.bird.yy.wifiproject.activity

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bird.yy.wifiproject.MainActivity
import com.bird.yy.wifiproject.R
import com.bird.yy.wifiproject.base.BaseActivity
import com.bird.yy.wifiproject.databinding.ActivityFlashBinding
import com.bird.yy.wifiproject.entity.AdBean
import com.bird.yy.wifiproject.manager.AdManage
import com.bird.yy.wifiproject.utils.Constant
import com.bird.yy.wifiproject.utils.EntityUtils
import com.bird.yy.wifiproject.utils.InterNetUtil
import com.bird.yy.wifiproject.utils.SPUtils
import com.bird.yy.wifiproject.viewModel.FlashViewModel


private const val COUNTER_TIME = 3L

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
        loadAd()
    }


    private fun countDownTimer() {
        timer = object : CountDownTimer(COUNTER_TIME * 1000, 1000L) {
            override fun onTick(p0: Long) {
                val process = 100 - (p0 * 100 / COUNTER_TIME / 1000)
                flashViewModel.progress.postValue(process.toInt())
                if (process >= 20) {
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
        val adBean = Constant.AdMap[Constant.adOpen]
        val adManage = AdManage()

        if (adBean?.ad != null) {
            timer?.cancel()
            adManage.showAd(
                this@FlashActivity,
                Constant.adOpen,
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
            adManage.loadAd(Constant.adOpen, this, object : AdManage.OnLoadAdCompleteListener {
                override fun onLoadAdComplete(ad: AdBean?) {
                }

                override fun isMax() {
                    jumpActivityFinish(MainActivity::class.java)
                }

            })
        }
    }

    private fun loadAd() {
        var adBean = Constant.AdMap[Constant.adOpen]
        val adManage = AdManage()
        if (adBean?.ad == null) {
            adManage.loadAd(Constant.adOpen, this, object : AdManage.OnLoadAdCompleteListener {
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
}


