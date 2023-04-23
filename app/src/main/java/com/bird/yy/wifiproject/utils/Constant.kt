package com.bird.yy.wifiproject.utils

import com.bird.yy.wifiproject.entity.WIFIEntity
import fr.bmartel.speedtest.SpeedTestReport

open class Constant {
    companion object {
        const val isConnectStatus = "isConnectStatus"
        const val packageName: String = "package:com.bird.yy.wifiproject"
        const val url: String = "https://play.google.com/store/apps/details?id="
        const val mail: String = "1192390712@qq.com"
        const val PrivacyPolicy = "https://www.baidu.com"
        const val ping1 = "202.108.22.5"
        const val ping2 = "14.119.104.189"
        const val ping3 = "14.119.104.189"
        const val chooseCountry = "chooseCountry"
        const val iR = "iR"
        const val isShowResultKey = "isShowResultKey"
        const val connectedCountryBean = "connectedCountryBean"
        const val connectingCountryBean = "connectingCountryBean"
        const val smart = "smart"
        const val service = "service"
        const val connectTime = "connectTime"
        var securityOrSpeed = ""
        var report: SpeedTestReport? = null
        var pingInt1 = 0
        var pingInt2 = 0
        var pingInt3 = 0
        var text = "00:00:00"
        var isShowLead = true
        var wifiEntity: WIFIEntity? = null
    }
}