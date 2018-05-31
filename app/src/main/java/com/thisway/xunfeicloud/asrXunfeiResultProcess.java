package com.thisway.xunfeicloud;


import android.util.Log;

import org.litepal.crud.DataSupport;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jun on 2018/5/7.
 */

public class asrXunfeiResultProcess {

    private static final String TAG = asrXunfeiResultProcess.class.getSimpleName();
    private static  int  result = 0;



    public static String resultProcess(String text) {


        LogUtil.i(TAG,"asrXunfeiResultProcess");

        StringBuffer ret = new StringBuffer() ;

        String target = text;
        //String key = "健身房";
        //String reg = "(\\w*)(健身房)(\\w*)";
       // String reg = "\\w*" + "健身房" + "\\w*";
        //String reg = "\\w*" + key + "\\w*";
        String[] key = {"健身房", "电梯","楼梯", "餐厅","中餐厅", "西餐厅","会议室", "失物招领处",
                      "行李寄存处", "附近的银行","附近的商场", "附近的机场","娱乐中心", "咖啡馆","附近的地铁",
                      "点餐", "洗衣","用餐", "退房","入住","房型", "押金","寄存行李"," 商店","换房间",
                      "延长住店","换币服务","叫醒服务","付款方式","优惠","房型",
                      "前进", "后退","原地左转", " 原地右转","前进左转"," 前进右转",};
        for (int i = 0;i < key.length ;i++ )
        {
            String reg = "\\w*" + key[i] + "\\w*";
            Pattern pattern = Pattern.compile(reg);
            Matcher matcher = pattern.matcher(target);
            boolean b = matcher.matches();
            LogUtil.i(TAG,i +":"+ b);
            if (b)
            {

                result = i + 1;
                LogUtil.i(TAG,""+result);
                xunfeiAsr xunfeiasr = DataSupport.find(xunfeiAsr.class,result);
                Log.d("Data", " id is " + xunfeiasr.getId());
                Log.d("Data", " keyID is " + xunfeiasr.getKeyID());
                Log.d("Data", " key is " + xunfeiasr.getKey());
                Log.d("Data", "answer is " + xunfeiasr.getAnswer());
                ret.append( xunfeiasr.getAnswer()) ;
                break;
            }
        }

        String asrAnswer = ret.toString();
        Log.d("Data", asrAnswer);
        if (asrAnswer == "")
            asrAnswer = "没有回答，将转语义理解，请再说一次您的问题";

        return asrAnswer;

    }


}
