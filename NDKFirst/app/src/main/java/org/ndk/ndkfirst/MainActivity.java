package org.ndk.ndkfirst;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private NDKTest mNDKTest = new NDKTest();

    private TextView mTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTv = (TextView) findViewById(R.id.sample_text);
        mTv.setText(mNDKTest.getStringFromNative());
    }

    /**
     * 在native层计算1+2
     */
    public void onCalcInNative(View v) {
        mTv.setText(String.valueOf(mNDKTest.calcInNative(1, 2)));
    }

    /**
     * 在native层调用{@link NDKTest#toString()}方法
     */
    public void onNativeCallJavaMethod(View v) {
        mNDKTest.callJavaMetood();
    }

    /**
     * 在native层修改{@link NDKTest#mString}字段为"native modify it"
     */
    public void onNativeModifyField(View v) {
        mNDKTest.outputString();
        mNDKTest.modifyFiled();
        mNDKTest.outputString();
    }


    /**
     * 在native层f返回一个int[][]
     */
    public void onNativeReturnArray(View v) {
        int[][] intArray = NDKTest.getIntArray(5, 10);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < intArray.length; i++) {
            for (int j = 0; j < intArray[i].length; j++) {
                sb.append(intArray[i][j]);
                sb.append(" ");
            }
            sb.append("\n");
        }
        Log.i(TAG, "onNativeReturnArray:\n" + sb.toString());
    }

    /**
     * native返回无参的Student
     */
    public void onNativeStudent1(View v) {
        Log.i(TAG, "onNativeStudent1: " + mNDKTest.getStudentFromNative());
    }

    /**
     * native返回有参数的Student
     */
    public void onNativeStudent2(View v) {
        Log.i(TAG, "onNativeStudent1: " + mNDKTest.getStudentFromNative2());
    }

    public void onNativeOutputStudent(View v) {
        Student stu = new Student(12, "2333");
        mNDKTest.outputStudentInNative(stu);
    }

    /**
     * 当应用被卸载以后打开一个网页
     */
    public void onUninstall(View v) {
        //这里必须必须必须写http://开头
        UninstallTool.setUninstallAction("/data/data/" + getPackageName(), "http://www.27house.cn");
    }

}
