package me.xuxiaoxiao.chatapi.qq.entity;

import java.util.HashMap;

public class Category {
    public int index;
    public int sort;
    public String name;
    public HashMap<Long, User> friends = new HashMap<>();
}
