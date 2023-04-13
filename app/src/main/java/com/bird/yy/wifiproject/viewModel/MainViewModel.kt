package com.bird.yy.wifiproject.viewModel

import android.net.wifi.WifiInfo
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    var wifiInfo: MutableLiveData<WifiInfo> = MutableLiveData()
}