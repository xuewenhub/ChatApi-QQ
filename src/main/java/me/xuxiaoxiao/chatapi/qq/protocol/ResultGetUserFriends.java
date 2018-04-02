package me.xuxiaoxiao.chatapi.qq.protocol;

import me.xuxiaoxiao.chatapi.qq.entity.Category;

import java.util.ArrayList;

public class ResultGetUserFriends {
    public ArrayList<Friend> friends;
    public ArrayList<MarkName> marknames;
    public ArrayList<Category> categories;
    public ArrayList<VipInfo> vipinfo;
    public ArrayList<Info> info;

    public static class Friend {
        public int flag;
        public long uin;
        public int categories;
    }

    public static class MarkName {
        public long uin;
        public String markName;
        public int type;
    }

    public static class VipInfo {
        public long u;
        public int is_vip;
        public int vip_level;
    }

    public static class Info {
        public int face;
        public long flag;
        public String nick;
        public long uin;
    }
}
