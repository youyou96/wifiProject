package com.bird.yy.wifiproject.utils;

import static android.content.Context.WIFI_SERVICE;
import static android.net.wifi.WifiConfiguration.KeyMgmt.NONE;
import static android.net.wifi.WifiConfiguration.Protocol.WPA;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.widget.Toast;

import androidx.annotation.RequiresApi;


import com.bird.yy.wifiproject.base.BaseApplication;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ExecutorService;

public class WifiUtil {

    public static final int WIFI_AP_STATE_ENABLING = 12;
    public static final int WIFI_AP_STATE_ENABLED = 13;
    public static final int WIFI_AP_STATE_FAILED = 14;

    public static final int WHAT_UPDATE_DEVICES_NUMBER = 602;//刷新总数
    public static final int WHAT_UPDATE_DEVICES_PROGRESS = 603;//刷新进度
    private static final String TAG = "TAG";
    private Timer mTimerDown;
    private Timer mTimerUp;
    private WifiManager mWifiManager;
    // 定义WifiInfo对象
    public WifiInfo mWifiInfo;
    // 网络连接列表
    private List<WifiConfiguration> mWifiConfiguration;

    public WifiManager getWifiManager() {
        return mWifiManager;
    }

    @SuppressLint("MissingPermission")
    public WifiUtil() {
        mWifiManager = (WifiManager) new BaseApplication().getApplicationContext().getSystemService(WIFI_SERVICE);
        // 取得WifiInfo对象
        mWifiInfo = mWifiManager.getConnectionInfo();
//            mWifiConfiguration = mWifiManager.getConfiguredNetworks();
        openWifi();
    }


