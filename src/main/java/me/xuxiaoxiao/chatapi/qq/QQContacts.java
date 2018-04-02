package me.xuxiaoxiao.chatapi.qq;

import me.xuxiaoxiao.chatapi.qq.entity.Category;
import me.xuxiaoxiao.chatapi.qq.entity.Discuss;
import me.xuxiaoxiao.chatapi.qq.entity.Group;
import me.xuxiaoxiao.chatapi.qq.entity.User;

import java.util.ArrayList;
import java.util.HashMap;

class QQContacts {
    public final HashMap<Integer, Category> categories = new HashMap<>();
    public final HashMap<Long, User> friends = new HashMap<>();
    public final HashMap<Long, Group> groups = new HashMap<>();
    public final HashMap<Long, Discuss> discusses = new HashMap<>();
    public final ArrayList<Object> recent = new ArrayList<>();
    public User me;
}
