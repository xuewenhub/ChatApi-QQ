package me.xuxiaoxiao.chatapi.qq.entity.contact;

import java.io.Serializable;

public class QQUser extends QQContact implements Serializable, Cloneable {
    public static final int GENDER_UNKNOWN = 0;
    public static final int GENDER_MALE = 1;
    public static final int GENDER_FEMALE = 2;
    /**
     * 用户的备注名
     */
    public String remark;
    /**
     * 用户个性签名
     */
    public String signature;
    /**
     * 用户所在国家
     */
    public String country;
    /**
     * 用户所在省份
     */
    public String province;
    /**
     * 用户所在城市
     */
    public String city;
    /**
     * 用户性别
     */
    public int gender;
    /**
     * 用户生日
     */
    public String birthday;
    /**
     * 用户在线状态
     */
    public String status;
    /**
     * 用户超级会员等级
     */
    public int vipLevel;
    /**
     * 是否是详细信息，如果不是详细信息，则说明是获取好友列表时的数据。
     * 接收到这个用户发来的消息后会再次获取该用户详细数据
     */
    public boolean isDetail;
}
