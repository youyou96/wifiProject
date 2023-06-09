package com.bird.yy.wifiproject.entity

data class WIFIEntity(
    val wifiSSID: String,
    val wifiBSSID: String,
    val needPassword: Boolean,
    val capabilities: String,
    val wifiStrength: Int,
    var password: String? = ""
) {

    override fun toString(): String {
        return "WIFIEntity(wifiSSID='$wifiSSID', wifiBSSID='$wifiBSSID', needPassword=$needPassword, capabilities='$capabilities', wifiStrength=$wifiStrength)"
    }
}
