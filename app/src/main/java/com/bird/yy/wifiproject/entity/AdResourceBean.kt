package com.bird.yy.wifiproject.entity

data class AdResourceBean(
    var serpac_sm: Int,
    var serpac_cm: Int,
    var serpac_o_open: MutableList<AdBean>,
    var serpac_n_home: MutableList<AdBean>,
    var serpac_n_result: MutableList<AdBean>,
    var serpac_n_connect: MutableList<AdBean>,
    var serpac_n_history: MutableList<AdBean>,
    var serpac_i_2H: MutableList<AdBean>,
)
