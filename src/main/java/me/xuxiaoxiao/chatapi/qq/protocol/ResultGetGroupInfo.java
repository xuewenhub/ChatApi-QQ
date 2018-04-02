package me.xuxiaoxiao.chatapi.qq.protocol;

import java.util.ArrayList;

public class ResultGetGroupInfo {
    public GInfo ginfo;
    public ArrayList<Stat> stats;
    public ArrayList<MInfo> minfo;
    public ArrayList<Card> cards;
    public ArrayList<VipInfo> vipinfo;

    public static class Stat {
        public int client_type;
        public long uin;
        public int stat;
    }

    public static class MInfo {
        public long uin;
        public String nick;
        public String gender;
        public String country;
        public String province;
        public String city;
    }

    public static class GInfo {
        public int face;
        public String memo;
        public String fingermemo;
        public long code;
        public long createtime;
        public long flag;
        public int level;
        public String name;
        public long gid;
        public long owner;
        public ArrayList<Member> members;
        public int option;

        public static class Member {
            public long muin;
            public int mflag;
        }
    }

    public static class Card {
        public long muin;
        public String card;
    }

    public static class VipInfo {
        public int vip_level;
        public long u;
        public int is_vip;
    }
}
