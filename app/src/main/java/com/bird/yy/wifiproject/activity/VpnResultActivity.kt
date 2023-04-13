package com.bird.yy.wifiproject.activity

import android.os.Bundle
import android.os.SystemClock
import com.bird.yy.wifiproject.R
import com.bird.yy.wifiproject.base.BaseActivity
import com.bird.yy.wifiproject.databinding.ActivityVpnResultBinding
import com.bird.yy.wifiproject.utils.Constant
import com.bird.yy.wifiproject.utils.SPUtils
import java.text.SimpleDateFormat
import java.util.*

class VpnResultActivity : BaseActivity<ActivityVpnResultBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val base = intent.getLongExtra("base", 0)
        val isStop = intent.getBooleanExtra("isStop", false)
        val text = intent.getStringExtra("text").toString()
        binding.connectionTime.base = base
        if (isStop) {
            binding.vpnStatusLogo.setBackgroundResource(R.mipmap.vpn_result_disconnect)
            binding.vpnResultTitle.text = "Disconnect"
            binding.connectionTime.text = text
            SPUtils.get().putString(Constant.connectedCountryBean, "")
        } else {
            binding.connectionTime.start()
            binding.vpnStatusLogo.setBackgroundResource(R.mipmap.vpn_result_connected)
            binding.vpnResultTitle.text = "Connected"
        }
        binding.connectionTime.setOnChronometerTickListener {
            val time = SystemClock.elapsedRealtime() - it.base
            val date = Date(time)
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            binding.connectionTime.text = sdf.format(date)
        }
        binding.arrowBack.setOnClickListener {
            finish()
        }
    }
}