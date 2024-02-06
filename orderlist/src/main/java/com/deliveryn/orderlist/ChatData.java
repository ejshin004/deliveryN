package com.deliveryn.orderlist;

import java.io.Serializable;

public class ChatData{
    private String msg;
    private String nickname;
    private Object timeStamp;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Object getTime() { return timeStamp;}

    public void setTime(Object timeStamp) { this.timeStamp = timeStamp; }


}