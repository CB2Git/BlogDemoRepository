package org.ndk.eventinjector.utils;


import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;

public class KeyTouchInjector {

    private static final String TAG = "KeyTouchInjector";

    static {
        System.loadLibrary("KeyTouchInjector");
    }

    /**
     * 检查是否有root权限
     */
    public native static boolean checkSU();

    /**
     * 初始化/dev/input/下面的设备权限为666
     * android 4.3以后需要特殊权限
     */
    private native static boolean initInjector(int version);


    /**
     * 还原/dev/input/下面的设备权限为600
     */
    private native static boolean recoveryInjector(int version);

    /**
     * 找到能被注入事件的驱动
     */
    public native static String findKeyTouchDevice();

    /**
     * native注入的位置，因为Android上层与下层不对应，所以加一个中转层
     */
    private native static boolean nativeInjectKeyEvent(String devPath, int keyCode);

    public static boolean init() {
        return initInjector(Build.VERSION.SDK_INT);
    }

    /**
     * 恢复被修改的文件权限
     */
    public static boolean recovery() {
        return recoveryInjector(Build.VERSION.SDK_INT);
    }

    /**
     * 参考linux/input.h中定义的键值
     */
    private static int parseKeyCode(int keyCode) {
        int parseKeyCode = -1;
        switch (keyCode) {
            case KeyEvent.KEYCODE_0:
                parseKeyCode = 11;
                break;
            case KeyEvent.KEYCODE_1:
                parseKeyCode = 2;
                break;
            case KeyEvent.KEYCODE_2:
                parseKeyCode = 3;
                break;
            case KeyEvent.KEYCODE_3:
                parseKeyCode = 4;
                break;
            case KeyEvent.KEYCODE_4:
                parseKeyCode = 5;
                break;
            case KeyEvent.KEYCODE_5:
                parseKeyCode = 6;
                break;
            case KeyEvent.KEYCODE_6:
                parseKeyCode = 7;
                break;
            case KeyEvent.KEYCODE_7:
                parseKeyCode = 8;
                break;
            case KeyEvent.KEYCODE_8:
                parseKeyCode = 9;
                break;
            case KeyEvent.KEYCODE_9:
                parseKeyCode = 10;
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                parseKeyCode = 114;
                break;
            default:
                parseKeyCode = -1;
                break;
        }
        return parseKeyCode;
    }

    /**
     * 注入一个按键消息
     */
    public static boolean injectKeyEvent(String devPath, int keyCode) {
        Log.i(TAG, "injectKeyEvent: devPath = " + devPath);
        if (TextUtils.isEmpty(devPath)) {
            return false;
        }
        int parseKeyCode = parseKeyCode(keyCode);
        if (parseKeyCode == -1) {
            return false;
        }
        return nativeInjectKeyEvent(devPath, parseKeyCode);
    }
}
