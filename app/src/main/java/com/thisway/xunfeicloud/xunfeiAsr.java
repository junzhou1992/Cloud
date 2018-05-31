package com.thisway.xunfeicloud;

import org.litepal.crud.DataSupport;

/**
 * Created by jun on 2018/5/8.
 */

public class xunfeiAsr extends DataSupport {

    private  long id;



    private long keyID;
    private String key;
    private String answer;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getKeyID() {
        return keyID;
    }

    public void setKeyID(long keyID) {
        this.keyID = keyID;
    }


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }


}
