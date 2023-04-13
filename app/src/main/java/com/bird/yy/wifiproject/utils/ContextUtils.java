package com.bird.yy.wifiproject.utils;

import com.bird.yy.wifiproject.base.BaseApplication;

import java.lang.reflect.Method;

public class ContextUtils {
    /**
     * 获取当前应用的Application
     * 先使用ActivityThread里获取Application的方法，如果没有获取到，
     * 再使用AppGlobals里面的获取Application的方法
     *
     * @return
     */
    public static BaseApplication getCurApplication() {
        BaseApplication application = null;
        try {
            Class atClass = Class.forName("android.app.ActivityThread");
            Method currentApplicationMethod = atClass.getDeclaredMethod("currentApplication");
            currentApplicationMethod.setAccessible(true);
            application = (BaseApplication) currentApplicationMethod.invoke(null);
        } catch (Exception e) {
        }

        if (application != null)
            return application;

        try {
            Class atClass = Class.forName("android.app.AppGlobals");
            Method currentApplicationMethod = atClass.getDeclaredMethod("getInitialApplication");
            currentApplicationMethod.setAccessible(true);
            application = (BaseApplication) currentApplicationMethod.invoke(null);
        } catch (Exception e) {
        }

        return application;
    }

}