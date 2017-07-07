package org.ndk.ndkfirst;

import android.util.Log;

public class NDKTest {

    private static final String TAG = "NDKTest";

    static {
        System.loadLibrary("ndk-test-lib");
    }

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


    /**
     * 输出native的修改结果
     */
    public void outputString() {
        Log.i(TAG, "mString: " + mString);
    }

    /**
     * native中会调用此方法
     */
    @Override
    public String toString() {
        Log.i(TAG, "toString: ");
        return mString;
    }

    /**
     * native层返回一个int[][]数组
     */
    public native static int[][] getIntArray(int row, int column);

    /**
     * native返回一个{@link Student}对象,调用无参构造函数
     */
    public native Student getStudentFromNative();

    /**
     * native返回一个{@link Student}对象,调用构造函数
     */
    public native Student getStudentFromNative2();

    /**
     * 在native层打印出Student对象
     * @param stu
     */
    public native void outputStudentInNative(Student stu);
}
