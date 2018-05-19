package me.xuxiaoxiao.chatapi.qq.entity.contact;

import java.io.Serializable;
import java.util.HashMap;

public class QQCategory implements Serializable, Cloneable {
    /**
     * 分组id
     */
    public int index;
    /**
     * 分组名称
     */
    public String name;
    /**
     * 分组内的好友
     */
    public HashMap<Long, QQUser> friends = new HashMap<>();
}
