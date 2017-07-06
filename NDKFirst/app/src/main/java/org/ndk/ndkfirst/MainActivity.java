package org.ndk.ndkfirst;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

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
}
