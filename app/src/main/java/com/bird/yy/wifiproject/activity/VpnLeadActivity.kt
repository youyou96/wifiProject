package com.bird.yy.wifiproject.activity

import android.os.Bundle
import com.bird.yy.wifiproject.base.BaseActivity
import com.bird.yy.wifiproject.databinding.ActivityVpnLeadBinding

class VpnLeadActivity :BaseActivity<ActivityVpnLeadBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.leadCancel.setOnClickListener {
            finish()
        }
        binding.leadButton.setOnClickListener {
            jumpActivityFinish(VpnHomeActivity::class.java)
        }
    }
}