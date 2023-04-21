package com.bird.yy.wifiproject.utils

import com.bird.yy.wifiproject.entity.AdBean
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
        const val adResourceBean ="adResourceBean"
        const val adOpen = "serpac_o_open"
        const val adInterstitial_r = "serpac_i_2R"
        const val adInterstitial_h = "serpac_i_2H"
        const val adNative_wifi_h = "serpac_wifi_home"
        const val adNative_wifi_p = "serpac_wifi_pwd"
        const val adNative_wifi_s = "serpac_wifi_security"
        const val adNative_wifi_history = "serpac_wifi_history"
        const val adNative_wifi_n = "serpac_wifi_network"
        const val adNative_vpn_h = "serpac_vpn_network"
        const val adNative_r = "serpac_n_result"
        const val openAdType ="open"
        const val nativeAdType = "native"
        const val interAdType = "inter"
        const val adTimeBean = "adTimeBean"
        var securityOrSpeed = ""
        var report: SpeedTestReport? = null
        var pingInt1 = 0
        var pingInt2 = 0
        var pingInt3 = 0
        var text = "00:00:00"
        var isShowLead  = true
        var AdMapStatus: MutableMap<String, Boolean> = mutableMapOf()
        var AdMap: MutableMap<String, AdBean> = mutableMapOf()
    }
}