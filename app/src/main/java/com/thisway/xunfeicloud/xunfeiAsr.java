package com.thisway.xunfeicloud;

import org.litepal.crud.DataSupport;

/**
 * Created by jun on 2018/5/8.
 */

public class xunfeiAsr extends DataSupport {

    private  long id;

    private long instuctionID;
    private String key;
    private String answer;

    public long getInstuctionID() {
        return instuctionID;
    }

    public void setInstuctionID(long instuctionID) {
        this.instuctionID = instuctionID;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
