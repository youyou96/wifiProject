package com.bird.yy.wifiproject.base

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Process
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDexApplication
import com.bird.yy.wifiproject.MainActivity
import com.bird.yy.wifiproject.activity.FlashActivity
import com.bird.yy.wifiproject.activity.VpnHomeActivity
import com.bird.yy.wifiproject.manager.ActivityManager
import com.bird.yy.wifiproject.utils.SPUtils
import com.github.shadowsocks.Core
import com.lzy.okgo.OkGo
import com.lzy.okgo.cache.CacheEntity
import com.lzy.okgo.cache.CacheMode
import com.lzy.okgo.cookie.CookieJarImpl
import com.lzy.okgo.cookie.store.DBCookieStore
import com.lzy.okgo.https.HttpsUtils


import kotlinx.coroutines.*
import okhttp3.OkHttpClient

import java.util.concurrent.TimeUnit

class BaseApplication : MultiDexApplication(), Application.ActivityLifecycleCallbacks,
    LifecycleObserver {
    private var foregroundActivities = 0
    private var isChangingConfiguration = false
    private var job: Job? = null
    private var bgFlag = false

    companion object {
        fun getActivityManager(): ActivityManager {
            return ActivityManager.get()
        }
    }

    override fun onCreate() {
        super.onCreate()
        Core.init(this, VpnHomeActivity::class)
        if (applicationContext.packageName.equals(getCurrentProcessName())) {
            initOkGo()
            SPUtils.get().init(this)
            registerActivityLifecycleCallbacks(this)

            // Log the Mobile Ads SDK version.
            ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Core.updateNotificationChannels()
    }

    private fun getCurrentProcessName(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return Application.getProcessName()
        }
        val pid = Process.myPid()
        var processName = ""
        val manager =
            applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        for (process in manager.runningAppProcesses) {
            if (process.pid == pid) {
                processName = process.processName
            }
        }
        return processName
    }

    private fun initOkGo() {
        //---------这里给出的是示例代码,告诉你可以这么传,实际使用的时候,根据需要传,不需要就不传-------------//
        //----------------------------------------------------------------------------------------//
        val builder: OkHttpClient.Builder = OkHttpClient.Builder()

        //超时时间设置，默认60秒
        builder.readTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);      //全局的读取超时时间
        builder.writeTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);
        builder.connectTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS) //全局的连接超时时间


        builder.cookieJar(CookieJarImpl(DBCookieStore(this))) //使用数据库保持cookie，如果cookie不过期，则一直有效

        builder.retryOnConnectionFailure(false)
        //https相关设置，以下几种方案根据需要自己设置
        //方法一：信任所有证书,不安全有风险
        val sslParams1 = HttpsUtils.getSslSocketFactory()
        //方法二：自定义信任规则，校验服务端证书
        //方法三：使用预埋证书，校验服务端证书（自签名证书）
        //HttpsUtils.SSLParams sslParams3 = HttpsUtils.getSslSocketFactory(getAssets().open("srca.cer"));
        //方法四：使用bks证书和密码管理客户端证书（双向认证），使用预埋证书，校验服务端证书（自签名证书）
        //HttpsUtils.SSLParams sslParams4 = HttpsUtils.getSslSocketFactory(getAssets().open("xxx.bks"), "123456", getAssets().open("yyy.cer"));
        builder.sslSocketFactory(sslParams1.sSLSocketFactory, sslParams1.trustManager)
        // 其他统一的配置
        OkGo.getInstance().init(this) //必须调用初始化
            .setOkHttpClient(builder.build()) //建议设置OkHttpClient，不设置会使用默认的
            .setCacheMode(CacheMode.NO_CACHE) //全局统一缓存模式，默认不使用缓存，可以不传
            .setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE).retryCount = 3 //全局统一超时重连次数，默认为三次，那么最差的情况会请求4次(一次原始请求，三次重连请求)，不需要可以设置为0
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        getActivityManager().addActivity(activity)
    }

    override fun onActivityStarted(activity: Activity) {
        foregroundActivities++
        if (foregroundActivities == 1 && !isChangingConfiguration) {
            job?.cancel()
            job = null
            if (bgFlag  ) {
                bgFlag = false
                activity.startActivity(Intent(activity, FlashActivity::class.java))
                if (activity is MainActivity){
                    activity.finish()
                }
            }
        }

        isChangingConfiguration = false
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
        foregroundActivities--
        if (foregroundActivities == 0) {
            job = GlobalScope.launch(Dispatchers.Main.immediate) {
                delay(3000L)
                bgFlag = true
            }
        }
        isChangingConfiguration = activity.isChangingConfigurations
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
        getActivityManager().removeActivity(activity)
    }
}