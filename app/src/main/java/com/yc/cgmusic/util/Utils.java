package com.yc.cgmusic.util;

/**
 * Created by Administrator on 2016/1/2.
 */
public class Utils {
    //防止按钮多次点击
    private static long lastClickTime;
    public synchronized static boolean isFastClick() {
        long time = System.currentTimeMillis();
        if ( time - lastClickTime < 500) {
            return true;
        }
        lastClickTime = time;
        return false;
    }
}
