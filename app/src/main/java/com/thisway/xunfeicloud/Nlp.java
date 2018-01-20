package com.thisway.xunfeicloud;

import android.os.Bundle;
import android.text.TextUtils;

import com.iflytek.aiui.AIUIAgent;
import com.iflytek.aiui.AIUIConstant;

import org.json.JSONObject;

/**
 * Created by jun on 2018/1/20.
 */

public class Nlp {

    private static String TAG = Nlp.class.getSimpleName();


    public static String parseNlsrResult(Bundle aiuIdata, String info) {

        StringBuffer ret = new StringBuffer() ;

        try {
            //data字段携带结果数据，info字段为描述数据的JSON字符串
            JSONObject bizParamJson = new JSONObject(info);
            JSONObject data = bizParamJson.getJSONArray("data").getJSONObject(0);
            JSONObject params = data.getJSONObject("params");
            JSONObject content = data.getJSONArray("content").getJSONObject(0);

            if (content.has("cnt_id")) {
                String cnt_id = content.getString("cnt_id");
                String cntStr = new String(aiuIdata.getByteArray(cnt_id), "utf-8");
                //String(byte[] bytes, Charset charset) 通过使用指定的 charset 解码指定的 byte 数组，构造一个新的 String。
                //public byte[] getByteArray (String key)  功能：获取key对应的byte数组

                // 获取该路会话的id，将其提供给支持人员，有助于问题排查
                // 也可以从Json结果中看到
                String sid = aiuIdata.getString("sid");

                // 获取从数据发送完到获取结果的耗时，单位：ms
                // 也可以通过键名"bos_rslt"获取从开始发送数据到获取结果的耗时
                long eosRsltTime = aiuIdata.getLong("eos_rslt", -1);//获取key对应的long值  没找到 则返回默认值
                //mTimeSpentText.setText(eosRsltTime + "ms");

                if (TextUtils.isEmpty(cntStr)) {
                    ret.append("该会话出错"+sid);
                    return ret.toString();
                }

                JSONObject cntJson = new JSONObject(cntStr);

                String sub = params.optString("sub");

                if ("nlp".equals(sub)) {
                    // 解析得到语义结果
                    String resultStr = cntJson.optString("intent");
                    LogUtil.i( TAG, resultStr );
                    JSONObject intent1 = cntJson.getJSONObject("intent");
                    int i = intent1.length();

                    if ( i != 0) {
                        String text;
                        String question = intent1.optString("text");
                        if (intent1.has("answer")) {
                            String answer2 = intent1.optString("answer");
                            JSONObject answer = new JSONObject(answer2);
                            text = answer.optString("text");
                            //只是当无返回值时，getString(String name)抛出错误，optString(String name)返回空值
                        } else {
                            text = "你的问题太难了，无法回答";
                            //text = getString(R.string.noanswer);
                        }
                        ret.append(question + ":" + text);
                        ret.append("\n");
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            ret.append("该会话出错");
        }
        return ret.toString();
    }

}
