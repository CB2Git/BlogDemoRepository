package org.ndk.eventinjector.utils;


/**
 * 读写/dev/input/eventX写入事件<p>
 * 需要对不同的设备进行底层适配
 */
public class KeyTouchInjector2 {

    private static final String TAG = "KeyTouchInjector2";

    static {
        System.loadLibrary("KeyTouchInjector2");
    }

    /**
     * 初始化/dev/uinput 权限为666
     * android 4.3以后需要特殊权限
     */
    public native static boolean initInjector(int version);

    public native static boolean sendKey();

    public native static boolean sendTouch();

    public native static boolean destroy();


}
