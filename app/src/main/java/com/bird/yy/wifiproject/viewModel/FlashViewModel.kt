package com.bird.yy.wifiproject.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FlashViewModel : ViewModel(){
    var progress: MutableLiveData<Int> = MutableLiveData()
}