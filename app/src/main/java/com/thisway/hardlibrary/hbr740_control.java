package com.thisway.hardlibrary;

/**
 * Created by jun on 2018/3/27.
 */

public class hbr740_control {

    public static native int open_hbr();
    public static native int set_group(int group);
    public static native int hbr_asr();
    public static native void close_hbr();
    static {
        try {
            System.loadLibrary("hbr740_control");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
