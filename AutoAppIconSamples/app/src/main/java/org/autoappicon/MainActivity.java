package org.autoappicon;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private ComponentName mComponentName1111;

    private ComponentName mComponentName1212;

    private ComponentName mComponentNameDefault;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String packageName = getPackageName();

        mComponentName1111 = new ComponentName(this, packageName + ".MainActivity1111");
        mComponentName1212 = new ComponentName(this, packageName + ".MainActivity1212");
        mComponentNameDefault = new ComponentName(this, packageName + ".MainActivity");
    }

    /**
     * 双十一图标
     */
    public void onChangeIconFor1111(View view) {
        enableComponent(mComponentName1111);
        disableComponent(mComponentName1212);
        disableComponent(mComponentNameDefault);
    }

    /**
     * 双十二
     */
    public void onChangeIconFor1212(View view) {
        enableComponent(mComponentName1212);
        disableComponent(mComponentName1111);
        disableComponent(mComponentNameDefault);
    }

    /**
     * 活动结束，还原默认图标
     */
    public void onRestoreIcon(View view) {
        enableComponent(mComponentNameDefault);
        disableComponent(mComponentName1212);
        disableComponent(mComponentName1111);
    }

    private void enableComponent(ComponentName componentName) {
        getPackageManager().setComponentEnabledSetting(componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    private void disableComponent(ComponentName componentName) {
        getPackageManager().setComponentEnabledSetting(componentName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
}
