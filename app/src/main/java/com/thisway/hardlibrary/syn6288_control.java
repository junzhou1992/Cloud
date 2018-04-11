package com.thisway.hardlibrary;

/**
 * Created by jun on 2018/3/27.
 */

public class syn6288_control {

    public static native int open_syn6288();
    public static native int tts( byte[] text);
    public static native int status();
    public static native void close_syn6288();

    static {
        try {
            System.loadLibrary("syn6288_control");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
