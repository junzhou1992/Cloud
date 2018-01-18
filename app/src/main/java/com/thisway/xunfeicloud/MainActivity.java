package com.thisway.xunfeicloud;

import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.iflytek.aiui.AIUIAgent;
import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIEvent;
import com.iflytek.aiui.AIUIListener;
import com.iflytek.aiui.AIUIMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private Toast mToast;

    private EditText et_input;
    private Button btn_startspeech, btn_startspeektext,btn_startrecognize,btn_startnlp ;


    private AIUIAgent mAIUIAgent = null;
    private String mSyncSid = "";
    private int mAIUIState = AIUIConstant.STATE_IDLE;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        initView() ;


    }

    private void initView() {
        setContentView(R.layout.activity_main) ;
        et_input = (EditText) findViewById(R.id.et_input );
        btn_startspeech = (Button) findViewById(R.id.btn_startspeech );
        btn_startspeektext = (Button) findViewById(R.id.btn_startspeektext );
        btn_startrecognize = (Button) findViewById(R.id.btn_startrecognize );
        btn_startnlp = (Button) findViewById(R.id.btn_startnlp );

        btn_startspeech .setOnClickListener(this) ;
        btn_startspeektext .setOnClickListener(this) ;
        btn_startrecognize.setOnClickListener(this);
        btn_startnlp.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_startnlp:// 语法理解
                nlptest();
                break;

        }

    }

    private void nlptest(){
        showTip("duihua");
        createAgent();
        startVoiceNlp();

/*        Intent intent = null;
        intent = new Intent(MainActivity.this, TestActivity.class);
        if (intent != null) {
            startActivity(intent);
       }*/

    }

    private void createAgent() {
        if (null == mAIUIAgent) {
            //Log.i(TAG, "create aiui agent");
            mAIUIAgent = AIUIAgent.createAgent(this, getAIUIParams(), mAIUIListener);
        }

        if (null == mAIUIAgent) {
            final String strErrorTip = "创建AIUIAgent失败！";
            LogUtil.i(TAG,strErrorTip);
            //showTip(strErrorTip);
            this.et_input.setText(strErrorTip);
        } else {
            LogUtil.i(TAG,"AIUIAgent已创建");
            //showTip("AIUIAgent已创建");
        }
    }

    /*
    * 这段其实是填写上自己的APPID
    * */
    private String getAIUIParams() {
        String params = "";

        AssetManager assetManager = getResources().getAssets();
        try {
            InputStream ins = assetManager.open( "cfg/aiui_phone.cfg" );
            byte[] buffer = new byte[ins.available()];
            //available() 返回此输入流下一个方法调用可以不受阻塞地从此输入流读取（或跳过）的估计字节数。

            ins.read(buffer);
            //  read(byte[] b)从输入流中读取一定数量的字节，并将其存储在缓冲区数组 b 中。
            ins.close();
            //   close() 关闭此输入流并释放与该流关联的所有系统资源。

            params = new String(buffer);
            //String(byte[] bytes)  通过使用平台的默认字符集解码指定的 byte 数组，构造一个新的 String。

            //LogUtil.i(TAG,params);
            JSONObject paramsJson = new JSONObject(params);  //以params来构建JSONObject
            paramsJson.getJSONObject("login").put("appid", getString(R.string.app_id));

            params = paramsJson.toString();  //调用toString()方法可直接将其内容显现出来
            //LogUtil.i(TAG,params);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return params;
    }

    private AIUIListener mAIUIListener = new AIUIListener() {

        @Override
        public void onEvent(AIUIEvent event) {
            Log.i( TAG,  "on event: " + event.eventType );

            switch (event.eventType) {
                case AIUIConstant.EVENT_CONNECTED_TO_SERVER:
                    showTip("已连接服务器");
                    break;

                case AIUIConstant.EVENT_SERVER_DISCONNECTED:
                    showTip("与服务器断连");
                    break;

                //唤醒事件
                case AIUIConstant.EVENT_WAKEUP:
                    showTip( "进入识别状态" );
                    break;

                //结果事件
                case AIUIConstant.EVENT_RESULT: {
                    try {
                        //LogUtil.e(TAG,event.info);
                        JSONObject bizParamJson = new JSONObject(event.info);
                        JSONObject data = bizParamJson.getJSONArray("data").getJSONObject(0);
                        JSONObject params = data.getJSONObject("params");
                        JSONObject content = data.getJSONArray("content").getJSONObject(0);

                        if (content.has("cnt_id")) {
                            String cnt_id = content.getString("cnt_id");
                            String cntStr = new String(event.data.getByteArray(cnt_id), "utf-8");
                            //String(byte[] bytes, Charset charset) 通过使用指定的 charset 解码指定的 byte 数组，构造一个新的 String。
                            //public byte[] getByteArray (String key)  功能：获取key对应的byte数组


                            // 获取该路会话的id，将其提供给支持人员，有助于问题排查
                            // 也可以从Json结果中看到
                            String sid = event.data.getString("sid");

                            // 获取从数据发送完到获取结果的耗时，单位：ms
                            // 也可以通过键名"bos_rslt"获取从开始发送数据到获取结果的耗时
                            long eosRsltTime = event.data.getLong("eos_rslt", -1);//获取key对应的long值  没找到 则返回默认值
                            //mTimeSpentText.setText(eosRsltTime + "ms");

                            if (TextUtils.isEmpty(cntStr)) {
                                return;
                            }
                            //LogUtil.e(TAG,cntStr);
                            JSONObject cntJson = new JSONObject(cntStr);

                            if (et_input.getLineCount() > 1000) {
                                et_input.setText("");
                            }

                           // et_input.append( "\n" );
                           // et_input.append(cntJson.toString());
                           // et_input.setSelection(et_input.getText().length());

                            String sub = params.optString("sub");


                            if ("nlp".equals(sub)) {
                                // 解析得到语义结果
                                String resultStr = cntJson.optString("intent");
                                JSONObject intent1 = cntJson.getJSONObject("intent");

                               int i = intent1.length();

                                //Log.e( TAG, resultStr );
                                //Log.e( TAG, String.valueOf(i) );
                                if ( i != 0) {
                                    String text;
                                    String question = intent1.optString("text");

                                    if (intent1.has("answer")) {
                                        String answer2 = intent1.optString("answer");
                                        JSONObject answer = new JSONObject(answer2);
                                        text = answer.optString("text");
                                        //只是当无返回值时，getString(String name)抛出错误，optString(String name)返回空值

                                        et_input.append(question + ":" + text);
                                        et_input.append("\n");
                                        //et_input.setSelection(et_input.getText().length());
                                    } else {
                                        text = "你的问题太难了";
                                        et_input.append(question + ":" + text);
                                        et_input.append("\n");
                                    }
                                }

                            }
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                       // et_input.append( "\n" );
                       // et_input.append( e.getLocalizedMessage());
                    }

                   // et_input.append( "\n" );
                } break;

                //错误事件
                case AIUIConstant.EVENT_ERROR: {
                    et_input.append( "\n" );
                    et_input.append( "错误: " + event.arg1+"\n" + event.info );
                } break;

                case AIUIConstant.EVENT_VAD: {
                    if (AIUIConstant.VAD_BOS == event.arg1) {
                        showTip("找到vad_bos");
                    } else if (AIUIConstant.VAD_EOS == event.arg1) {
                        showTip("找到vad_eos");
                    } else {
                        showTip("" + event.arg2);
                    }
                } break;

                case AIUIConstant.EVENT_START_RECORD: {
                    showTip("已开始录音");
                } break;

                case AIUIConstant.EVENT_STOP_RECORD: {
                    showTip("已停止录音");
                } break;

                case AIUIConstant.EVENT_STATE: {	// 状态事件
                    mAIUIState = event.arg1;

                    if (AIUIConstant.STATE_IDLE == mAIUIState) {
                        // 闲置状态，AIUI未开启
                        showTip("STATE_IDLE");
                    } else if (AIUIConstant.STATE_READY == mAIUIState) {
                        // AIUI已就绪，等待唤醒
                        showTip("STATE_READY");
                    } else if (AIUIConstant.STATE_WORKING == mAIUIState) {
                        // AIUI工作中，可进行交互
                        showTip("STATE_WORKING");
                    }
                } break;

                case AIUIConstant.EVENT_CMD_RETURN: {
                    if (AIUIConstant.CMD_SYNC == event.arg1) {	// 数据同步的返回
                        int dtype = event.data.getInt("sync_dtype", -1);
                        int retCode = event.arg2;

                        switch (dtype) {
                            case AIUIConstant.SYNC_DATA_SCHEMA: {
                                if (AIUIConstant.SUCCESS == retCode) {
                                    // 上传成功，记录上传会话的sid，以用于查询数据打包状态
                                    // 注：上传成功并不表示数据打包成功，打包成功与否应以同步状态查询结果为准，数据只有打包成功后才能正常使用
                                    mSyncSid = event.data.getString("sid");

                                    // 获取上传调用时设置的自定义tag
                                    String tag = event.data.getString("tag");

                                    // 获取上传调用耗时，单位：ms
                                    long timeSpent = event.data.getLong("time_spent", -1);
                                    if (-1 != timeSpent) {
                                        //mTimeSpentText.setText(timeSpent + "ms");
                                    }

                                    showTip("上传成功，sid=" + mSyncSid + "，tag=" + tag + "，你可以试着说“打电话给刘德华”");
                                } else {
                                    mSyncSid = "";
                                    showTip("上传失败，错误码：" + retCode);
                                }
                            } break;
                        }
                    } else if (AIUIConstant.CMD_QUERY_SYNC_STATUS == event.arg1) {	// 数据同步状态查询的返回
                        // 获取同步类型
                        int syncType = event.data.getInt("sync_dtype", -1);
                        if (AIUIConstant.SYNC_DATA_QUERY == syncType) {
                            // 若是同步数据查询，则获取查询结果，结果中error字段为0则表示上传数据打包成功，否则为错误码
                            String result = event.data.getString("result");

                            showTip(result);
                        }
                    }
                } break;

                default:
                    break;
            }
        }

    };

    private void startVoiceNlp(){
        if (null == mAIUIAgent) {
            showTip("AIUIAgent为空，请先创建");
            return;
        }

        Log.i( TAG, "start voice nlp" );
        et_input.setText("");

        // 先发送唤醒消息，改变AIUI内部状态，只有唤醒状态才能接收语音输入
        // 默认为oneshot模式，即一次唤醒后就进入休眠。可以修改aiui_phone.cfg中speech参数的interact_mode为continuous以支持持续交互
        if (AIUIConstant.STATE_WORKING != mAIUIState) {
            AIUIMessage wakeupMsg = new AIUIMessage(AIUIConstant.CMD_WAKEUP, 0, 0, "", null);
            mAIUIAgent.sendMessage(wakeupMsg);
        }

        // 打开AIUI内部录音机，开始录音。若要使用上传的个性化资源增强识别效果，则在参数中添加pers_param设置
        // 个性化资源使用方法可参见http://doc.xfyun.cn/aiui_mobile/的用户个性化章节
        String params = "sample_rate=16000,data_type=audio,pers_param={\"uid\":\"\"}";
        AIUIMessage startRecord = new AIUIMessage(AIUIConstant.CMD_START_RECORD, 0, 0, params, null);

        mAIUIAgent.sendMessage(startRecord);
    }



    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }


}
