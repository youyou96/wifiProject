package com.bird.yy.wifiproject.entity

import com.wifianalyzer.secure.fast.R


data class Country(
    var id: Int = 0,
    var name: String? = "Super Fast Servers",
    var host: String = "example.shadowsocks.org",
    var remotePort: Int = 8388,
    var password: String = "u1rRWTssNv0p",
    val account: String = "chacha20-ietf-poly1305",
    var city:String = "",
    var src: Int? = R.mipmap.fast,
    var method: String = "aes-256-cfb",
    var isChoose: Boolean? = false,
    var route: String = "all",
    var remoteDns: String = "dns.google"
)