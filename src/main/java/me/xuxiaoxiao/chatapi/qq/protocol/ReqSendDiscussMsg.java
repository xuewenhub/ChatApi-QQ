package me.xuxiaoxiao.chatapi.qq.protocol;

import com.google.gson.Gson;

import java.util.Random;

public class ReqSendDiscussMsg {
    public static int MSGID = new Random().nextInt() % 10000;

    public long did;
    public String content;
    public int face;
    public long clientid = 53999199;
    public long msg_id;
    public String psessionid;

    public ReqSendDiscussMsg(long discuss, String content, String psessionid) {
        this.did = discuss;
        this.content = new Gson().toJson(new Object[]{content, new ResultPoll.Font()});
        this.msg_id = MSGID++;
        this.psessionid = psessionid;
    }
}
