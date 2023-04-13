package com.bird.yy.wifiproject.utils;

/**
 * 对象锁（控制子线程）
 * Created by Administrator on 2018/1/3 0003.
 */

public class Lock {

    private boolean lock = true;

    public boolean isLock() {
        return lock;
    }

    public void setLock(boolean lock) {
        this.lock = lock;
    }
}