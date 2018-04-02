package me.xuxiaoxiao.chatapi.qq.protocol;

import java.util.ArrayList;

public class ResultGetDiscuInfo {
    public Info info;
    public ArrayList<MemberInfo> mem_info;
    public ArrayList<MemberStatus> mem_status;

    public static class Info {
        public long did;
        public String discu_name;
        public ArrayList<Member> mem_list;

        public static class Member {
            public long mem_uin;
            public long ruin;
        }
    }

    public static class MemberInfo {
        public long uin;
        public String nick;
    }

    public static class MemberStatus {
        public long uin;
        public String status;
        public int client_type;
    }
}
