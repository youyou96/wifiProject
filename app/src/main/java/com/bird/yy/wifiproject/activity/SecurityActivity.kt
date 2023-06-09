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
import com.bird.yy.wifiproject.utils.Constant

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
    }

    @SuppressLint("HardwareIds")
    @RequiresApi(Build.VERSION_CODES.R)
    private fun initData() {
        Log.d("xxxxxx",Constant.report?.transferRateBit.toString())
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
}