package com.thisway.xunfeicloud;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.iflytek.aiui.AIUIAgent;
import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIEvent;
import com.iflytek.aiui.AIUIListener;
import com.iflytek.aiui.AIUIMessage;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.GrammarListener;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class SpeechActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = SpeechActivity.class.getSimpleName();
    private Toast mToast;
    private SharedPreferences mSharedPreferences,mSharedPreferences_asr ;
    private SpeechRecognizer mAsr;

    private static final String KEY_GRAMMAR_ABNF_ID = "grammar_abnf_id";
    private static final String GRAMMAR_TYPE_ABNF = "abnf";

    private EditText et_input;
    private Button btn_celnlp, btn_startspeektext,btn_startrecognize,btn_startnlp ;

    private String mEngineType = SpeechConstant.TYPE_CLOUD;
    private AIUIAgent mAIUIAgent = null;
    private int mAIUIState = AIUIConstant.STATE_IDLE;

    private SpeechSynthesizer mTts;
    private String voicer = "xiaoyan";  // 默认发音人

    private String[] mCloudVoicersEntries;
    private String[] mCloudVoicersValue ;

    //praviate int state = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.e(TAG,"onCreate");
        mSharedPreferences = getSharedPreferences(settingFragment.PREFER_NAME, MODE_PRIVATE);

        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        initView() ;
        createAgent();
        speekText("您好,请问您有什么问题");

    }



    //重写onCreateOptionsMenu方法
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.backup:
                Toast.makeText(this, "You clicked backup", Toast.LENGTH_SHORT).show();
                break;

            case R.id.settings:
                if(SpeechConstant.TYPE_CLOUD.equals(mEngineType)){
                    Intent intent = new Intent(SpeechActivity.this, settingActivity.class);
                    startActivity(intent);
                }else{
                    showTip("请前往xfyun.cn下载离线合成体验");
                }
                break;

            case R.id.person_select:
                 showPresonSelectDialog();
                break;

            default:
        }
        return true;
    }


    private void initView() {
        setContentView(R.layout.activity_speech) ;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toobar);
        setSupportActionBar(toolbar);
        et_input = (EditText) findViewById(R.id.et_input );
        btn_celnlp= (Button) findViewById(R.id.btn_cacelnlp );
        btn_startspeektext = (Button) findViewById(R.id.btn_startspeektext );
        btn_startrecognize = (Button) findViewById(R.id.btn_startrecognize );
        btn_startnlp = (Button) findViewById(R.id.btn_startnlp );


        btn_celnlp .setOnClickListener(this) ;
        btn_startspeektext .setOnClickListener(this) ;
        btn_startrecognize.setOnClickListener(this);
        btn_startnlp.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_cacelnlp:// 取消语义理解
                stopVoiceNlp();
                break;
            case R.id.btn_startnlp:// 语义理解
                startVoiceNlp();
                break;

            case R.id. btn_startspeektext:// 语音合成（把文字转声音）
                String text = et_input.getText().toString();
                speekText(text);
                break;

            case R.id.btn_startrecognize://语法识别（完成语音命令的识别）
                //stopVoiceNlp();
                asrtest();
                break;


        }

    }

    //语法识别
    private void asrtest() {
// 云端语法文件
        String mCloudGrammar = "#ABNF 1.0 UTF-8;\n" +
                "                         language zh-CN;\n" +
                "                          mode voice;\n" +
                "        root $main;\n" +
                "        $main =[我] [$want]  [$ask] [$Irrelevent1] [$go] ( $opera | $location | $content) [$Irrelevent2] ;\n" +
                "        $want = 想 | 需要 | 可以;\n" +
                "        $ask = 知道 | 请问 | 问 | 了解;\n" +
                "        $Irrelevent1 = 怎么 | 在哪里 |  有哪些 |几点 ;\n"+
                "        $go = 去 | 要 | 到 | 向 | 走;\n" +
                "        $opera = 点餐服务 | 洗衣服务  | 点餐 | 洗衣 | 用餐 | 退房 | 入住;\n" +
                "        $content = 入住时间 | 退房时间 | 酒店押金 | 酒店房型 | 入住 | 退房 | 用餐时间  | 洗衣时间 | 点餐时间;\n" +
                "        $location = 电梯 |楼梯 | 餐厅 | 中餐厅 | 西餐厅 |  健身房 | 会议室 | 失物招领处 | 行李寄存处 | 附近银行 | 附近商场 | 附近机场 | 咖啡馆;\n" +
                "        $Irrelevent2 = 怎么走 | 在哪里 | 那里 | 有哪些 |是几点 | 是多少;  ";

        // 语法、词典临时变量
        String mContent;
        // 函数调用返回值
        int ret = 0;

        mEngineType = SpeechConstant.TYPE_CLOUD;

        // 初始化识别对象
        mAsr = SpeechRecognizer.createRecognizer(SpeechActivity.this, mInitListener);
        //mCloudGrammar = FucUtil.readFile(this,"grammar_sample.abnf","utf-8");

        mSharedPreferences_asr = getSharedPreferences(getPackageName(),	MODE_PRIVATE);

        // mContent = new String(mCloudGrammar);

        mAsr.setParameter(SpeechConstant.TEXT_ENCODING,"utf-8");
        ret = mAsr.buildGrammar(GRAMMAR_TYPE_ABNF, mCloudGrammar, mCloudGrammarListener);
        if(ret != ErrorCode.SUCCESS)
            showTip("语法构建失败,错误码：" + ret);


        //指定引擎类型
        mAsr.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        String grammarId = mSharedPreferences_asr.getString(KEY_GRAMMAR_ABNF_ID, null);
        mAsr.setParameter(SpeechConstant.CLOUD_GRAMMAR, grammarId);
        ret = mAsr.startListening(mRecognizerListener);
        if (ret != ErrorCode.SUCCESS) {
            showTip("识别失败,错误码: " + ret);
        }

        return;

    }

    /**
     * 云端构建语法监听器。
     */
    private GrammarListener mCloudGrammarListener = new GrammarListener() {
        @Override
        public void onBuildFinish(String grammarId, SpeechError error) {
            if(error == null){
                String grammarID = new String(grammarId);
                SharedPreferences.Editor editor =  mSharedPreferences_asr.edit();
                if(!TextUtils.isEmpty(grammarId))
                    editor.putString(KEY_GRAMMAR_ABNF_ID, grammarID);
                editor.commit();
                showTip("语法构建成功：" + grammarId);
            }else{
                showTip("语法构建失败,错误码：" + error.getErrorCode());
            }
        }
    };

    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败,错误码："+code);
            }
        }
    };


    /**
     * 识别监听器。
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            showTip("当前正在说话，音量大小：" + volume);
            Log.d(TAG, "返回音频数据："+data.length);
        }

        @Override
        public void onResult(final RecognizerResult result, boolean isLast) {
            LogUtil.d("语法识别","onResult");
            if (null != result) {
                LogUtil.i(TAG, "recognizer result：" + result.getResultString());
                // 有匹配结果时recognizer result：{"sn":1,"ls":true,"bg":0,"ed":0,"ws":[{"bg":0,"cw":[{"sc":"58","gm":"0","w":"去中心楼"},{"sc":"54","gm":"0","w":"中心楼"},{"sc":"51","gm":"0","w":"我去中心楼"}]}]}
                //没有匹配结果时 recognizer result：{"sn":1,"ls":true,"bg":0,"ed":0,"ws":[{"bg":0,"cw":[{"sc":"91","gm":"0","w":"nomatch:out-of-voca","mn":[{"id":"nomatch","name":"nomatch:out-of-voca"}]}]}]}

                String text ;
                if("cloud".equalsIgnoreCase(mEngineType)){
                    //text = JsonParser.parseGrammarResult(result.getResultString());
                    text = JsonParser.myParseGrammarResult(result.getResultString());
                    LogUtil.i(TAG, "recognizer result：" + text);
                }else {
                    text = JsonParser.parseLocalGrammarResult(result.getResultString());
                }

                // 显示
                et_input.setText(text);
                //if(text.equals("没有匹配结果."))
                //{
                    //mAsr.stopListening();
                   //showTip("停止识别");
                   // mAsr.cancel();
                    // showTip("取消识别");
                   // startVoiceNlp();  //没有匹配结果时就开始语义理解
               // } else{
                    //text = JsonParser.myParseGrammarResult(result.getResultString());
                    asrResultProcess.resultProcess(text);
               // }






            } else {
                Log.d(TAG, "recognizer result : null");
            }
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            LogUtil.d("语法识别","结束说话");
            showTip("结束说话");
        }

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            LogUtil.d("语法识别","开始说话");
            showTip("开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            LogUtil.d("语法识别","onError Code："	+ error.getErrorCode());
            showTip("onError Code："	+ error.getErrorCode());
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }

    };

    private void createAgent() {
        if (null == mAIUIAgent) {
            LogUtil.i(TAG,getString(R.string.createAIUI));
            mAIUIAgent = AIUIAgent.createAgent(this, getAIUIParams(), mAIUIListener);
        }

        if (null == mAIUIAgent) {
            final String strErrorTip = "创建AIUIAgent失败！";
            LogUtil.e(TAG,strErrorTip);
            this.et_input.setText(strErrorTip);
        } else {
            LogUtil.i(TAG,"AIUIAgent已创建");

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
            LogUtil.i(TAG,params);
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
            LogUtil.i( TAG,  "on event: " + event.eventType );

            switch (event.eventType) {
                //已连接服务器
                case AIUIConstant.EVENT_CONNECTED_TO_SERVER:
                    LogUtil.i(TAG,getString(R.string.connectedServer));
                    break;

                //已服务器断开
                case AIUIConstant.EVENT_SERVER_DISCONNECTED:
                    LogUtil.e(TAG,getString(R.string.disconnectedServer));
                    break;

                //唤醒事件
                case AIUIConstant.EVENT_WAKEUP:
                    LogUtil.i(TAG,getString(R.string.enterEventWakeUp));
                    break;

                //结果事件
                case AIUIConstant.EVENT_RESULT: {
                    LogUtil.i(TAG,getString(R.string.enterResultEvent));
                    try {
                        String text = null;
                        //data字段携带结果数据，info字段为描述数据的JSON字符串
                        JSONObject bizParamJson = new JSONObject(event.info);
                        //String info =  bizParamJson.toString();
                       // LogUtil.e( TAG, "info:"+ info );


                        JSONObject data = bizParamJson.getJSONArray("data").getJSONObject(0);
                        JSONObject params = data.getJSONObject("params");
                        JSONObject content = data.getJSONArray("content").getJSONObject(0);

                        if (content.has("cnt_id")) {
                            String cnt_id = content.getString("cnt_id");
                            String cntStr = new String(event.data.getByteArray(cnt_id), "utf-8");

                            //cntstr为结果的数据  通过cnt_id来取

                           // LogUtil.e( TAG, "cntStr:"+ cntStr );

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
                               // ret.append("该会话出错"+sid);
                                return;
                            }

                            JSONObject cntJson = new JSONObject(cntStr);

                            if (et_input.getLineCount() > 1000) {
                                et_input.setText("");
                            }

                            String sub = params.optString("sub");

                            if ("nlp".equals(sub)) {
                                // 解析得到语义结果
                                String resultStr = cntJson.optString("intent");
                                LogUtil.i( TAG, resultStr );
                                JSONObject intent1 = cntJson.getJSONObject("intent");
                                int i = intent1.length();

                                if ( i != 0) {
                                    String question = intent1.optString("text");
                                    if (intent1.has("answer")) {
                                        String answer2 = intent1.optString("answer");
                                        JSONObject answer = new JSONObject(answer2);
                                        text = answer.optString("text");
                                        //只是当无返回值时，getString(String name)抛出错误，optString(String name)返回空值

                                        // if(text.equals("语法识别"))
                                            //stopVoiceNlp();
                                            // asrtest();
                                        //自定义问答中定义了语法识别  如果是客户说了语法识别 就进入语法识别中


                                    } else {
                                        text = getString(R.string.noanswer);
                                    }
                                    et_input.append(question + ":" + text);
                                }

                                //第四次进入结果事件
                                //不判断的话  会出现无效文本  20009错误
                                if (i ==0)
                                {
                                    stopVoiceNlp();
                                    speekText(et_input.getText().toString());  //开始语音合成
                                }
                            }
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        et_input.append(e.getLocalizedMessage());
                    }

                } break;

                //错误事件
                case AIUIConstant.EVENT_ERROR: {
                    LogUtil.e(TAG,getString(R.string.enterErrorEvent));
                    et_input.append( "\n" );
                    et_input.append( "错误: " + event.arg1+"\n" + event.info );
                    //arg1字段为错误码，info字段为错误描述信息
                } break;

                //VAD事件   当arg1取值为1时，arg2为音量大小。
                case AIUIConstant.EVENT_VAD: {
                    LogUtil.i(TAG,getString(R.string.enterVADevent));
                    if (AIUIConstant.VAD_BOS == event.arg1) {
                        LogUtil.i(TAG,getString(R.string.vad_bos));
                    } else if (AIUIConstant.VAD_EOS == event.arg1) {
                        LogUtil.i(TAG,getString(R.string.vad_eos));
                    } else {
                        LogUtil.i(TAG,"" + event.arg2);//音量大小
                    }
                } break;



                case AIUIConstant.EVENT_START_RECORD: {
                    LogUtil.i(TAG,getString(R.string.START_RECORD));
                    showTip("已开始录音");
                } break;

                case AIUIConstant.EVENT_STOP_RECORD: {
                    LogUtil.i(TAG,getString(R.string.STOP_RECORD));
                    showTip("已停止录音");
                } break;

                //服务状态事件  当向AIUI发送CMD_GET_STATE命令时抛出该事件
                case AIUIConstant.EVENT_STATE: {
                    LogUtil.i(TAG,getString(R.string.enterStateEvent));

                    mAIUIState = event.arg1;

                    if (AIUIConstant.STATE_IDLE == mAIUIState) {
                        // 闲置状态，AIUI未开启
                        LogUtil.i(TAG,getString(R.string.STATE_IDLE));
                        showTip("STATE_IDLE");
                        //statue = status + 1;
                        //if(status ！= 0)
                        //    stopVoiceNlp();
                        //     startActivity(new Intent(SpeechActivity.this,MainActivity.class));
                    } else if (AIUIConstant.STATE_READY == mAIUIState) {
                        // AIUI已就绪，等待唤醒
                        LogUtil.i(TAG,getString(R.string.STATE_READY));
                        //startActivity(new Intent(SpeechActivity.this,MainActivity.class));  错误进入工作状态前必须要先进入就绪状态
                        //showTip("STATE_READY");
                    } else if (AIUIConstant.STATE_WORKING == mAIUIState) {
                        // AIUI工作中，可进行交互
                        LogUtil.i(TAG,getString(R.string.STATE_WORKING));
                       // showTip("STATE_WORKING");
                    }
                } break;

                //某条CMD命令对应的返回事件  对于除CMD_GET_STATE外的有返回的命令，都会返回该事件
                //用arg1标识对应的CMD命令，arg2为返回值
                case AIUIConstant.EVENT_CMD_RETURN: {
                    LogUtil.i(TAG,getString(R.string.enterCMD_RETURNTevent));

                } break;

                default:
                    break;
            }
        }

    };

    private void startVoiceNlp(){
        if (null == mAIUIAgent) {
            LogUtil.e( TAG, "AIUIAgent为空，请先创建" );
            showTip("AIUIAgent为空，请先创建");
            return;
        }

        LogUtil.i( TAG, "start voice nlp" );
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

    private void stopVoiceNlp(){
        if (null == mAIUIAgent) {
            showTip("AIUIAgent 为空，请先创建");
            return;
        }
        LogUtil.i(TAG,"stop voice nlp");
        // 停止录音
        String params = "sample_rate=16000,data_type=audio";
        AIUIMessage stopRecord = new AIUIMessage(AIUIConstant.CMD_STOP_RECORD, 0, 0, params, null);

        mAIUIAgent.sendMessage(stopRecord);
    }

    private void stopSpeech () {

    }



    private void speekText(String text) {
        //1. 创建 SpeechSynthesizer 对象 , 第二个参数： 本地合成时传 InitListener
        mTts = SpeechSynthesizer.createSynthesizer( this, null);
        // 云端发音人名称列表
        mCloudVoicersEntries = getResources().getStringArray(R.array.voicer_cloud_entries);
        mCloudVoicersValue = getResources().getStringArray(R.array.voicer_cloud_values);
//2.合成参数设置，详见《 MSC Reference Manual》 SpeechSynthesizer 类
//设置发音人（更多在线发音人，用户可参见 附录 13.2
        setParam();

       // mTts.setParameter(SpeechConstant. VOICE_NAME, "vixyun" ); // 设置发音人
       // mTts.setParameter(SpeechConstant. SPEED, "50" );// 设置语速
        //mTts.setParameter(SpeechConstant. VOLUME, "80" );// 设置音量，范围 0~100
        //mTts.setParameter(SpeechConstant. ENGINE_TYPE, SpeechConstant. TYPE_CLOUD); //设置云端
//设置合成音频保存位置（可自定义保存位置），保存在 “./sdcard/iflytek.pcm”
//保存在 SD 卡需要在 AndroidManifest.xml 添加写 SD 卡权限
//仅支持保存为 pcm 和 wav 格式， 如果不需要保存合成音频，注释该行代码
        //mTts.setParameter(SpeechConstant. TTS_AUDIO_PATH, "./sdcard/iflytek.pcm" );
//3.开始合成
        //String text = et_input.getText().toString();
        int code = mTts.startSpeaking( text, new MySynthesizerListener()) ;
        if (code != ErrorCode.SUCCESS) {
            LogUtil.e(TAG,"语音合成失败,错误码: " + code);
            showTip("语音合成失败,错误码: " + code);
        }


    }

    class MySynthesizerListener implements SynthesizerListener {

        @Override
        public void onSpeakBegin() {
            showTip(getString(R.string.StartPlay));
            LogUtil.i(TAG,getString(R.string.StartPlay));
        }

        @Override
        public void onSpeakPaused() {
            //showTip(" 暂停播放 ");
            LogUtil.i(TAG,getString(R.string.PausePlay));
        }

        @Override
        public void onSpeakResumed() {
            //showTip(" 继续播放 ");
            LogUtil.i(TAG,getString(R.string.ContinuePlay));
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos ,
                                     String info) {
            // 合成进度
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度
        }

        @Override
        public void onCompleted(SpeechError error) {
            if (error == null) {
                showTip(getString(R.string.PlayCompleted));
                LogUtil.i(TAG,getString(R.string.PlayCompleted));
               // mTts.stopSpeaking();
                //asrtest();  //开始命令词识别
               // startVoiceNlp();

            } else if (error != null ) {
                showTip(error.getPlainDescription( true));
            }
        }

        @Override
        public void onEvent(int eventType, int arg1 , int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话 id，当业务出错时将会话 id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话 id为null
            //if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //     String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //     Log.d(TAG, "session id =" + sid);
            //}
        }
    }


    /**
     * 参数设置
     * @return
     */
    private void setParam(){
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        // 根据合成引擎设置相应参数
        if(mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
            // 设置在线合成发音人
            mTts.setParameter(SpeechConstant.VOICE_NAME,voicer);
            //设置合成语速
            String yusu = mSharedPreferences.getString("speed_preference", "50");
            LogUtil.i("yusu",yusu);
            mTts.setParameter(SpeechConstant.SPEED,yusu );

            //mTts.setParameter(SpeechConstant.SPEED, mSharedPreferences.getString("speed_preference", "50"));
            //设置合成音调
            mTts.setParameter(SpeechConstant.PITCH, mSharedPreferences.getString("pitch_preference", "50"));
            //设置合成音量
            mTts.setParameter(SpeechConstant.VOLUME, mSharedPreferences.getString("volume_preference", "50"));
        }


        //设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, mSharedPreferences.getString("stream_preference", "3"));
        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/tts.wav");
    }

    private int selectedNum = 0;
    /**
     * 发音人选择。
     */
    private void showPresonSelectDialog() {

            // 在线合成发音人选择
                new AlertDialog.Builder(this).setTitle("在线合成发音人选项")
                        .setSingleChoiceItems(mCloudVoicersEntries, // 单选框有几项,各是什么名字
                                selectedNum, // 默认的选项
                                new DialogInterface.OnClickListener() { // 点击单选框后的处理
                                    public void onClick(DialogInterface dialog,
                                                        int which) { // 点击了哪一项
                                        voicer = mCloudVoicersValue[which];
                                        selectedNum = which;
                                        dialog.dismiss();
                                    }
                                }).show();

    }


    @Override
    protected void onPause() {
        super.onPause();
        LogUtil.i(TAG,"onPause");

        if( null != mAIUIAgent ){
            LogUtil.i(TAG,"mAIUIAgentDesrtroy");
            mAIUIAgent.destroy();
            mAIUIAgent = null;
        }

        if( null != mTts ){
            LogUtil.i(TAG,"mTtsDestory");
            mTts.stopSpeaking();
            // 暂停时释放合成连接
            mTts.destroy();
        }

        if( null != mAsr ){
            // 暂停时释放asr连接
            mAsr.cancel();//取消识别
            mAsr.destroy();
        }


    }

    private void showTip(final String str) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mToast.setText(str);
                mToast.show();
            }
        });

    }


}
