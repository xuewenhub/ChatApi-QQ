package me.xuxiaoxiao.chatapi.qq.protocol;

public class BaseRsp<T> {
    public int retcode;
    public T result;
    public String errmsg;
}