    /**
     * 开启一个热点（无密码）
     *
     * @param ssid 热点名称
     */
    public boolean stratWifiAp(String ssid) {
        boolean result;
        mWifiManager.setWifiEnabled(false);
        Method method1 = null;
        try {
            method1 = mWifiManager.getClass().getMethod("setWifiApEnabled",
                    WifiConfiguration.class, boolean.class);
            WifiConfiguration netConfig = new WifiConfiguration();
            //wifi热点名字
            netConfig.SSID = ssid;
            //密码
//            netConfig.preSharedKey = "";

            netConfig.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            netConfig.allowedProtocols.set(WPA);
            netConfig.allowedKeyManagement.set(NONE);//密码类型：NONE,WPA_PSK
            netConfig.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.CCMP);
            netConfig.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            netConfig.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.CCMP);
            netConfig.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.TKIP);


            method1.invoke(mWifiManager, netConfig, true);
            result = true;
            // Method method2 =
            // wifiManager.getClass().getMethod("getWifiApState");
            // int state = (Integer) method2.invoke(wifiManager);
            // LogUtil.i("wifi state" + state);


        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }

        return result;
    }


    /**
     * 关闭WiFi热点
     */
    public void closeWifiAP() {
        try {
            Method method = mWifiManager.getClass().getMethod("getWifiApConfiguration");
//            ReflectionUtils.makeAccessible(method);
            WifiConfiguration config = (WifiConfiguration) method.invoke(mWifiManager);
            Method method2 = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method2.invoke(mWifiManager, config, false);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }


    public int getWifiApState() {
        try {
            Method method = mWifiManager.getClass().getMethod("getWifiApState");
            int i = (Integer) method.invoke(mWifiManager);
            return i;
        } catch (Exception e) {
            return WIFI_AP_STATE_FAILED;
        }
    }

    public boolean isApEnabled() {
        int state = getWifiApState();
        return WIFI_AP_STATE_ENABLING == state || WIFI_AP_STATE_ENABLED == state;
    }


    public interface ScanConnectDevicesCallBack {
        void currentConnectDevices(String reachableIp);

        void refreshConnectDevicesCount();
    }

    /**
     * 判断是不是WIFI连接  手动关闭开启
     *
     * @return
     */
    public static boolean networkIsConnectedWifi() {
        ConnectivityManager cm = (ConnectivityManager) new BaseApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWiFiNetworkInfo = cm.getActiveNetworkInfo();
        if (mWiFiNetworkInfo != null) {
            if (mWiFiNetworkInfo.isAvailable()) {
                if (mWiFiNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {// WIFI
                    return true;
                } else if (mWiFiNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {// 移动数据
                    return false;
                }
            } else {
                return false;
            }
        }
        return false;
    }

    /**
     * 获取连接wifi的设备数（遍历ping）
     *
     * @param lock 控制子线程开启与销毁的开关。true：默认开启； false：线程关闭销毁
     *             （页面关闭时，应调用  {@code lock.setLock(false);},{@code if(threadPool !=null) threadPool.shutdownNow();} 关闭扫描子线程
     *             （此变量为了解决部分华为7.0以上手机线程数超标造成OOM异常）
     *             <p>
     *             获取当前WIFI下实时连接的设备数目原理说明
     *             一个路由设备路由器的IP地址通常都是192.168.1.1-192.168.1.255，其中92.168.1.1一般为路由器后台管理地址，那剩下的地址也就是路由器分配给其他设备的IP地址，只有255-1=254个。
     *             实际生活中，无线路由器最多能连接多少用户？对于连接用户主要取决路由器的性能与宽带大小，根据路由器厂商的说法，一般的家用路由器能够同时连接10-40个左右的无线设备，这不仅取决于路由器的处理能力，也与宽带大小有关。
     *             1.通过WiFi获取当前网关设备IP地址，（假定一个网关设备的IP地址为192.168.11.1;那所有通过WIFI接入到该网关的设备所能分配到的地址一定是192.168.11.X）
     *             2.InetAddress.isReachable()判断1-255个地址中可到达的地址即为当前已经接入到网关设备的的设备
     */
    public void scanCurrentConnectDevices(Lock lock, ExecutorService threadPool, ScanConnectDevicesCallBack scanConnectDevicesCallBack) {
        //开启15线程；每个线程处理17个地址的连接查看
        for (int i = 1; i <= 17; i++) {
            final int first = (i - 1) * 17 + 1;
            final int count = i * 17;
            ScanConnectDeviceThread thread = new ScanConnectDeviceThread(scanConnectDevicesCallBack, lock, first, count);
            threadPool.execute(thread);
        }

    }

    public void stopDownTimer() {
        if (mTimerDown != null) {
            mTimerDown.cancel();
            mTimerDown.purge();
            mTimerDown = null;
        }
    }

    public void stopUpTimer() {
        if (mTimerUp != null) {
            mTimerUp.cancel();
            mTimerUp.purge();
            mTimerUp = null;
        }
    }
    //-----------------------------------------


    public DhcpInfo getDhcpInfo() {
        return (mWifiManager == null) ? null : mWifiManager.getDhcpInfo();
    }

    public int getConnectedDhcpInfoGateway() {
        return getDhcpInfo().gateway;
    }

    public int getConnectedDhcpInfoIpAddress() {
        return getDhcpInfo().ipAddress;
    }

    public int getConnectedDhcpInfoNetMask() {
        return getDhcpInfo().netmask;
    }

    public int getConnectedDhcpInfoDns() {
        return getDhcpInfo().dns1;
    }

    /**
     * 获取本机ip
     *
     * @return
     */
    public String getLocalIpAddress() {
        try {
            WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
            // 获取32位整型IP地址
            int ipAddress = wifiInfo.getIpAddress();

            //返回整型地址转换成“*.*.*.*”地址
            return String.format("%d.%d.%d.%d",
                    (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                    (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
        } catch (Exception e) {
            e.printStackTrace();
            return "192.168.1.1";
        }
    }

    /* 判断网络是否可用
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.INTERNET"/>}</p>
     * <p>需要异步ping，如果ping不通就说明网络不可用</p>
     *
     * @param ip ip地址（自己服务器ip），如果为空，ip为阿里巴巴公共ip
     * @return {@code true}: 可用<br>{@code false}: 不可用
     */
    public static boolean isAvailableByPing() {
        String ip = "www.baidu.com";
        if (ip == null || ip.length() <= 0) {
            ip = "223.5.5.5";// 阿里巴巴公共ip
        }
        Runtime runtime = Runtime.getRuntime();
        Process ipProcess = null;
        try {
            //-c 后边跟随的是重复的次数，-w后边跟随的是超时的时间，单位是秒，不是毫秒，要不然也不会anr了
            ipProcess = runtime.exec("ping -c 3 -w 3 " + ip);
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            //在结束的时候应该对资源进行回收
            if (ipProcess != null) {
                ipProcess.destroy();
            }
            runtime.gc();
        }
        return false;
    }

    /**
     * 获取发送conn_box命令需要的ip(鸿冠盒子)
     *
     * @return
     */
    public String getConnBoxIpAddress() {
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        // 获取32位整型IP地址
        int ipAddress = wifiInfo.getIpAddress();
        int netmask = mWifiManager.getDhcpInfo().netmask;

        //根据ip与子网掩码计算出广播地址
        String bcAddress = longToIp(getBCAddress(intToIpWM(ipAddress), intToIpWM(netmask)));


        //返回整型地址转换成“*.*.*.*”地址
        return bcAddress;
    }

    /**
     * IP转换网关
     *
     * @param ip
     * @return
     */
    public static String getGatewayFromIp(String ip) {
        String gateway = "";
        try {
            String[] nums = ip.split("\\.");
            gateway = String.format("%s.%s.%s.%s", nums[0], nums[1], nums[2], "1");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return gateway;
    }

    /**
     * 信号强度（单位 dBm，802.11网络的）
     *
     * @return
     */
    public int getConnectedRssi() {
        mWifiInfo = mWifiManager.getConnectionInfo();
        int dbm = (mWifiInfo == null) ? 0 : mWifiInfo.getRssi();
        if (dbm > -20) {
            dbm = -20;
        }
        return dbm;
    }

    // 打开WIFI
    public void openWifi() {
        if (!mWifiManager.isWifiEnabled()) {
            closeWifiAP();
            mWifiManager.setWifiEnabled(true);
        } else {
//            Toast.makeText(mActivity,"亲，Wifi已经开启,不用再开了", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isWifiEnabled() {
        return mWifiManager.isWifiEnabled();
    }

    /**
     * 打开设置中的wifi界面
     *
     * @param activity
     */
    public void openSettingWifi(Activity activity) {
        if (Build.VERSION.SDK_INT > 10) {
            // 3.0以上打开设置界面，也可以直接用ACTION_WIRELESS_SETTINGS打开到wifi界面
            activity.startActivity(new Intent(Settings.ACTION_SETTINGS));
        } else {
            activity.startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
        }
    }


    // 关闭WIFI
    public void closeWifi() {
        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        }/*else if(mWifiManager.getWifiState() == 1){
            Toast.makeText(context,"亲，Wifi已经关闭，不用再关了", Toast.LENGTH_SHORT).show();
        }else if (mWifiManager.getWifiState() == 0) {
            Toast.makeText(context,"亲，Wifi正在关闭，不用再关了", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(context,"请重新关闭", Toast.LENGTH_SHORT).show();
        }*/
    }

    // 检查当前WIFI状态
    public void checkState(Context context) {
        if (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLING) {
            Toast.makeText(context, "Wifi正在关闭", Toast.LENGTH_SHORT).show();
        } else if (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED) {
            Toast.makeText(context, "Wifi已经关闭", Toast.LENGTH_SHORT).show();
        } else if (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
            Toast.makeText(context, "Wifi正在开启", Toast.LENGTH_SHORT).show();
        } else if (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
            Toast.makeText(context, "Wifi已经开启", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "没有获取到WiFi状态", Toast.LENGTH_SHORT).show();
        }
    }
    @SuppressLint("MissingPermission")
    public void startScan(ArrayList<ScanResult> wifiList) {

        mWifiManager.startScan();
        //得到扫描结果

         List<ScanResult> results = mWifiManager.getScanResults();
//        LogUtil.i("TAG", "startScan: "+results.toString());
        // 得到配置好的网络连接
//        mWifiConfiguration = mWifiManager.getConfiguredNetworks();

        if (results == null) {
            if (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
                Toast.makeText(new BaseApplication().getApplicationContext(), "当前区域没有无线网络", Toast.LENGTH_SHORT).show();
            } else if (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
                Toast.makeText(new BaseApplication().getApplicationContext(), "wifi正在开启，请稍后扫描", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(new BaseApplication().getApplicationContext(), "WiFi没有开启", Toast.LENGTH_SHORT).show();
            }
        } else {
            for (ScanResult result : results) {
                if (result.SSID == null || result.SSID.length() == 0 || result.capabilities.contains("[IBSS]")) {
                    continue;
                }
                boolean found = false;
                for (ScanResult item : wifiList) {
                    if (item.SSID.equals(result.SSID) && item.capabilities.equals(result.capabilities)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    wifiList.add(result);
                }
            }
        }
    }


    /**
     * 获取UTF-8编码的SSID (如果原数据编码gbk，也转成utf-8)
     *
     * @param result
     * @return
     */
    public static String getWifiSsidFromScanResult(ScanResult result) {
        String wifiSsidStr = "";
        Class<ScanResult> scanResultClass = ScanResult.class;
        try {
            Field wifiSsidField = scanResultClass.getField("wifiSsid");
            Object wifiSsid = wifiSsidField.get(result);
            //获取原数据
            byte[] ssidBytes = getSsidBytesFromWifiSsid(wifiSsid);
            //判断原数据是否是GBK编码
            if (StringTextUtils.isGBKString(ssidBytes)) {
                //gbk转utf-8
                wifiSsidStr = StringTextUtils.getUTF8StringFromGBKString(new String(ssidBytes, "GBK"));
            } else {//不是，默认UTF-8编码
                wifiSsidStr = wifiSsid.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (result.SSID.contains("_WiFi5")) {
            wifiSsidStr = wifiSsidStr + "_WIFI5";
        }
        if (result.SSID.contains("_WiFi6")) {
            wifiSsidStr = wifiSsidStr + "_WIFI6";
        }
        return wifiSsidStr;
    }

    /**
     * 从WifiInfo类中获取到隐藏方法 getWifiSsid
     *
     * @param wifiInfo 原数据
     * @return WifiSsid的实例
     */
    public static Object getWifiSsidFromWifiInfo(WifiInfo wifiInfo) {
        Object wifiSsid = null;
        Class<WifiInfo> wifiInfoClass = WifiInfo.class;
        try {
            Method method = wifiInfoClass.getMethod("getWifiSsid");
            wifiSsid = method.invoke(wifiInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return wifiSsid;
    }


    /**
     * 从WifiSsid类获取隐藏的ssid原始数据
     *
     * @param wifiSsid
     * @return
     */
    public static byte[] getSsidBytesFromWifiSsid(Object wifiSsid) {
        byte[] ssidBytes = null;
        try {
            Class WifiSsidClass = Class.forName("android.net.wifi.WifiSsid");
            Field octetsField = WifiSsidClass.getField("octets");//ByteArrayOutputStream octets
            ByteArrayOutputStream octets = (ByteArrayOutputStream) octetsField.get(wifiSsid);
            ssidBytes = octets.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ssidBytes;
    }

    // 得到MAC地址(手机):CC:A2:23:D7:5C:20
    public String getMacAddress() {

//        mWifiInfo = mWifiManager.getConnectionInfo();
//        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();//6.0方法失效，只返回02:00:00:00:00:00
        try {
            NetworkInterface networkInterface = NetworkInterface.getByName("wlan0");
//            LogUtil.println("localMac-------"+ByteUtil.formatMacByByte(networkInterface.getHardwareAddress()));
            return ByteUtil.formatMacByByte(networkInterface.getHardwareAddress());
        } catch (SocketException e) {
            e.printStackTrace();
            return "00:00:00:00:00:00";
        }
    }

    // 得到接入点的BSSID（路由器mac）
    public String getConnectedBSSID() {
        mWifiInfo = mWifiManager.getConnectionInfo();
        SupplicantState state = mWifiInfo.getSupplicantState();
        return (mWifiInfo == null || mWifiInfo.getBSSID() == null) ? "" : mWifiInfo.getBSSID();
    }
    @SuppressLint("MissingPermission")
    public String getConnectedSSID5_6() {
        mWifiInfo = mWifiManager.getConnectionInfo();
        String wifiSsidStr = "NULL";
        try {
            //获取原数据
            byte[] ssidBytes = getSsidBytesFromWifiSsid(getWifiSsidFromWifiInfo(mWifiInfo));
            //判断原数据是否是GBK编码
            if (StringTextUtils.isGBKString(ssidBytes)) {
                //gbk转utf-8
                wifiSsidStr = StringTextUtils.getUTF8StringFromGBKString(new String(ssidBytes, "GBK"));
            } else {//不是，默认UTF-8编码
                wifiSsidStr = mWifiInfo.getSSID();//例："我的wifi"
                if (!TextUtils.isEmpty(wifiSsidStr) && wifiSsidStr.length() > 1) {
                    wifiSsidStr = wifiSsidStr.substring(1, wifiSsidStr.length() - 1);//去掉前后引号
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (mWifiInfo != null) {
                List<ScanResult> list =  mWifiManager.getScanResults();
                for (ScanResult item : list) {
                    if (item.BSSID.equals(mWifiInfo.getBSSID())) {
                        //ScanResult.getWifiStandard()获取的是AP设备wifi标准，
                        //WifiInfo.getWifiStandard();获取的是协议wifi标准，
                        int  standard = item.getWifiStandard();
                        if (standard == ScanResult.WIFI_STANDARD_11AC ){ //WiFi5 wave1
                            wifiSsidStr = wifiSsidStr + "_WiFi5";
                        }
                        if (standard == ScanResult.WIFI_STANDARD_11AX ){ //WiFi6
                            wifiSsidStr = wifiSsidStr + "_WiFi6";
                        }
                    }
                }
//                int  standard = mWifiInfo.getWifiStandard();
//                if (standard == ScanResult.WIFI_STANDARD_11AC ){ //WiFi5 wave1
//                    wifiSsidStr = wifiSsidStr + "_WiFi5";
//                }
//                if (standard == ScanResult.WIFI_STANDARD_11AX ){ //WiFi6
//                    wifiSsidStr = wifiSsidStr + "_WiFi6";
//                }
            }
        }
        return (mWifiInfo == null) ? "NULL" : wifiSsidStr;
    }

    public String getConnectedSSID() {
        mWifiInfo = mWifiManager.getConnectionInfo();
        String wifiSsidStr = "NULL";
        try {
            //获取原数据
            byte[] ssidBytes = getSsidBytesFromWifiSsid(getWifiSsidFromWifiInfo(mWifiInfo));
            //判断原数据是否是GBK编码
            if (StringTextUtils.isGBKString(ssidBytes)) {
                //gbk转utf-8
                wifiSsidStr = StringTextUtils.getUTF8StringFromGBKString(new String(ssidBytes, "GBK"));
            } else {//不是，默认UTF-8编码
                wifiSsidStr = mWifiInfo.getSSID();//例："我的wifi"
                if (!TextUtils.isEmpty(wifiSsidStr) && wifiSsidStr.length() > 1) {
                    wifiSsidStr = wifiSsidStr.substring(1, wifiSsidStr.length() - 1);//去掉前后引号
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (mWifiInfo == null) ? "NULL" : wifiSsidStr;
    }

    public String getWifiRouteIPAddress() {
        DhcpInfo dhcpInfo = mWifiManager.getDhcpInfo();
        String routeIp = Formatter.formatIpAddress(dhcpInfo.gateway);
        return routeIp;
    }

    /**
     * 获取当前连接wifi的加密方式
     *
     * @return
     */
    @SuppressLint("MissingPermission")
    public String getConnectedCapabilities() {
        String capabilities = "";
        List<ScanResult> results = mWifiManager.getScanResults();
        for (int i = 0; i < results.size(); i++) {
            ScanResult scanResult = results.get(i);
            String currMac = getConnectedBSSID();
            String mac = scanResult.BSSID;
            if (mac.equals(currMac)) {
                // 权限（加密方式）
                // LogUtil.d("CurrentCapabilities",""+scanResult.capabilities);
                capabilities = scanResult.capabilities.replaceAll("(-CCMP|\\[ESS\\])", "").trim();
            }
        }
        return capabilities;

    }

    /**
     * 连接速度（单位：兆比特每秒-Mbps）
     *
     * @return
     */
    public int getConnectedLinkSpeed() {
        mWifiInfo = mWifiManager.getConnectionInfo();
        return (mWifiInfo == null) ? 0 : mWifiInfo.getLinkSpeed();
    }

    /**
     * 通过热点用户名和密码连接热点
     *
     * @param wifiApName
     * @param password
     * @param type       type 1:NOPASS ; 2:WEP ;   3:WPAWPA2
     */
    @SuppressLint("MissingPermission")
    public void connectWifiApByNameAndPwd(String wifiApName, String password, int type, final CallBack callBack) {
        WifiManager mWifiManager = (WifiManager) new BaseApplication().getApplicationContext().getSystemService(WIFI_SERVICE);
        //Andorid10.以下
        if (Build.VERSION.SDK_INT < 29) {
            if (TextUtils.isEmpty(password)) {
                try {
                    mWifiConfiguration = mWifiManager.getConfiguredNetworks();
                    if (mWifiConfiguration != null) {
                        for (int i = 0; i < mWifiConfiguration.size(); i++) {
                            WifiConfiguration configItem = mWifiConfiguration.get(i);
                            //匹配到指定wifi
                            if (wifiApName.equals(configItem.SSID.replace("\"", ""))) {
                                //连接wifi
                                boolean res = mWifiManager.enableNetwork(configItem.networkId, true);
                                return ;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                WifiConfiguration wifiNewConfiguration = createWifiInfo(wifiApName, password, type);//使用wpa2的wifi加密方式
                int newNetworkId = mWifiManager.addNetwork(wifiNewConfiguration);
                if (newNetworkId == -1) {
                    if (callBack != null) {
                        callBack.connnectWifResult(false, "请手动到手机Wifi列表中连接名为" + wifiApName + "的wifi!");
                    }
                    return;
                }
                // 如果wifi权限没打开（1、先打开wifi，2，使用指定的wifi
                if (!mWifiManager.isWifiEnabled()) {
                    mWifiManager.setWifiEnabled(true);
                }
                boolean enableNetwork = mWifiManager.enableNetwork(newNetworkId, true);
                if (!enableNetwork) {
                    if (callBack != null) {
                        callBack.connnectWifResult(false, "");
                    }
                    return;
                }
                if (callBack != null) {
                    callBack.connnectWifResult(true, "");
                }
            }
        } else {
            boolean isOpenWifi = mWifiManager.isWifiEnabled();
            if (!isOpenWifi) {
               new BaseApplication().getApplicationContext().startActivity(new Intent(Settings.Panel.ACTION_WIFI));
                if (callBack != null) {
                    callBack.connnectWifResult(false, "");
                }
                return;
            } else {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_WIFI_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                new BaseApplication().getApplicationContext().startActivity(intent);
            }
        }
    }

    /**
     * 获取当前信号频率(单位: 兆赫兹 MHz)
     *
     * @return
     */
    @SuppressLint("MissingPermission")
    public int getConnectedFrequency() {
        if (Build.VERSION.SDK_INT >= 21) {
            mWifiInfo = mWifiManager.getConnectionInfo();
            return (mWifiInfo == null) ? -1 : mWifiInfo.getFrequency();
        } else {
            List<ScanResult> scanResults = mWifiManager.getScanResults();
            for (ScanResult result : scanResults) {
                if (result.BSSID.equalsIgnoreCase(mWifiInfo.getBSSID())
                        && result.SSID.equalsIgnoreCase(mWifiInfo.getSSID()
                        .substring(1, mWifiInfo.getSSID().length() - 1))) {
                    return result.frequency;
                }
            }
        }
        return -1;
    }

    /**
     * 获取频率范围
     *
     * @return 2.4 / 5  (单位：GHz)
     */
    public static String getConnectedRange(int frequency, int LinkSpeed) {
        String res = "2.4";
        //若WIFI 频率未获取到，修正判断依据已连接速率大于300mbps作为5G依据； 参考  2.4g连接速率150Mbps，5G 连接速率433Mbps
        if (frequency < 100) {
            if (LinkSpeed > 400) {
                return "5";
            }
        }
        if (frequency <= 2500) {
            res = "2.4";
        } else if (frequency >= 5000) {
            res = "5";
        }
        return res;
    }

    public interface CallBack {
        void connnectWifResult(boolean connectResult, String message);
    }

    /**
     * 创建一个wifi配置
     *
     * @param SSID
     * @param Password
     * @param Type
     * @return
     */
    public WifiConfiguration createWifiInfo(String SSID, String Password, int Type) {
        WifiManager mWifiManager = (WifiManager) new BaseApplication().getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";
        WifiConfiguration tempConfig = null;
        if (isWifiEnabled()) {
            try {
                @SuppressLint("MissingPermission")
                List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
                if (existingConfigs != null) {
                    for (WifiConfiguration existingConfig : existingConfigs) {
                        if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                            tempConfig = existingConfig;
                        }
                    }
                }
            } catch (Exception e) {

            }
        }
        if (tempConfig != null) {
            mWifiManager.removeNetwork(tempConfig.networkId);
        }

        if (Type == 1) //WIFICIPHER_NOPASS
        {
            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == 2) //WIFICIPHER_WEP
        {
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + Password + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == 3) //WIFICIPHER_WPA
        {
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }

    /**
     * 移除指定wifi
     *
     * @param ssid
     * @return
     */
    @SuppressLint("MissingPermission")
    public boolean removeNetwork(String ssid) {
        boolean res = false;
        try {
            mWifiConfiguration = mWifiManager.getConfiguredNetworks();
            if (mWifiConfiguration != null) {
                for (int i = 0; i < mWifiConfiguration.size(); i++) {
                    WifiConfiguration configItem = mWifiConfiguration.get(i);
                    if (ssid.equals(configItem.SSID.replace("\"", ""))) {
                        mWifiManager.disableNetwork(configItem.networkId);
                        mWifiManager.disconnect();
                        res = mWifiManager.removeNetwork(configItem.networkId);

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * 连接指定WIFI (通用：不管有没有配置（连过的）wifi)
     *
     * @param ssid
     * @param password
     * @param type     1:NOPASS ; 2:WEP ;   3:WPAWPA2
     */
    public void connectIndexWifi(String ssid, String password, int type, CallBack callBack) {
        connectWifiApByNameAndPwd(ssid, password, type, callBack);
    }


    /**
     * 连接指定WIFI（已配置过的wifi）
     *
     * @param ssid WIFI名称
     */
    public void connectIndexWifi(String ssid) {
        connectWifiApByNameAndPwd(ssid, "", 3, null);
    }

    // 断开指定ID的网络
    public void disconnectWifi(int netId) {
        mWifiManager.disableNetwork(netId);
        mWifiManager.disconnect();
    }

    //扫描wifi是否有5G-Ap(最多扫描5次)
    private void scanAPHave5G() {
        scanBoxAPCount++;
        ArrayList<ScanResult> scanResults = new ArrayList<>();
        startScan(scanResults);
        for (int i = 0; i < scanResults.size(); i++) {
            ScanResult item = scanResults.get(i);
            int frequency = item.frequency;
            if (frequency >= 3000) {
                isSupport5G = true;
                break;
            }
        }
        if (!isSupport5G && scanBoxAPCount < 5) {
            scanAPHave5G();
        }
    }

    @SuppressLint("MissingPermission")
    public boolean isCanScan(String SSID) {
        boolean isCanScan = false;
        //得到扫描结果
        List<ScanResult> results = mWifiManager.getScanResults();
        if (results != null) {
            for (ScanResult wifiItem : results) {
                if (wifiItem.SSID.equals(SSID)) {
                    isCanScan = true;
                }
            }
        }
        return isCanScan;
    }


    /**
     * @return true if this adapter supports 5 GHz band
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean is5GHzBandSupported() {
        return mWifiManager.is5GHzBandSupported();
    }

    private boolean isSupport5G = false;
    private int scanBoxAPCount;

    /**
     * 获取802标准协议
     *
     * @param frequency 频率 MHz
     * @param linkSpeed 连接速率  Mbps
     * @return
     */
    public String get802WLANProtocol(int frequency, int linkSpeed) {
        int isSixthGeneration = -1;
        String protocol = "802.11";
        mWifiInfo = mWifiManager.getConnectionInfo();
        if (mWifiInfo != null) {
            String strWifiInfo = mWifiInfo.toString();
            //strWifiInfo=" SSID: , BSSID: 02:00:00:00:00:00, MAC: 02:00:00:00:00:00, Supplicant state: COMPLETED, Wifi Generation: 6, TWT support: false, Eight Max VHT Spatial streams support: false, RSSI: -58, Link speed: 344Mbps, Tx Link speed: 344Mbps, Rx Link speed: 413Mbps, Frequency: 2412MHz, Net ID: -1, Metered hint: false, score: 0\n";
            //小米手机支持WIFI6代技术 判断依据
            if (strWifiInfo.contains("Wifi Generation:")) {
//                int index = strWifiInfo.indexOf("Wifi Generation:");
//                isSixthGeneration = Integer.valueOf(strWifiInfo.substring(index + 16, index + 18).trim().replace("-",""));
//                if (isSixthGeneration == 6) {
//                    protocol = "802.11ax";
//                    return protocol;
//                }

                int index = strWifiInfo.indexOf("Wifi Generation:");
                String stringInfo = strWifiInfo.substring(index + 16, index + 18).trim();
                String numberString = StringTextUtils.getNumberRemoveSpecialCharacter(stringInfo);
                if (!TextUtils.isEmpty(numberString)) {
                    isSixthGeneration = Integer.valueOf(numberString);
                }
                if (isSixthGeneration == 6) {
                    protocol = "802.11ax";
                    return protocol;
                } else {
                    getProtocol(frequency, linkSpeed);
                }
            } else {
                getProtocol(frequency, linkSpeed);
            }
        }
        return protocol;

    }

    /**
     * 检测手机是否支持5G频段
     *
     * @return
     */
    public boolean checkIs5GHzBandSupported() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            isSupport5G = is5GHzBandSupported();
        } else {
            scanAPHave5G();
        }
        return isSupport5G;
    }

    /**
     * 根据频率获得信道
     *
     * @param frequency 中心频率
     * @return
     */
    public static int getConnectedChannel(int frequency) {
        int channel = 1;
        switch (frequency) {
            case 2412:
                channel = 1;
                break;
            case 2417:
                channel = 2;
                break;
            case 2422:
                channel = 3;
                break;
            case 2427:
                channel = 4;
                break;
            case 2432:
                channel = 5;
                break;
            case 2437:
                channel = 6;
                break;
            case 2442:
                channel = 7;
                break;
            case 2447:
                channel = 8;
                break;
            case 2452:
                channel = 9;
                break;
            case 2457:
                channel = 10;
                break;
            case 2462:
                channel = 11;
                break;
            case 2467:
                channel = 12;
                break;
            case 2472:
                channel = 13;
                break;
            case 2484:
                channel = 14;
                break;
            case 5035:
                channel = 7;
                break;
            case 5040:
                channel = 8;
                break;
            case 5045:
                channel = 9;
                break;
            case 5055:
                channel = 11;
                break;
            case 5060:
                channel = 12;
                break;
            case 5080:
                channel = 16;
                break;
            case 5170:
                channel = 34;
                break;
            case 5180:
                channel = 36;
                break;
            case 5190:
                channel = 38;
                break;
            case 5200:
                channel = 40;
                break;
            case 5210:
                channel = 42;
                break;
            case 5220:
                channel = 44;
                break;
            case 5230:
                channel = 46;
                break;
            case 5240:
                channel = 48;
                break;
            case 5260:
                channel = 52;
                break;
            case 5280:
                channel = 56;
                break;
            case 5300:
                channel = 60;
                break;
            case 5320:
                channel = 64;
                break;
            case 5500:
                channel = 100;
                break;
            case 5520:
                channel = 104;
                break;
            case 5540:
                channel = 108;
                break;
            case 5560:
                channel = 112;
                break;
            case 5580:
                channel = 116;
                break;
            case 5600:
                channel = 120;
                break;
            case 5620:
                channel = 124;
                break;
            case 5640:
                channel = 128;
                break;
            case 5660:
                channel = 132;
                break;
            case 5680:
                channel = 136;
                break;
            case 5700:
                channel = 140;
                break;
            case 5745:
                channel = 149;
                break;
            case 5765:
                channel = 153;
                break;
            case 5785:
                channel = 157;
                break;
            case 5805:
                channel = 161;
                break;
            case 5825:
                channel = 165;
                break;
            case 4915:
                channel = 183;
                break;
            case 4920:
                channel = 184;
                break;
            case 4925:
                channel = 185;
                break;
            case 4935:
                channel = 187;
                break;
            case 4940:
                channel = 188;
                break;
            case 4945:
                channel = 189;
                break;
            case 4960:
                channel = 192;
                break;
            case 4980:
                channel = 196;
                break;
        }
        return channel;
    }
    @SuppressLint("MissingPermission")
    public List<ScanResult> getCurrentScanResults() {
        if (mWifiManager == null) {
            mWifiManager = (WifiManager)new BaseApplication().getApplicationContext().getSystemService(WIFI_SERVICE);
        }
        //得到扫描结果
        return mWifiManager.getScanResults();
    }

    /**
     * 获取wifi 对应网关管理地址
     *
     * @return
     */
    public String intToIp() {
        int paramInt = getDhcpInfo().gateway;
        String url = "";
        url = "http://" + (paramInt & 0xFF) + "." + (0xFF & paramInt >> 8) + "." + (0xFF & paramInt >> 16) + "."
                + (0xFF & paramInt >> 24);
        return url;
    }

    private String getProtocol(int frequency, int linkSpeed) {
        String protocol = "";
        if (frequency >= 2400 && frequency < 2600 && linkSpeed <= 2) {//2.4-2.5 GHz,最高速率2 Mbps
            protocol = "802.11";
        }
        if (frequency >= 5000 && frequency < 6000 && linkSpeed <= 54) {//5.15-5.35/5.47-5.725/5.725-5.875 GHz ,最高速率11 Mbps
            protocol = "802.11a";
        }
        if (frequency >= 2400 && frequency < 2600 && linkSpeed <= 11) {//2.4GHz或者2.5GHz ,最高速率11 Mbps
            protocol = "802.11b";
        }
        if (frequency >= 2400 && frequency < 2600 && linkSpeed <= 54 && linkSpeed > 11) {//2.4GHz或者2.5GHz ,最高速率54 Mbps
            protocol = "802.11g";
        }
        if (frequency < 6000 && linkSpeed <= 600 && linkSpeed > 54) {//2.4GHz或者5GHz ,最高速率600 Mbps
            protocol = "802.11n";
        }
        if (frequency < 6000 && linkSpeed <= 7000 && linkSpeed > 600) {//2.4GHz或者5GHz ,最高速率867Mbps, 1.73 Gbps, 3.47 Gbps, 6.93 Gbps
            protocol = "802.11ac";
        }
        if (frequency > 6000 && linkSpeed > 7000) {//60GHz ,最高速率7000 Mbps
            protocol = "802.11ad";
        }
        return protocol;
    }

    /**
     * 获取连接速度干扰等级值（0-100）
     *
     * @param frequency 频率 MHz
     * @param linkSpeed 连接速率  Mbps
     * @return 真实速率与最大值的差值占最大值的百分之几（干扰值）
     */
    public static int getlinkSpeedDifferLevelValue(int frequency, int linkSpeed) {
        int differScaleValue = 10;
        String protocol = "802.11";
        if (frequency >= 2400 && frequency < 2600 && linkSpeed <= 2) {//2.4-2.5 GHz,最高速率2 Mbps
            protocol = "802.11";
            differScaleValue = (int) ((2 - linkSpeed) / 2f * 100);
        }
        if (frequency >= 5000 && frequency < 6000 && linkSpeed <= 54) {//5.15-5.35/5.47-5.725/5.725-5.875 GHz ,最高速率11 Mbps
            protocol = "802.11a";
            differScaleValue = (int) ((54 - linkSpeed) / 54f * 100);
        }
        if (frequency >= 2400 && frequency < 2600 && linkSpeed <= 11) {//2.4GHz或者2.5GHz ,最高速率11 Mbps
            protocol = "802.11b";
            differScaleValue = (int) ((11 - linkSpeed) / 11f * 100);
        }
        if (frequency >= 2400 && frequency < 2600 && linkSpeed <= 54 && linkSpeed > 11) {//2.4GHz或者2.5GHz ,最高速率54 Mbps
            protocol = "802.11g";
            differScaleValue = (int) ((54 - linkSpeed) / 54f * 100);
        }
        if (frequency < 6000 && linkSpeed <= 600 && linkSpeed > 54) {//2.4GHz或者5GHz ,最高速率600 Mbps
            protocol = "802.11n";
            differScaleValue = (int) ((300 - linkSpeed) / 300f * 100);
        }
        if (frequency < 6000 && linkSpeed <= 7000 && linkSpeed > 600) {//2.4GHz或者5GHz ,最高速率867Mbps, 1.73 Gbps, 3.47 Gbps, 6.93 Gbps
            protocol = "802.11ac";
            if (linkSpeed <= 867) {
                differScaleValue = (int) ((867 - linkSpeed) / 867f * 100);
            }
            if (linkSpeed <= 1740) {
                differScaleValue = (int) ((1740 - linkSpeed) / 1740f * 100);
            }
            if (linkSpeed <= 3480) {
                differScaleValue = (int) ((3480 - linkSpeed) / 3480f * 100);
            }
            if (linkSpeed <= 6940) {
                differScaleValue = (int) ((6940 - linkSpeed) / 6940f * 100);
            }

        }
        if (frequency > 6000 && linkSpeed > 7000) {//60GHz ,最高速率7000 Mbps
            protocol = "802.11ad";
            differScaleValue = 10;
        }

        return differScaleValue;

    }

    /**
     * 获取连接速度等级值（0-100）
     *
     * @param frequency 频率 MHz
     * @param linkSpeed 连接速率  Mbps
     * @return 真实速率占最大值的百分之几
     */
    public static int getlinkSpeedLevelValue(int frequency, int linkSpeed) {
        int speedScaleValue = 0;
        String protocol = "802.11";
        if (frequency >= 2400 && frequency < 2600 && linkSpeed <= 2) {//2.4-2.5 GHz,最高速率2 Mbps
            protocol = "802.11";
            speedScaleValue = (int) ((linkSpeed) / 2f * 100);
        }
        if (frequency >= 5000 && frequency < 6000 && linkSpeed <= 54) {//5.15-5.35/5.47-5.725/5.725-5.875 GHz ,最高速率11 Mbps
            protocol = "802.11a";
            speedScaleValue = (int) ((linkSpeed) / 54f * 100);
        }
        if (frequency >= 2400 && frequency < 2600 && linkSpeed <= 11) {//2.4GHz或者2.5GHz ,最高速率11 Mbps
            protocol = "802.11b";
            speedScaleValue = (int) ((linkSpeed) / 11f * 100);
        }
        if (frequency >= 2400 && frequency < 2600 && linkSpeed <= 54 && linkSpeed > 11) {//2.4GHz或者2.5GHz ,最高速率54 Mbps
            protocol = "802.11g";
            speedScaleValue = (int) ((linkSpeed) / 54f * 100);
        }
        if (frequency < 6000 && linkSpeed <= 600 && linkSpeed > 54) {//2.4GHz或者5GHz ,目前主流300(最高速率600 Mbps)
            protocol = "802.11n";
            speedScaleValue = (int) ((linkSpeed) / 300f * 100);
        }
        if (frequency < 6000 && linkSpeed <= 7000 && linkSpeed > 600) {//2.4GHz或者5GHz ,最高速率867Mbps, 1.73 Gbps, 3.47 Gbps, 6.93 Gbps
            protocol = "802.11ac";
            if (linkSpeed <= 867) {
                speedScaleValue = (int) ((linkSpeed) / 867f * 100);
            }
            if (linkSpeed <= 1740) {
                speedScaleValue = (int) ((linkSpeed) / 1740f * 100);
            }
            if (linkSpeed <= 3480) {
                speedScaleValue = (int) ((linkSpeed) / 3480f * 100);
            }
            if (linkSpeed <= 6940) {
                speedScaleValue = (int) ((linkSpeed) / 6940f * 100);
            }

        }
        if (frequency > 6000 && linkSpeed > 7000) {//60GHz ,最高速率7000 Mbps
            protocol = "802.11ad";
            speedScaleValue = 100;
        }
        if (frequency < 6000 && linkSpeed > 9000) {// 工作在 2.4GHz或者5GHz 频段  无线最高速率9.6Gbps
            protocol = "802.11ax";
            speedScaleValue = 100;
        }
        return speedScaleValue;

    }


    /**
     * 格式化等级
     *
     * @param progress 数值程度（0~100）
     * @return 强； 弱； 差
     */
    public static String formatLevel(int progress) {
        String level = "不可用";
        if (progress <= 30) {
            level = "差";
        } else if (progress > 30 && progress <= 70) {
            level = "良";

        } else if (progress > 70) {
            level = "优";

        }
        return level;

    }

    public static String formatLevel2(int progress) {
        String level = "低";
        if (progress <= 45) {
            level = "低";
        } else if (progress > 45 && progress <= 75) {
            level = "中";

        } else if (progress > 75) {
            level = "高";

        }
        return level;

    }


    /**
     * 场强等级
     *
     * @param rssi 场强（-50）
     * @return 强； 弱； 差
     */
    public static String rssiLevel(int rssi) {
        String level = "";
        if (rssi >= -45) {
            level = "优";
        } else if (rssi < -45 && rssi >= -65) {
            level = "良";
        } else if (rssi < -65 && rssi >= -75) {
            level = "差";
        } else {
            level = "不可用";
        }
        return level;

    }


    /**
     * 加密类型
     */
    public enum PasswordType {NONE, WEP, WPA, WPA2, WPA3, WPA_PSK, WPA2_PSK, WPA3_PSK, WPA2_PSK_TKIP, WPA3_PSK_TKIP, WPA2_PSK_AES, WPA3_PSK_AES}


    /**
     * 格式化 ，获取加密等级
     *
     * @param type 原加密类型
     * @return ##PasswordType.WEP.ordinal()## （0~11）
     */
    public static PasswordType getPasswordLevel(String type) {
        PasswordType res = PasswordType.NONE;
        if (!TextUtils.isEmpty(type)) {
            if (type.toUpperCase().contains("WEP")) {
                res = PasswordType.WEP;
            }
            if (type.toUpperCase().contains("WPA")) {
                res = PasswordType.WPA;
            }
            if (type.toUpperCase().contains("WPA2")) {
                res = PasswordType.WPA2;
            }
            if (type.toUpperCase().contains("WPA3")) {
                res = PasswordType.WPA3;
            }
            if (type.toUpperCase().contains("WPA") && type.toUpperCase().contains("PSK")) {
                res = PasswordType.WPA_PSK;
            }
            if (type.toUpperCase().contains("WPA2") && type.toUpperCase().contains("PSK")) {
                res = PasswordType.WPA2_PSK;
            }
            if (type.toUpperCase().contains("WPA3") && type.toUpperCase().contains("PSK")) {
                res = PasswordType.WPA3_PSK;
            }
            if (type.toUpperCase().contains("WPA2") && type.toUpperCase().contains("PSK") && type.toUpperCase().contains("TKIP")) {
                res = PasswordType.WPA2_PSK_TKIP;
            }
            if (type.toUpperCase().contains("WPA3") && type.toUpperCase().contains("PSK") && type.toUpperCase().contains("TKIP")) {
                res = PasswordType.WPA3_PSK_TKIP;
            }
            if (type.toUpperCase().contains("WPA2") && type.toUpperCase().contains("PSK") && type.toUpperCase().contains("AES")) {
                res = PasswordType.WPA2_PSK_AES;
            }
            if (type.toUpperCase().contains("WPA3") && type.toUpperCase().contains("PSK") && type.toUpperCase().contains("AES")) {
                res = PasswordType.WPA3_PSK_AES;
            }

        }

        return res;
    }


    /**
     * @param capabilities
     * @return 加密类型等级（0：无密码；1:WEP ;2：WPA ;3:WPA2  4:WPA/WPA2 混合）
     */
    public static int getPasswordType(String capabilities) {
        int level = 0;
        if (!TextUtils.isEmpty(capabilities)) {
            if (!capabilities.toUpperCase().contains("WPA") && !capabilities.toUpperCase().contains("WEP")) {
                level = 0;
            }
            if (capabilities.toUpperCase().contains("WEP")) {
                level = 1;
            }

            if (capabilities.toUpperCase().matches("^(.*WPA.*)$") && !capabilities.toUpperCase().contains("WPA2")) {
                level = 2;
            }
            if (capabilities.toUpperCase().matches("^(.*WPA.*)$") && capabilities.toUpperCase().contains("WPA2")) {
                level = 3;
            }
            if (capabilities.toUpperCase().matches("^(.*WPA.*WPA.*)$") && capabilities.toUpperCase().contains("WPA2")) {
                level = 4;
            }
            if (capabilities.toUpperCase().contains("WPA3")) {
                level = 5;
            }

        }
        return level;
    }


    /**
     * 认证模式 （用于华创硬件发命令）
     * <p>
     * <p>
     * OPEN;
     * SHARED;
     * WPAPSK;
     * WPA2PSK;
     * WPAPSKWPA2PSK   (混合);
     * </p>
     */
    public static String[] AUTH_MODE = {"OPEN", "SHARED", "WPAPSK", "WPA2PSK", "WPAPSKWPA2PSK"};

    /**
     * 加密方式 （用于华创硬件发命令）
     * <p>
     * NONE
     * WEP
     * TKIP
     * AES
     * TKIPAES(混合)
     */
    public static String[] ENCRYPTION = {"NONE", "WEP", "TKIP", "AES", "TKIPAES"};


    /**
     * 获取认证模式等级[华创]
     *
     * @param capabilities 例： [WPA-PSK-CCMP+TKIP][WPA2-PSK-CCMP+TKIP][WPS][ESS]
     * @return <p>
     * 0: OPEN;
     * 1:SHARED;
     * 2:WPAPSK;
     * 3:WPA2PSK;
     * 4:WPAPSKWPA2PSK   (混合);
     * </p>
     */
    public static int getAuthModeLevel(String capabilities) {
        int level = 0;
        if (!TextUtils.isEmpty(capabilities)) {
            if (!capabilities.toUpperCase().contains("WPA") && !capabilities.toUpperCase().contains("WEP")) {
                level = 0;
            }
            if (capabilities.toUpperCase().contains("WEP")) { //WEP-Shared
                level = 1;
            }

            if (capabilities.toUpperCase().matches("^(.*WPA.*)$") && !capabilities.toUpperCase().contains("WPA2")) {
                level = 2;
            }
            if (capabilities.toUpperCase().matches("^(.*WPA.*)$") && capabilities.toUpperCase().contains("WPA2")) {
                level = 3;
            }
            if (capabilities.toUpperCase().matches("^(.*WPA.*WPA.*)$") && capabilities.toUpperCase().contains("WPA2")) {
                level = 4;
            }

        }
        return level;
    }

    /**
     * 获取认证模式等级[华创] （CCMP使用AES块加密算法）
     *
     * @param capabilities 例： [WPA-PSK-CCMP+TKIP][WPA2-PSK-CCMP+TKIP][WPS][ESS]  ；<br/>	[WPA-PSK-CCMP][WPA2-PSK-CCMP][ESS]
     * @return <p>
     * 0：NONE
     * 1：WEP
     * 2：TKIP
     * 3：AES
     * 4：TKIPAES(混合)
     * </p>
     */
    public static int getEncryptionLevel(String capabilities) {
        int level = 0;
        if (!TextUtils.isEmpty(capabilities)) {
            if (!capabilities.toUpperCase().contains("WPA") && !capabilities.toUpperCase().contains("WEP")) {
                level = 0;
            }
            if (capabilities.toUpperCase().contains("WEP")) { //WEP-Shared
                level = 1;
            }

            if (capabilities.toUpperCase().matches("^(.*TKIP.*)$") && !capabilities.toUpperCase().contains("CCMP")) {
                level = 2;
            }
            if (capabilities.toUpperCase().matches("^(.*CCMP.*)$") && !capabilities.toUpperCase().contains("TKIP")) {
                level = 3;
            }
            if (capabilities.toUpperCase().matches("^(.*TKIP.*)$") && capabilities.toUpperCase().contains("CCMP")) {
                level = 4;
            }

        }
        return level;
    }


    /**
     * int转ip【只用于WifiManager获取的数值】（是ip的反转数值）
     *
     * @param paramInt
     * @return
     */
    public static String intToIpWM(int paramInt) {
        return (paramInt & 0xFF) + "." + (0xFF & paramInt >> 8) + "." + (0xFF & paramInt >> 16) + "."
                + (0xFF & paramInt >> 24);
    }


    public static String longToIp(long paramInt) {
        return (0xFF & paramInt >> 24) + "." + (0xFF & paramInt >> 16) + "." + (0xFF & paramInt >> 8) + "."
                + (0xFF & paramInt);
    }

    public static long ipToLong(String strIp) {
        String[] ip = strIp.split("\\.");
        return (Long.parseLong(ip[0]) << 24) + (Long.parseLong(ip[1]) << 16) + (Long.parseLong(ip[2]) << 8) + Long.parseLong(ip[3]);
    }

    /**
     * 计算网络号
     *
     * @param subnetMask
     * @param ip
     * @return
     */
    public static long getNetCode(String ip, String subnetMask) {
        return ipToLong(ip) & ipToLong(subnetMask);
    }

    /**
     * 获取广播地址
     *
     * @param ip
     * @param subnetMask
     * @return
     */
    public static long getBCAddress(String ip, String subnetMask) {
        if (TextUtils.isEmpty(subnetMask) || "0.0.0.0".equals(subnetMask) ||
                TextUtils.isEmpty(ip) || "0.0.0.0".equals(ip)) {//ip/子网掩码为空，返回默认广播地址
            return ipToLong("255.255.255.255");
        } else {//掩码不为空
            long qufan = getOnesComplementCode(ipToLong(subnetMask));
            return getNetCode(ip, subnetMask) | qufan;
        }

    }

    /**
     * 计算反码
     *
     * @param trueCode 原码
     * @return
     */
    public static long getOnesComplementCode(long trueCode) {
        long result = 0;
        StringBuilder sb = new StringBuilder();
        String trueBinaryStr = Long.toBinaryString(trueCode);
        if (!TextUtils.isEmpty(trueBinaryStr)) {
            String[] list = trueBinaryStr.split("");
            for (int i = 0; i < list.length; i++) {
                String item = list[i];
                if (item.equals("0")) {
                    sb.append("1");
                } else if (item.equals("1")) {
                    sb.append("0");
                }
            }
            String resultStr = sb.toString();
            if (!TextUtils.isEmpty(resultStr)) {
                result = Long.valueOf(resultStr, 2);
            }
        }
        return result;
    }

    class ScanConnectDeviceThread implements Runnable {
        private final Lock lock;
        private final int first;
        private final int count;
        private final ScanConnectDevicesCallBack scanConnectDevicesCallBack;

        public ScanConnectDeviceThread(ScanConnectDevicesCallBack scanConnectDevicesCallBack, Lock lock, int first, int count) {
            this.lock = lock;
            this.first = first;
            this.count = count;
            this.scanConnectDevicesCallBack = scanConnectDevicesCallBack;
        }

        @Override
        public void run() {
            try {
                int realCount = count;
                WifiInfo connectionInfo = mWifiManager.getConnectionInfo();
                int ipAddress = connectionInfo.getIpAddress();
                String ipString = Formatter.formatIpAddress(ipAddress);
                // LogUtil.i("scanCurrentConnectDevices", "local_ip: " + ipString +" is online!");
                String prefix = ipString.substring(0, ipString.lastIndexOf(".") + 1);
                boolean trusted = false;
                for (int i = first; i <= realCount; i++) {//多线程
                    if (!lock.isLock()) return;//上锁，线程退出
                    String testIp = prefix + i;
                    // LogUtil.i("ScanDevice", "testIp: " + testIp );
                    InetAddress address = InetAddress.getByName(testIp);
                    if (address.getCanonicalHostName().startsWith("192.")) {
                        trusted = true;
                    }
                    boolean reachable = address.isReachable(1000);
                    if (scanConnectDevicesCallBack != null) {
                        scanConnectDevicesCallBack.refreshConnectDevicesCount();
                    }
                    if (reachable) {//已连接
                        if (scanConnectDevicesCallBack != null) {
                            scanConnectDevicesCallBack.currentConnectDevices(testIp);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }//end run
    }

    public String intToIp(int paramInt) {
        return (paramInt & 0xFF) + "." + (0xFF & paramInt >> 8) + "." + (0xFF & paramInt >> 16) + "."
                + (0xFF & paramInt >> 24);
    }

    public int getMaxSupportedRxLinkSpeedMbps() {
        mWifiInfo = mWifiManager.getConnectionInfo();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return mWifiInfo.getMaxSupportedRxLinkSpeedMbps();
        }else {
            return 0;
        }
    }
    public int getMaxSupportedTxLinkSpeedMbps() {
        mWifiInfo = mWifiManager.getConnectionInfo();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return mWifiInfo.getMaxSupportedTxLinkSpeedMbps();
        }else {
            return 0;
        }
    }
}