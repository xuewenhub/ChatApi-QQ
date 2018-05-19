package me.xuxiaoxiao.chatapi.qq.entity.message;

import me.xuxiaoxiao.chatapi.qq.entity.contact.QQDiscuss;
import me.xuxiaoxiao.chatapi.qq.entity.contact.QQGroup;
import me.xuxiaoxiao.chatapi.qq.entity.contact.QQUser;

import java.io.Serializable;

public class QQMessage implements Serializable, Cloneable {
    /**
     * 消息id
     */
    public long id;
    /**
     * 如果是群消息则为消息来源的群，否则为null
     */
    public QQGroup fromGroup;
    /**
     * 如果是群消息则为消息来源的群成员，否则为null
     */
    public QQGroup.Member fromGroupMember;
    /**
     * 如果是讨论组消息则为来源的讨论组，否则为null
     */
    public QQDiscuss fromDiscuss;
    /**
     * 如果是讨论组消息则为消息来源的讨论组成员，否则为null
     */
    public QQDiscuss.Member fromDiscussMember;
    /**
     * 如果是好友消息则为消息来源的好友，否则为null
     */
    public QQUser fromUser;
    /**
     * 消息的接收方，如果是我发给好友的消息，则为好友，否则为自己
     */
    public QQUser toUser;
    /**
     * 消息的创建时间
     */
    public long timestamp;
    /**
     * 消息的内容，多个消息块用空格连接
     */
    public String content;
}
