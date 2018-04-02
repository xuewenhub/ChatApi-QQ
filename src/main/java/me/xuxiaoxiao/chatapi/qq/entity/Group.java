package me.xuxiaoxiao.chatapi.qq.entity;

import java.util.HashMap;

public class Group {
    public long gid;
    public long code;
    public int face;
    public String name;
    public int level;
    public long flag;
    public long owner;
    public String memo;
    public String fingermemo;
    public long createtime;
    public int option;
    public HashMap<Long, User> members;
}
