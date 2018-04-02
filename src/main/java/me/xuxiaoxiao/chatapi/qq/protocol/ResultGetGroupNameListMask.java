package me.xuxiaoxiao.chatapi.qq.protocol;

import me.xuxiaoxiao.chatapi.qq.entity.Group;

import java.util.ArrayList;

public class ResultGetGroupNameListMask {
    public ArrayList<GMask> gmasklist;
    public ArrayList<Group> gnamelist;
    public ArrayList<GMark> gmarklist;

    public static class GMask {
        public long gid;
        public int mask;
    }

    public static class GMark {
        public long uin;
        public String markname;
    }
}
