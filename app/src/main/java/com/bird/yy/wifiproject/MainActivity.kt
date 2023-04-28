package com.bird.yy.wifiproject

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.location.LocationManager
import android.net.*
import android.net.wifi.*
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import com.bird.yy.wifiproject.activity.ActivityHistory
import com.bird.yy.wifiproject.activity.NetworkTestLoadingActivity
import com.bird.yy.wifiproject.activity.PrivacyPolicyWebView
import com.bird.yy.wifiproject.activity.VpnLeadActivity
import com.bird.yy.wifiproject.adapter.WIFIAdapter
import com.bird.yy.wifiproject.base.BaseActivity
import com.bird.yy.wifiproject.dialog.PwdDialog
import com.bird.yy.wifiproject.entity.WIFIEntity
import com.bird.yy.wifiproject.listener.OnWifiConnectListener
import com.bird.yy.wifiproject.listener.OnWifiEnabledListener
import com.bird.yy.wifiproject.listener.OnWifiScanResultsListener
import com.bird.yy.wifiproject.manager.WiFiManagerNew
import com.bird.yy.wifiproject.utils.Constant
import com.bird.yy.wifiproject.utils.SPUtils
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.wifianalyzer.secure.fast.R
import com.wifianalyzer.secure.fast.databinding.ActivityMainBinding


