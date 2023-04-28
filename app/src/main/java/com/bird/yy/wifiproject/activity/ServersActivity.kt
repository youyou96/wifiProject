package com.bird.yy.wifiproject.activity

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.bird.yy.wifiproject.adapter.ServersAdapter
import com.bird.yy.wifiproject.base.BaseActivity
import com.bird.yy.wifiproject.entity.Country
import com.bird.yy.wifiproject.entity.CountryBean
import com.bird.yy.wifiproject.utils.Constant
import com.bird.yy.wifiproject.utils.EntityUtils
import com.bird.yy.wifiproject.utils.SPUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wifianalyzer.secure.fast.databinding.ActivityVpnServiceBinding
import org.greenrobot.eventbus.EventBus
import java.lang.reflect.Type

class ServersActivity : BaseActivity<ActivityVpnServiceBinding>() {
    private val serversAdapter = ServersAdapter()
    private var isConnection = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.serversRv.adapter = serversAdapter
        isConnection = intent.getBooleanExtra("isConnection", false)
        setList()
        serversAdapter.itemClickListener = object :ServersAdapter.ItemClickListener{
            override fun onItemClick(country: Country) {
                if (country.isChoose == true) {
                    finish()
                } else {
                    if (isConnection) {
                        val alertDialog = AlertDialog.Builder(this@ServersActivity)
                            .setMessage("If you want to connect to another VPN, you need to disconnect the current connection first. Do you want to disconnect the current connection?")
                            .setPositiveButton("yes") { p0, p1 ->
                                EventBus.getDefault().post(country)
                                saveConnectingCountryBean(country)
                                finish()
                            }
                            .setNegativeButton("no", null)
                            .create()
                        alertDialog.show()
                    } else {
                        EventBus.getDefault().post(country)
                        saveConnectingCountryBean(country)
                        finish()
                    }
                }
            }

        }
        binding.arrowBack.setOnClickListener {
            finish()
        }
    }
    private fun setList() {
        val countryJson: String? = SPUtils.get().getString(Constant.service, "")
        val countryList: MutableList<Country> = ArrayList()
        countryList.add(Country(0, "Super Fast Server"))
        if (countryJson != null) {
            if (countryJson.isNotEmpty()) {
                val type: Type = object : TypeToken<List<CountryBean?>?>() {}.type
                val countryBean: MutableList<CountryBean> =
                    Gson().fromJson(countryJson.toString(), type)
                if (countryBean.isNotEmpty()) {
                    countryBean.forEach {
                        val country: Country = EntityUtils().countryBeanToCountry(it)
                        countryList.add(country)
                    }
                }
            }
        }
        val countryString = SPUtils.get().getString(Constant.chooseCountry, "")
        if (countryString != null && countryString.isNotEmpty()) {
            val country = Gson().fromJson(countryString, Country::class.java)
            if (country != null) {
                val profileName = country.name
                for (item in countryList) {
                    if (profileName?.contains(item.name!!) == true) {
                        item.isChoose = true
                    }
                }
            }
        }
        serversAdapter.setNewData(countryList)
    }
    private fun saveConnectingCountryBean(event: Country) {
        val countryJson: String? = SPUtils.get().getString(Constant.service, "")
        if (countryJson != null) {
            if (countryJson.isNotEmpty()) {
                val type: Type = object : TypeToken<List<CountryBean?>?>() {}.type
                val countryBean: MutableList<CountryBean> =
                    Gson().fromJson(countryJson.toString(), type)
                if (countryBean.isNotEmpty()) {
                    if (event?.name?.contains("Super Fast") == true) {
                        val countryData = CountryBean()
                        countryData.country = event?.name!!
                        SPUtils.get()
                            .putString(Constant.connectingCountryBean, Gson().toJson(countryData))
                    } else {
                        countryBean.forEach {
                            if (event?.name?.equals(it.country) == true) {
                                SPUtils.get()
                                    .putString(Constant.connectingCountryBean, Gson().toJson(it))
                            }
                        }
                    }
                }
            }
        }
    }

}