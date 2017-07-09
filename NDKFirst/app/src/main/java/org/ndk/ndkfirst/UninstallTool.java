package org.ndk.ndkfirst;

public class UninstallTool {

    static {
        System.loadLibrary("uninstall-tool");
    }

    public native static void setUninstallAction(String package_name,String url);

}