class MainActivity : BaseActivity<ActivityMainBinding>(), OnWifiScanResultsListener,
    OnWifiConnectListener, OnWifiEnabledListener {
    private var requestPermissionName: String = Manifest.permission.ACCESS_FINE_LOCATION
    private lateinit var wifiManager: WifiManager
    private val wifiAdapter = WIFIAdapter()
    private lateinit var wiFiManagerNew: WiFiManagerNew
    private val networkBroadcastReceiver = WiFiManagerNew.NetworkBroadcastReceiver()
    private var connectivityManager: ConnectivityManager? = null
    private var chooseWifiInfo: WIFIEntity? = null

    /* *******************************************************************************************/

    private val requestSinglePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted: Boolean ->
            if (granted) {
                // 申请定位权限通过，扫描WIFI
                if (wifiManager.isWifiEnabled) {
                    wifiManager.startScan()
                    val wifiInfo = wifiManager.connectionInfo
                    refreshConnectStatus(wifiInfo)
                }
            } else {
                //未同意授权
                if (!shouldShowRequestPermissionRationale(requestPermissionName)) {
                    //用户拒绝权限并且系统不再弹出请求权限的弹窗
                    //这时需要我们自己处理，比如自定义弹窗告知用户为何必须要申请这个权限
                    showDialogByActivity(
                        "No location permission, unable to search for nearby WiFi", "confirm", true
                    ) { _, _ -> finish() }
                }
            }
        }
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            connectivityManager?.run {
                if (boundNetworkForProcess != network) {
                    if (boundNetworkForProcess != network) {
                        val wifiInfo = wifiManager.connectionInfo
                        refreshConnectStatus(wifiInfo)
                        bindProcessToNetwork(network)
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        binding.contentLayout.wifiRv.adapter = wifiAdapter
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
//        // 注册广播
        registerReceiver(networkBroadcastReceiver, IntentFilter().apply {
            addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
            addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
            addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
            addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)
            addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)
        })
        wiFiManagerNew = WiFiManagerNew.getInstance(this)
        if (!wifiManager.isWifiEnabled) {
            wifiManager.isWifiEnabled = true
        }
//        initData()
        initListener()

    }

    override fun onResume() {
        super.onResume()
        wiFiManagerNew.setOnWifiEnabledListener(this)
        wiFiManagerNew.setOnWifiScanResultsListener(this)
        wiFiManagerNew.setOnWifiConnectListener(this)
    }

    override fun onPause() {
        super.onPause()
        wiFiManagerNew.removeOnWifiConnectListener()
        wiFiManagerNew.removeOnWifiEnabledListener()
        wiFiManagerNew.removeOnWifiScanResultsListener()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onStart() {
        super.onStart()
        initData()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("ServiceCast")
    private fun initData() {
        val location: LocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (location.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    requestPermissionName
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val wifiInfo = wifiManager.connectionInfo
                refreshConnectStatus(wifiInfo)
                wifiManager.startScan()
            } else {

                requestSinglePermissionLauncher.launch(requestPermissionName)


            }
        } else {
            showDialogByActivity(
                "Please go to the settings page to enable location permissions", "ok", true,
                { dialog, which ->
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }, "cancel"
            ) { dialog, which -> dialog?.dismiss() }
        }

    }

    private fun refreshConnectStatus(wifiInfo: WifiInfo?) {
        if (wifiInfo != null && wifiManager.isWifiEnabled) {
            binding.contentLayout.homeConnectStatus.setBackgroundResource(R.drawable.connected)
            binding.contentLayout.homeWifiName.text = wifiInfo?.ssid
            binding.contentLayout.homeWifiStatus.text = "connected wifi"
            binding.contentLayout.homeWifiIcon.setImageResource(R.mipmap.home_connected)
            binding.contentLayout.homeConnectedTv.visibility = View.VISIBLE
            binding.contentLayout.homeConnectedCl.visibility = View.VISIBLE
            binding.contentLayout.homeConnectedName.text = wifiInfo?.ssid

        } else {
            binding.contentLayout.homeConnectStatus.setBackgroundResource(R.drawable.disconnected)
            binding.contentLayout.homeWifiName.text = "Wifi Not Connected"
            binding.contentLayout.homeWifiStatus.text = "no connection wifi"
            binding.contentLayout.homeWifiIcon.setImageResource(R.mipmap.home_no_connect)
            binding.contentLayout.homeConnectedTv.visibility = View.GONE
            binding.contentLayout.homeConnectedCl.visibility = View.GONE

        }
    }

    @SuppressLint("RtlHardcoded")
    private fun initListener() {
        binding.contentLayout.homeSetting.setOnClickListener {
            if (binding.drawerLayout.isOpen) {
                binding.drawerLayout.close()
            } else {
                binding.drawerLayout.open()
            }
        }
        binding.contentLayout.homeRefreshTv.setOnClickListener {
            if (wifiManager != null) {
                wifiManager.startScan()
            }
        }
        wifiAdapter.itemClickListener = object : WIFIAdapter.ItemClickListener {
            @SuppressLint("ShowToast")
            override fun onItemClick(wifiInfo: WIFIEntity) {
                if (!wifiManager.isWifiEnabled) {
                    Toast.makeText(this@MainActivity, "WiFi not enabled", Toast.LENGTH_SHORT).show()
                    return
                }
                chooseWifiInfo = wifiInfo
                if (binding.drawerLayout.isOpen) {
                    return
                }
                if (wifiInfo.capabilities.contains(
                        "wpa",
                        true
                    ) || (wifiInfo.capabilities.contains("wep", true))
                ) {
                    //pwd dialog
//                    showInputWIFIPasswordDialog(wifiInfo)
                    val wifiEntityJson = SPUtils.get().getString(wifiInfo.wifiSSID, "")
                    if (wifiEntityJson != null) {
                        if (wifiEntityJson.isNotEmpty()) {
                            val wifiEntity = Gson().fromJson(wifiEntityJson, WIFIEntity::class.java)
                            if (wifiEntity != null && wifiInfo.wifiSSID == wifiEntity.wifiSSID) {
                                wifiInfo.password = wifiEntity.password
                            }
                        }
                    }

                    val pwdDialog =
                        PwdDialog(this@MainActivity, wifiInfo.wifiSSID, wifiInfo.password)
                    pwdDialog.setConnectWifi { pwd ->
                        wifiInfo.password = pwd
                        Constant.wifiEntity = wifiInfo
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                            if (wifiInfo.capabilities.contains(
                                    "wpa",
                                    true
                                )
                            ) {
                                WiFiManagerNew.getInstance(this@MainActivity)
                                    .connectWPA2Network(wifiInfo.wifiSSID, pwd)
                            } else {
                                WiFiManagerNew.getInstance(this@MainActivity)
                                    .connectWEPNetwork(wifiInfo.wifiSSID, pwd)
                            }

                        } else {
                            if (Settings.canDrawOverlays(this@MainActivity)) {
                                if (wifiInfo.capabilities.contains(
                                        "wpa",
                                        true
                                    )
                                ) {
                                    WiFiManagerNew.getInstance(this@MainActivity)
                                        .connectWPA2Network(wifiInfo.wifiSSID, pwd)
                                } else {
                                    WiFiManagerNew.getInstance(this@MainActivity)
                                        .connectWEPNetwork(wifiInfo.wifiSSID, pwd)
                                }
                            } else {
                                showDialogByActivity(
                                    "Due to system limitations, we need floating window permissions to work",
                                    "Grant",
                                    false
                                ) { dialog, which ->
                                    kotlin.runCatching {
                                        startActivity(
                                            Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION")
                                                .setData(Constant.packageName.toUri())
                                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        )
                                    }.onFailure {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Error occurred, please grant it manually in the settings",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                }
                            }
                        }

                    }
                    pwdDialog.show()
                } else {
                    WiFiManagerNew.getInstance(this@MainActivity)
                        .connectOpenNetwork(wifiInfo.wifiSSID)
                }


            }
        }
        binding.settingLayout.contactUs.setOnClickListener {
            openMail()
        }
        binding.settingLayout.privacyPolicy.setOnClickListener {
            jumpActivity(PrivacyPolicyWebView::class.java)
        }
        binding.settingLayout.updateTv.setOnClickListener {
            rateNow()
        }
        binding.settingLayout.shareTv.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.putExtra(Intent.EXTRA_TEXT, Constant.url)
            intent.type = "text/plain"
            startActivity(intent)
        }
        binding.contentLayout.homeSecurityCl.setOnClickListener {
            if (!wifiManager.isWifiEnabled) {
                Toast.makeText(this@MainActivity, "WiFi not enabled", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (binding.drawerLayout.isOpen) {
                return@setOnClickListener
            }
            jumpActivity(NetworkTestLoadingActivity::class.java)
            Constant.securityOrSpeed = "security"
        }
        binding.contentLayout.homeSpeedCl.setOnClickListener {
            if (!wifiManager.isWifiEnabled) {
                Toast.makeText(this@MainActivity, "WiFi not enabled", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (binding.drawerLayout.isOpen) {
                return@setOnClickListener
            }
            jumpActivity(NetworkTestLoadingActivity::class.java)
            Constant.securityOrSpeed = "speed"
        }
        binding.contentLayout.historyIv.setOnClickListener {
            if (binding.drawerLayout.isOpen) {
                return@setOnClickListener
            }
            jumpActivity(ActivityHistory::class.java)
        }
        binding.contentLayout.vpnCl.setOnClickListener {
            if (binding.drawerLayout.isOpen) {
                return@setOnClickListener
            }
            jumpActivity(VpnLeadActivity::class.java)
        }
//        binding.contentLayout.homeConnectedCl.setOnClickListener {
//            if (binding.drawerLayout.isOpen || !wifiManager.isWifiEnabled) {
//                return@setOnClickListener
//            }
//            WiFiManagerNew.getInstance(this).disconnectCurrentWifi()
//        }
    }


    private fun rateNow() {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            intent.setPackage("com.android.vending")
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Please install Google Store", Toast.LENGTH_LONG).show()
        }
    }

    private fun openMail() {
        val uri: Uri = Uri.parse("mailto:" + Constant.mail)
        val packageInfos: List<ResolveInfo> =
            packageManager!!.queryIntentActivities(Intent(Intent.ACTION_SENDTO, uri), 0)
        val tempPkgNameList: MutableList<String> = java.util.ArrayList()
        val emailIntents: MutableList<Intent> = java.util.ArrayList()
        for (info in packageInfos) {
            val pkgName = info.activityInfo.packageName
            if (!tempPkgNameList.contains(pkgName)) {
                tempPkgNameList.add(pkgName)
                val intent: Intent? = packageManager!!.getLaunchIntentForPackage(pkgName)
                if (intent != null) {
                    emailIntents.add(intent)
                }
            }
        }
        if (emailIntents.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_SENDTO, uri)
            startActivity(intent)
            val chooserIntent =
                Intent.createChooser(intent, "Please select mail application")
            if (chooserIntent != null) {
                startActivity(chooserIntent)
            } else {
                showDialogByActivity("Please set up a Mail account", "OK", true, null)
            }
        } else {
            showDialogByActivity("Please set up a Mail account", "OK", true, null)
        }
    }

    override fun onWiFiConnectLog(log: String?) {
        if (log == "COMPLETED") {
            refreshConnectStatus(wifiManager.connectionInfo)
        }
        if (chooseWifiInfo != null) {
            if (log != null) {
                if (chooseWifiInfo?.wifiSSID?.let { log.contains(it) } == true) {
                    Snackbar.make(
                        binding.contentLayout.wifiRv,
                        "$log connecting",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onWiFiConnectSuccess(SSID: String?) {
        Log.i("MainActivity.TAG", "onWiFiConnectLog: $SSID")
        if (chooseWifiInfo != null) {
            if (SSID != null) {
                val wifiInfo = wifiManager.connectionInfo
                val wifiEntity = Constant.wifiEntity
                if (wifiEntity != null) {
                    if (SSID == wifiInfo.ssid) {
                        SPUtils.get().putString(SSID.replace("\"", ""), Gson().toJson(wifiEntity))
                    }
                }
                refreshConnectStatus(wifiInfo)
                if (chooseWifiInfo?.wifiSSID?.let { SSID.contains(it) } == true) {
                    Toast.makeText(applicationContext, "$SSID  connected", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(applicationContext, "${chooseWifiInfo!!.wifiSSID}  connected fail", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

    }

    override fun onWiFiConnectFailure(SSID: String?) {
        if (chooseWifiInfo != null) {
            if (SSID != null) {
                if (chooseWifiInfo?.wifiSSID?.let { SSID.contains(it) } == true) {
                    refreshConnectStatus(null)
                    Toast.makeText(applicationContext, "$SSID  connected fail", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    override fun onWifiEnabled(enabled: Boolean) {
        if (!enabled) {
            Toast.makeText(this@MainActivity, "WiFi not enabled", Toast.LENGTH_SHORT).show()
            refreshConnectStatus(null)
        } else {
            refreshConnectStatus(wifiManager.connectionInfo)
        }
    }

    override fun onScanResults(scanResults: MutableList<ScanResult>?) {
        reFreshData(scanResults)
    }

    private fun reFreshData(scanResults: MutableList<ScanResult>?) {
        // 扫描完成
        val wifiData = ArrayList<WIFIEntity>()
        scanResults?.forEach {
            val ssid = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.wifiSsid.toString()
            } else {
                it.SSID
            }
            val bssid = it.BSSID
            // 获取WIFI加密类型
            val capabilities = it.capabilities
            // 获取WIFI信号强度
            val level = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                wifiManager.calculateSignalLevel(it.level) ?: 0
            } else {
                WifiManager.calculateSignalLevel(it.level, 4)
            }
            val wifiInfo = wifiManager.connectionInfo
            val connectedSSID = wifiInfo?.ssid?.replace("\"", "")
            if (ssid.isNotEmpty()) {
                var isExist = false
                wifiData.forEach { item ->
                    if (item.wifiSSID == ssid || ssid == connectedSSID) {
                        isExist = true
                    }
                }
                if (!isExist) {
                    wifiData.add(
                        WIFIEntity(
                            ssid,
                            bssid,
                            capabilities.contains("wpa", true) || capabilities.contains(
                                "wep",
                                true
                            ),
                            capabilities,
                            level
                        )
                    )
                }
            }

        }
        // 根据信号强度降序排列
        wifiData.sortByDescending { it.wifiStrength }
        wifiAdapter.setNewData(wifiData)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(networkBroadcastReceiver)
        connectivityManager?.run {
            bindProcessToNetwork(null)
            try {
                unregisterNetworkCallback(networkCallback)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


}