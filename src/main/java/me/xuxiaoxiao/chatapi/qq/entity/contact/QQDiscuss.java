package me.xuxiaoxiao.chatapi.qq.entity.contact;

import java.io.Serializable;
import java.util.HashMap;

public class QQDiscuss extends QQContact implements Serializable, Cloneable {
    /**
     * 讨论组的所有成员
     */
    public HashMap<Long, Member> members;

    public static class Member {
        /**
         * 讨论组成员id
         */
        public long id;
        /**
         * 讨论组成员名称
         */
        public String name;
    }
}
