package com.bird.yy.wifiproject.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.RemoteException
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceDataStore
import com.bird.yy.wifiproject.R
import com.bird.yy.wifiproject.base.BaseActivity
import com.bird.yy.wifiproject.databinding.ActivityVpnHomeBinding
import com.bird.yy.wifiproject.entity.AdBean
import com.bird.yy.wifiproject.entity.Country
import com.bird.yy.wifiproject.entity.CountryBean
import com.bird.yy.wifiproject.entity.SmartBean
import com.bird.yy.wifiproject.manager.AdManage
import com.bird.yy.wifiproject.utils.*
import com.github.shadowsocks.Core
import com.github.shadowsocks.aidl.IShadowsocksService
import com.github.shadowsocks.aidl.ShadowsocksConnection
import com.github.shadowsocks.bg.BaseService
import com.github.shadowsocks.database.ProfileManager
import com.github.shadowsocks.preference.OnPreferenceDataStoreChangeListener
import com.github.shadowsocks.utils.Key
import com.github.shadowsocks.utils.StartService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

class VpnHomeActivity : BaseActivity<ActivityVpnHomeBinding>(), ShadowsocksConnection.Callback,
    OnPreferenceDataStoreChangeListener {
    private var state = BaseService.State.Idle
    private var adManage = AdManage()
    private fun toggle() = if (state.canStop) showConnect() else connect.launch(null)
    private val connect = registerForActivityResult(StartService()) {
        if (it) Toast.makeText(this, "Missing permissions", Toast.LENGTH_SHORT)
            .show() else showConnect()
    }
    private val connection = ShadowsocksConnection(true)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        connection.connect(this, this)
        EventBus.getDefault().register(this)
        initListener()
        loadNativeAd()
    }

    @SuppressLint("SetTextI18n")
    private fun initView() {
        if (Constant.isShowLead) {
            val customizedDialog = CustomizedDialog(this, "images/data.json", false, true)
            Constant.isShowLead = false
            if (!customizedDialog.isShowing) {
                binding.vpnHomeSrc.visibility = View.INVISIBLE
                customizedDialog.show()
            }
            customizedDialog.setOnClick {
                customizedDialog.dismiss()
                binding.vpnHomeSrc.visibility = View.VISIBLE
                if (!state.canStop) {
                    if (!ButtonUtils.isFastDoubleClick(R.id.animation_view)) {
                        connect()
                    }
                }

            }
            customizedDialog.setOnCancelListener {
                binding.vpnHomeSrc.visibility = View.VISIBLE
            }
        }
        val countryString = SPUtils.get().getString(Constant.chooseCountry, "")
        if (countryString != null && countryString.isNotEmpty()) {
            val country = Gson().fromJson(countryString, Country::class.java)
            if (country != null) {
                country.src?.let { it1 -> binding.vpnHomeCountrySrc.setBackgroundResource(it1) }
                binding.vpnHomeCountryTv.text = country.name + "-" + country.city
            }
        }
    }

    private fun initListener() {
        binding.arrowBack.setOnClickListener { finish() }
        binding.vpnHomeCity.setOnClickListener {
            jumpActivity(ServersActivity::class.java)
        }
        binding.vpnHomeStatusIv.setOnClickListener {
            connect()
        }
    }

    private fun connect() {
        if (InterNetUtil().isShowIR()) {
            showDialogByActivity(
                "Due to the policy reason , this service is not available in your country",
                "confirm", false
            ) { dialog, which -> finish() }

        } else {
            isHasNet()
        }
    }

    private fun isHasNet() {
        if (InterNetUtil().isNetConnection(this)) {
            toggle()
        } else {
            showDialogByActivity("Please check your network", "OK", true, null)
        }
    }

    override fun stateChanged(state: BaseService.State, profileName: String?, msg: String?) {
        changeConnectionStatus(state)
    }

    override fun onServiceConnected(service: IShadowsocksService) {
        changeConnectionStatus(
            try {
                BaseService.State.values()[service.state]
            } catch (_: RemoteException) {
                BaseService.State.Idle
            }
        )
    }

    override fun onPreferenceDataStoreChanged(store: PreferenceDataStore, key: String) {
        when (key) {
            Key.serviceMode -> {
                connection.disconnect(this)
                connection.connect(this, this)
            }
        }
    }

    private fun changeConnectionStatus(status: BaseService.State) {
        this.state = status

        when (status) {
            BaseService.State.Idle -> {
                SPUtils.get().putBoolean(Constant.isConnectStatus, false)
                binding.vpnHomePb.setProgress(0)
                binding.vpnHomeStatusIv.setBackgroundResource(R.mipmap.vpn_home_disconnect_logo)
                binding.theConnectionTimeTv.stop()
                binding.theConnectionTimeTv.setTextColor(getColor(R.color.white))
                binding.theConnectionTimeTv.text = "00:00:00"
                binding.vpnHomeStatusTv.text = "Connect by clicking the button"
                Toast.makeText(this, "please try again", Toast.LENGTH_LONG).show()
            }
            BaseService.State.Connected -> {
                binding.vpnHomePb.setProgress(0)
                SPUtils.get().putBoolean(Constant.isConnectStatus, true)
                if (countryBean != null) {
                    SPUtils.get()
                        .putString(Constant.connectedCountryBean, Gson().toJson(countryBean))
                    SPUtils.get().putString(
                        Constant.chooseCountry,
                        Gson().toJson(EntityUtils().countryBeanToCountry(countryBean!!))
                    )
                }
                binding.vpnHomeStatusIv.setBackgroundResource(R.mipmap.vpn_home_connected_logo)
                binding.vpnHomeStatusTv.text = "You are surfing the Internet safely"
                binding.theConnectionTimeTv.setTextColor(getColor(R.color.main_connected_time))
                binding.theConnectionTimeTv.setOnChronometerTickListener {
                    val time = SystemClock.elapsedRealtime() - it.base
                    val date = Date(time)
                    val sdf = SimpleDateFormat("HH:mm:ss", Locale.US)
                    sdf.timeZone = TimeZone.getTimeZone("UTC")
                    binding.theConnectionTimeTv.text = sdf.format(date)
                }
                val connectTime = SPUtils.get().getLong(Constant.connectTime, 0)
                if (connectTime > 0) {
                    binding.theConnectionTimeTv.base = connectTime
                } else {
                    binding.theConnectionTimeTv.base = SystemClock.elapsedRealtime()
                }
                if (SystemClock.elapsedRealtime() - (binding.theConnectionTimeTv.base) < 20 && SPUtils.get()
                        .getBoolean(Constant.isShowResultKey, false)
                ) {
                    lifecycleScope.launch(Dispatchers.Main.immediate) {
                        delay(300L)
                        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                            var country: Country? = null
                            if (countryBean != null) {
                                country = EntityUtils().countryBeanToCountry(countryBean!!)
                            }
                            val srcInt = if (country != null) country!!.src else R.mipmap.fast
                            val intent = Intent(this@VpnHomeActivity, VpnResultActivity::class.java)
                            intent.putExtra("base", binding.theConnectionTimeTv.base)
                            intent.putExtra("srcInt", srcInt)
                            startActivity(intent)
                            SPUtils.get().putBoolean(Constant.isShowResultKey, false)
                            refreshUi()
                        }
                    }
                }
                binding.theConnectionTimeTv.start()
            }
            BaseService.State.Stopped -> {
                binding.vpnHomePb.setProgress(0)
                binding.vpnHomeStatusIv.setBackgroundResource(R.mipmap.vpn_home_disconnect_logo)
                binding.vpnHomeStatusTv.text = "Connect by clicking the button"
                binding.theConnectionTimeTv.stop()
                binding.theConnectionTimeTv.text = "00:00:00"
                binding.theConnectionTimeTv.setTextColor(getColor(R.color.white))
                SPUtils.get().putLong(Constant.connectTime, 0L)
                if (SPUtils.get()
                        .getBoolean(Constant.isShowResultKey, false) && Constant.text != "00:00:00"
                ) {
                    SPUtils.get().putBoolean(Constant.isConnectStatus, false)
                    var country: Country? = null
                    if (countryBean != null) {
                        country = EntityUtils().countryBeanToCountry(countryBean!!)
                    }
                    val srcInt = if (country != null) country!!.src else R.mipmap.fast
                    lifecycleScope.launch(Dispatchers.Main.immediate) {
                        delay(300L)
                        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                            val intent = Intent(this@VpnHomeActivity, VpnResultActivity::class.java)
                            intent.putExtra("text", Constant.text)
                            intent.putExtra("isStop", true)
                            intent.putExtra("srcInt", srcInt)
                            startActivity(intent)
                            SPUtils.get().putBoolean(Constant.isShowResultKey, false)
                            refreshUi()
                            val countryBeanJson =
                                SPUtils.get().getString(Constant.connectingCountryBean, "")
                            if (countryBeanJson != null && countryBeanJson.isNotEmpty()) {
                                val countryBeanConnecting =
                                    Gson().fromJson(countryBeanJson, CountryBean::class.java)
                                if (countryBeanConnecting != null) {
                                    SPUtils.get().putString(
                                        Constant.chooseCountry,
                                        Gson().toJson(
                                            EntityUtils().countryBeanToCountry(
                                                countryBeanConnecting
                                            )
                                        )
                                    )
                                }
                            }
                        }
                    }

                }
            }
            else -> {
                binding.vpnHomeStatusTv.text = "Connect by clicking the button"
                SPUtils.get().putBoolean(Constant.isConnectStatus, false)
                binding.vpnHomePb.setProgress(0)
                binding.vpnHomeStatusIv.setBackgroundResource(R.mipmap.vpn_home_disconnect_logo)
                binding.theConnectionTimeTv.base = SystemClock.elapsedRealtime()
                binding.theConnectionTimeTv.stop()
                binding.theConnectionTimeTv.text = "00:00:00"
                binding.theConnectionTimeTv.setTextColor(getColor(R.color.white))
                SPUtils.get().putLong(Constant.connectTime, 0L)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun refreshUi() {
        var countryBeanJson = ""
        if (state.canStop) {
            if (SPUtils.get()
                    .getString(Constant.connectedCountryBean, "")?.isNotEmpty() == true
            ) {
                countryBeanJson = SPUtils.get()
                    .getString(Constant.connectedCountryBean, "").toString()
                Log.e("mainServiceChoose", "connected")
            }
        }
        if (countryBeanJson?.isEmpty() == true) {
            countryBeanJson = SPUtils.get()
                .getString(Constant.connectingCountryBean, "").toString()
            Log.e("mainServiceChoose", "connecting")

        }

        if (countryBeanJson != null) {
            val countryBean = Gson().fromJson(countryBeanJson, CountryBean::class.java)
            if (countryBean != null) {
                val country = EntityUtils().countryBeanToCountry(countryBean)
                country.src?.let { it1 -> binding.vpnHomeCountrySrc.setBackgroundResource(it1) }
                binding.vpnHomeCountryTv.text = country?.name + "-" + country?.city
                Log.e("mainServiceChoose", country?.name + "-" + country?.city)
            }
        }

    }

    private var connectionJob: Job? = null
    private var time: Int = 0
    private var interAdIsShow = false
    override fun onBinderDied() {
        connection.disconnect(this)
        connection.connect(this, this)
    }

    private var countryBean: CountryBean? = null
    private fun connectAnnotation() {
        ProfileManager.clear()
//        var countryBean: CountryBean? = null
        val countryBeanJson = if (state.canStop) SPUtils.get()
            .getString(Constant.connectedCountryBean, "") else SPUtils.get()
            .getString(Constant.connectingCountryBean, "")
        if (countryBeanJson != null) {
            if (countryBeanJson.isNotEmpty()) {
                countryBean = Gson().fromJson(countryBeanJson, CountryBean::class.java)
            }
        }
        if (countryBean == null || countryBean?.country?.contains("Super Fast") == true) {
            runBlocking {
                val smartList = getFastSmart()
                if (smartList.isNotEmpty()) {
                    val fast = if (smartList.size >= 3) {
                        Random().nextInt(3)
                    } else {
                        Random().nextInt(smartList.size)
                    }
                    countryBean = smartList[fast].smart
                    countryBean?.country = "Super Fast Server"
                    val country = EntityUtils().countryBeanToCountry(countryBean!!)
                    country.src = R.mipmap.fast
                    val profile = EntityUtils().countryToProfile(country)
                    val profileNew = ProfileManager.createProfile(profile)
                    Core.switchProfile(profileNew.id)
                    SPUtils.get()
                        .putString(Constant.connectingCountryBean, Gson().toJson(countryBean))
                }
            }
        } else {
            val country = countryBean?.let { EntityUtils().countryBeanToCountry(it) }
            val profile = country?.let { EntityUtils().countryToProfile(it) }
            val profileNew = profile?.let { ProfileManager.createProfile(it) }
            profileNew?.id?.let { Core.switchProfile(it) }
            SPUtils.get().putString(Constant.connectingCountryBean, Gson().toJson(countryBean))
        }
    }

    private suspend fun getFastSmart(): MutableList<SmartBean> {
        val smartJson = SPUtils.get().getString(Constant.smart, "")
        val serviceJson = SPUtils.get().getString(Constant.service, "")
        var serviceList: MutableList<CountryBean> = mutableListOf()
        val smartBeanList: MutableList<SmartBean> = mutableListOf()
        if (serviceJson?.isNotEmpty() == true) {
            val serviceType: Type = object : TypeToken<List<CountryBean?>?>() {}.type
            serviceList = Gson().fromJson(serviceJson, serviceType)
        }
        if (smartJson?.isNotEmpty() == true) {
            val type: Type = object : TypeToken<List<String?>?>() {}.type
            val smartList: MutableList<String> = Gson().fromJson(smartJson, type)
            if (smartList.isNotEmpty() && serviceList.isNotEmpty()) {
                for (item in smartList) {
                    for (service in serviceList) {
                        if (item == service.city) {
                            smartBeanList.add(
                                SmartBean(
                                    service,
                                    InterNetUtil().delayTest(service.ip, 1)
                                )
                            )
                        }
                    }

                }
            }
        }
        return smartBeanList
    }

    private fun showConnect() {
        if (state.canStop) {
            binding.vpnHomeStatusTv.text = "Disconnecting…"
        } else {
            binding.vpnHomeStatusTv.text = "Connecting…"
        }

        time = 0
        if (state.canStop) {
            Constant.text = binding.theConnectionTimeTv.text as String
        }
        SPUtils.get().putBoolean(Constant.isShowResultKey, true)
        val isCancel = state.canStop
        connectionJob = lifecycleScope.launch {
            flow {
                (0 until 10).forEach {
                    delay(1000)
                    time += 1
                    emit(it)
                }
            }.onStart {
                //start
                connectAnnotation()
            }.onCompletion {
                //finish
                if (state.canStop) {
                    Core.stopService()
                } else {
                    Core.startService()
                }
            }.collect {
                //process
                if (time == 1) {
                    loadInterAd()
                }
                binding.vpnHomePb.setProgress(it * 10)
                if (interAdIsShow) {
                    connectionJob?.cancel()
                    return@collect
                }
            }
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: Country?) {
        connect()
    }

    override fun onDestroy() {
        super.onDestroy()
        SPUtils.get().putLong(Constant.connectTime, binding.theConnectionTimeTv.base)
        SPUtils.get().putBoolean(Constant.isShowResultKey, false)
        EventBus.getDefault().unregister(this)
        countryBean = null
    }

    private fun loadNativeAd() {
        val adBean = Constant.AdMap[Constant.adNative_vpn_h]
        var time: Long = 0
        if (adBean != null) {
            time = System.currentTimeMillis() - adBean.saveTime
        }

        if (adBean?.ad == null || time > 50 * 60 * 1000) {
            Log.d("xxxx", "load")
            adManage.loadAd(
                Constant.adNative_vpn_h,
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
        } else {
            Log.d("xxxx", "show")
            showNativeAd(adBean)
        }
    }

    fun showNativeAd(ad: AdBean) {
        adManage.showAd(
            this@VpnHomeActivity,
            Constant.adNative_vpn_h,
            ad,
            binding.adFrameLayout,
            object : AdManage.OnShowAdCompleteListener {
                override fun onShowAdComplete() {
                }

                override fun isMax() {
                }

            })
    }

    private fun loadInterAd() {
        interAdIsShow = false
        val adBean = Constant.AdMap[Constant.adInterstitial_h]
        var time: Long = 0
        if (adBean != null) {
            time = System.currentTimeMillis() - adBean.saveTime
        }
        if (adBean?.ad == null || time > 50 * 60 * 1000) {
            adManage.loadAd(
                Constant.adInterstitial_h,
                this,
                object : AdManage.OnLoadAdCompleteListener {
                    override fun onLoadAdComplete(ad: AdBean?) {
                        if (ad?.ad != null) {
                            interAdIsShow = true
                            adManage.showAd(
                                this@VpnHomeActivity,
                                Constant.adInterstitial_h,
                                ad,
                                null,
                                object : AdManage.OnShowAdCompleteListener {
                                    override fun onShowAdComplete() {
                                        AdManage().loadAd(
                                            Constant.adInterstitial_h,
                                            this@VpnHomeActivity
                                        )
                                        if (state.canStop) {
                                            Core.stopService()
                                        } else {
                                            Core.startService()
                                        }
                                    }

                                    override fun isMax() {
                                        if (state.canStop) {
                                            Core.stopService()
                                        } else {
                                            Core.startService()
                                        }
                                    }

                                })
                        }
                    }

                    override fun isMax() {
                        if (state.canStop) {
                            Core.stopService()
                        } else {
                            Core.startService()
                        }
                    }

                })
        } else {
            interAdIsShow = true
            adManage.showAd(
                this@VpnHomeActivity,
                Constant.adInterstitial_h,
                adBean,
                null,
                object : AdManage.OnShowAdCompleteListener {
                    override fun onShowAdComplete() {
                        AdManage().loadAd(Constant.adInterstitial_h, this@VpnHomeActivity)
                        if (state.canStop) {
                            Core.stopService()
                        } else {
                            Core.startService()
                        }
                    }

                    override fun isMax() {
                        if (state.canStop) {
                            Core.stopService()
                        } else {
                            Core.startService()
                        }
                    }

                })
        }

        val adBeanNativeR = Constant.AdMap[Constant.adNative_r]
        if (adBeanNativeR?.ad == null) {
            AdManage().loadAd(Constant.adNative_r, this)
        }
    }
}