package com.thisway.xunfeicloud;

/**
 * Created by jun on 2018/2/27.
 */

public class asrResultProcess {

    private static final String TAG = asrResultProcess.class.getSimpleName();

    public static void resultProcess(String text) {
        String c;
        int indexOf1 = text.indexOf("去");
        int indexOf2 = text.indexOf("要");
        int indexOf3 = text.indexOf("到");
        int indexOf4 = text.indexOf("向");

        if (indexOf1 != -1) {
            c = text.substring(indexOf1 + 1);
        } else if (indexOf2 != -1) {
            c = text.substring(indexOf2 + 1);
        } else if (indexOf3 != -1) {
            c = text.substring(indexOf3 + 1);
        } else if (indexOf4 != -1) {
            c = text.substring(indexOf4 + 1);
        } else {
            c = text;
        }

        //et_input.append(c);


        switch (c) {
            case "前进":
                LogUtil.i(TAG, "发送前进的数据");
                break;

            case "后退":
                LogUtil.i(TAG, "发送后退的数据");
                break;

            case "左转":
                LogUtil.i(TAG, "发送去左转的数据");
                break;

            case "右转":
                LogUtil.i(TAG, "发送去右转的数据");
                break;

            case "开灯":
                LogUtil.i(TAG, "发送去开灯的数据");
                break;

            case "关灯":
                LogUtil.i(TAG, "发送去关灯的数据");
                break;

            case "打电话":
                LogUtil.i(TAG, "发送去打电话的数据");
                break;

            case "发短信":
                LogUtil.i(TAG, "发送去发短信的数据");
                break;

            case "点外卖":
                LogUtil.i(TAG, "发送去点外卖的数据");
                break;


            case "图书馆":
                LogUtil.i(TAG, "发送图书馆的数据");
                break;

            case "群英楼":
                LogUtil.i(TAG, "发送去群英楼的数据");
                break;


            case "中心楼":
                LogUtil.i(TAG, "发送去中心楼的数据");
                break;

            case "中山院":
                LogUtil.i(TAG, "发送去中山院的数据");
                break;

            case "沙糖园":
                LogUtil.i(TAG, "发送去沙糖园的数据");
                break;

            case "香园":
                LogUtil.i(TAG, "发送去香园的数据");
                break;

            case "东南大学":
                LogUtil.i(TAG, "发送去东南大学的数据");
                break;

            default:
                LogUtil.i(TAG, "无法匹配命令词，请重新发送命令");
                break;


        }

         return;
    }



}
