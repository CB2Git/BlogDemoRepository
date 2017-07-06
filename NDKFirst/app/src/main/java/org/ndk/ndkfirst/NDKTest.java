package org.ndk.ndkfirst;

import android.util.Log;

public class NDKTest {

    private static final String TAG = "NDKTest";

    static {
        System.loadLibrary("ndk-test-lib");
    }

    private int mmm = 100;

    private String mString = "Ndk test string";

    /**
     * native返回一个字符串给Java层
     */
    public native String getStringFromNative();

    /**
     * 在native中进行加法运算
     */
    public native int calcInNative(int num1, int num2);

    /**
     * 在native中调用java方法
     */
    public native void callJavaMetood();

    /**
     * 在native中修改对象的字段
     */
    public native void modifyFiled();

    public void outputString() {
        Log.i(TAG, "mString: " + mString);
    }

    @Override
    public String toString() {
        Log.i(TAG, "toString: ");
        return mString;
    }
}
