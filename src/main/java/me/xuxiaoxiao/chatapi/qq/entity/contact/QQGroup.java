package me.xuxiaoxiao.chatapi.qq.entity.contact;

import java.io.Serializable;
import java.util.HashMap;

public class QQGroup extends QQContact implements Serializable, Cloneable {
    /**
     * 群code，获取群信息时用到
     */
    public long code;
    /**
     * 群主id
     */
    public long owner;
    /**
     * 群备注名称，手机qq上没有这个功能，桌面qq有
     */
    public String remark;
    /**
     * 群公告
     */
    public String notice;
    /**
     * 群创建时间
     */
    public long createtime;
    /**
     * 群内所有成员
     */
    public HashMap<Long, Member> members;

    public static class Member {
        /**
         * 群成员id
         */
        public long id;
        /**
         * 群成员名称
         */
        public String name;
        /**
         * 群成员名片
         */
        public String display;
        /**
         * 群成员所在国家
         */
        public String country;
        /**
         * 群成员所在省份
         */
        public String province;
        /**
         * 群成员所在城市
         */
        public String city;
        /**
         * 群成员性别
         */
        public int gender;
        /**
         * 群成员vip等级
         */
        public int vipLevel;
    }
}
