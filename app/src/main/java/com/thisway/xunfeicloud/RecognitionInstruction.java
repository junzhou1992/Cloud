package com.thisway.xunfeicloud;

import org.litepal.crud.DataSupport;

/**
 * Created by jun on 2018/4/10.
 */

public class RecognitionInstruction extends DataSupport {

    private int id;
    private String instruction;
    private String answer;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }



}
