package me.xuxiaoxiao.chatapi.qq.entity;

import me.xuxiaoxiao.chatapi.qq.protocol.ResultPoll;

public class GroupMessage {
    public int msgId;
    public int msgType;
    public long fromGroup;
    public long fromUser;
    public long toUser;
    public String content;
    public ResultPoll.Font font;
    public long time;
}
