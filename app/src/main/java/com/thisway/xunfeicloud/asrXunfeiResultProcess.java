package com.thisway.xunfeicloud;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jun on 2018/5/7.
 */

public class asrXunfeiResultProcess {

    private static final String TAG = asrXunfeiResultProcess.class.getSimpleName();
    private static  int  result= 0;


    public static String resultProcess(String text) {
        // 这种产品是红色的还是蓝色的啊？
        //这种产品是水货还是正品呢？

        // 我们要提取“红色”“蓝色”和 “水货”，“正品”这些关键字

        LogUtil.i(TAG,"asrXunfeiResultProcess");

        StringBuffer ret = new StringBuffer() ;

        String target = text;
        //String key = "健身房";
        //String reg = "(\\w*)(健身房)(\\w*)";
       // String reg = "\\w*" + "健身房" + "\\w*";
        //String reg = "\\w*" + key + "\\w*";
        String[] key = {"健身房", "电梯","楼梯", "餐厅","中餐厅", " 西餐厅","会议室", "失物招领处",
                      "行李寄存处", "附近银行","附近商场", "附近机场","娱乐中心", " 咖啡馆","附近地铁"};
        for (int i = 0;i < key.length ;i++ )
        {
            String reg = "\\w*" + key[i] + "\\w*";
            Pattern pattern = Pattern.compile(reg);
            Matcher matcher = pattern.matcher(target);
            boolean b = matcher.matches();
            LogUtil.i(TAG,""+b);
            if (b)
            {
                result = i + 1;
                LogUtil.i(TAG,""+result);
                break;
            }
        }




        switch(result){
            case 0:
                LogUtil.i(TAG, "没有匹配结果");
                ret.append( "没有匹配结果.") ;
                break;

            case 1:
                LogUtil.i(TAG, "发送健身房的数据");
                ret.append( "发送健身房的数据") ;
                break;

            case 2:
                LogUtil.i(TAG, "发送楼梯的数据");
                ret.append( "发送楼梯的数据") ;
                break;

        }

        return ret.toString();

    }


}
