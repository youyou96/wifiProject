package com.bird.yy.wifiproject.activity

import android.content.Context
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import com.bird.yy.wifiproject.R
import com.bird.yy.wifiproject.base.BaseActivity
import com.bird.yy.wifiproject.databinding.ActivityNetworkTestBinding
import com.bird.yy.wifiproject.utils.Constant

class NetworkTestActivity : BaseActivity<ActivityNetworkTestBinding>() {
    private lateinit var wifiManager: WifiManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        binding.routerSpeed.text = "${Constant.pingInt1}ms"
        binding.totalGameSpeed.text = "${Constant.pingInt2}ms"
        binding.cnSpeed.text = "${Constant.pingInt3}ms"
        initView()
        binding.arrowBack.setOnClickListener {
            finish()
        }
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
            if (Constant.report!!.transferRateBit.toLong() < 250 * 1024) {
                binding.videoTitle.text = "Accordng to your speed,We recommend \n 360P videos"
                binding.tv360.setTextColor(resources.getColor(R.color.color34CC32))
                binding.tv720.setTextColor(resources.getColor(R.color.colorB7C3FF))
                binding.tv1080.setTextColor(resources.getColor(R.color.colorB7C3FF))
                binding.tv4k.setTextColor(resources.getColor(R.color.colorB7C3FF))
                binding.iv360.setBackgroundResource(R.drawable.network_green)
                binding.iv720.setBackgroundResource(R.drawable.network_gray)
                binding.iv360.setBackgroundResource(R.drawable.network_gray)
                binding.iv360.setBackgroundResource(R.drawable.network_gray)
                binding.line1.setBackgroundResource(R.color.colorB7C3FF)
                binding.line2.setBackgroundResource(R.color.colorB7C3FF)
                binding.line3.setBackgroundResource(R.color.colorB7C3FF)
            } else if (Constant.report!!.transferRateBit.toLong() < 500 * 1024) {
                binding.videoTitle.text = "Accordng to your speed,We recommend \n 720P videos"
                binding.tv360.setTextColor(resources.getColor(R.color.color34CC32))
                binding.tv720.setTextColor(resources.getColor(R.color.color34CC32))
                binding.tv1080.setTextColor(resources.getColor(R.color.colorB7C3FF))
                binding.tv4k.setTextColor(resources.getColor(R.color.colorB7C3FF))
                binding.iv360.setBackgroundResource(R.drawable.network_green)
                binding.iv720.setBackgroundResource(R.drawable.network_green)
                binding.iv360.setBackgroundResource(R.drawable.network_gray)
                binding.iv360.setBackgroundResource(R.drawable.network_gray)
                binding.line1.setBackgroundResource(R.color.color34CC32)
                binding.line2.setBackgroundResource(R.color.colorB7C3FF)
                binding.line3.setBackgroundResource(R.color.colorB7C3FF)

            } else if (Constant.report!!.transferRateBit.toLong() < 750 * 1024) {
                binding.videoTitle.text = "Accordng to your speed,We recommend \n 1080P videos"
                binding.tv360.setTextColor(resources.getColor(R.color.color34CC32))
                binding.tv720.setTextColor(resources.getColor(R.color.color34CC32))
                binding.tv1080.setTextColor(resources.getColor(R.color.color34CC32))
                binding.tv4k.setTextColor(resources.getColor(R.color.colorB7C3FF))
                binding.iv360.setBackgroundResource(R.drawable.network_green)
                binding.iv720.setBackgroundResource(R.drawable.network_green)
                binding.iv360.setBackgroundResource(R.drawable.network_green)
                binding.iv360.setBackgroundResource(R.drawable.network_gray)
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
                binding.iv360.setBackgroundResource(R.drawable.network_green)
                binding.iv360.setBackgroundResource(R.drawable.network_green)
                binding.line1.setBackgroundResource(R.color.color34CC32)
                binding.line2.setBackgroundResource(R.color.color34CC32)
                binding.line3.setBackgroundResource(R.color.color34CC32)

            }
        }
    }
}