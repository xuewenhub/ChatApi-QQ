package me.xuxiaoxiao.chatapi.qq;

import me.xuxiaoxiao.chatapi.qq.entity.*;
import me.xuxiaoxiao.chatapi.qq.protocol.*;
import me.xuxiaoxiao.xtools.common.XTools;

import java.io.File;
import java.net.HttpCookie;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;

public class QQClient {
    public static final String LOGIN_EXCEPTION = "登陆异常";
    public static final String INIT_EXCEPTION = "初始化异常";
    public static final String LISTEN_EXCEPTION = "监听异常";

    public static final String STATUS_AWAY = "away";
    public static final String STATUS_ONLINE = "online";
    public static final String STATUS_CALLME = "callme";
    public static final String STATUS_BUSY = "busy";
    public static final String STATUS_SILENT = "silent";
    public static final String STATUS_HIDDEN = "hidden";
    public static final String STATUS_OFFLINE = "offline";

    private final QQAPI qqAPI = new QQAPI();
    private final QQThread qqThread = new QQThread();
    private final QQContacts qqContacts = new QQContacts();
    private final File folder;
    private final QQChatListener qqChatListener;
    private String checkSig;

    public QQClient(QQChatListener qqChatListener) {
        this(qqChatListener, null, null);
    }

    public QQClient(QQChatListener qqChatListener, File folder, Handler handler) {
        Objects.requireNonNull(qqChatListener);
        if (folder == null) {
            folder = new File("");
        }
        if (handler == null) {
            handler = new ConsoleHandler();
        }
        this.qqChatListener = qqChatListener;
        this.folder = folder;
        QQTools.LOGGER.setLevel(handler.getLevel());
        QQTools.LOGGER.setUseParentHandlers(false);
        QQTools.LOGGER.addHandler(handler);
    }

    public void startup() {
        qqThread.start();
    }

    public boolean isWorking() {
        return !qqThread.isInterrupted();
    }

    public void shutdown() {
        qqThread.interrupt();
    }

    public User userMe() {
        return this.qqContacts.me;
    }

    public User userFriend(Long uid) {
        return this.qqContacts.friends.get(uid);
    }

    public HashMap<Long, User> userFriends() {
        return this.qqContacts.friends;
    }

    public Discuss userDiscus(Long did) {
        return this.qqContacts.discusses.get(did);
    }

    public HashMap<Long, Discuss> userDiscuss() {
        return this.qqContacts.discusses;
    }

    public Group userGroup(Long gid) {
        return this.qqContacts.groups.get(gid);
    }

    public HashMap<Long, Group> userGroups() {
        return this.qqContacts.groups;
    }

    public HashMap<Integer, Category> userCategory() {
        return this.qqContacts.categories;
    }

    public void sendFriend(long friend, String content) {
        QQTools.LOGGER.fine(String.format("向好友 %d 发送消息：%s", friend, content));
        qqAPI.send_buddy_msg2(friend, content);
    }

    public void sendGroup(long group, String content) {
        QQTools.LOGGER.fine(String.format("向群 %d 发送消息：%s", group, content));
        qqAPI.send_qun_msg2(group, content);
    }

    public void sendDiscuss(long discuss, String content) {
        QQTools.LOGGER.fine(String.format("向讨论组 %d 发送消息：%s", discuss, content));
        qqAPI.send_discu_msg2(discuss, content);
    }

    public abstract static class QQChatListener {

        public abstract void onQRCode(File qrCode);

        public void onAvatar(String base64Avatar) {
        }

        public void onFailure(String reason) {
        }

        public void onLogin() {
        }

        public void onUserMessage(int msgId, User from, String content) {
        }

        public void onGroupMessage(int msgId, Group group, User from, String content) {
        }

        public void onDiscussMessage(int msgId, Discuss discuss, User from, String content) {
        }

        public void onLogout() {
        }
    }

    private class QQThread extends Thread {

        @Override
        public void run() {
            int loginCount = 0;
            while (!isInterrupted()) {
                //用户登录
                QQTools.LOGGER.finer(String.format("正在进行第%d次登录", loginCount));
                String loginErr = login();
                if (!XTools.strEmpty(loginErr)) {
                    qqChatListener.onFailure(loginErr);
                    return;
                }
                //用户初始化
                QQTools.LOGGER.finer("正在初始化");
                String initErr = initial();
                if (!XTools.strEmpty(initErr)) {
                    qqChatListener.onFailure(initErr);
                    return;
                }
                qqChatListener.onLogin();
                //同步消息
                QQTools.LOGGER.finer("正在监听消息");
                String listenErr = listen();
                if (!XTools.strEmpty(listenErr)) {
                    if (loginCount++ > 10) {
                        qqChatListener.onFailure(listenErr);
                        return;
                    } else {
                        continue;
                    }
                }
                //退出登录
                QQTools.LOGGER.finer("正在退出登录");
                qqChatListener.onLogout();
                return;
            }
        }

        private String login() {
            try {
                //获取pt_login_sig
                qqAPI.xlogin();
                for (HttpCookie cookie : qqAPI.httpOption.cookieManager.getCookieStore().getCookies()) {
                    if ("pt_login_sig".equals(cookie.getName())) {
                        qqAPI.pt_login_sig = cookie.getValue();
                    }
                }
                if (XTools.strEmpty(checkSig)) {
                    //获取登录二维码和qrsig
                    qqChatListener.onQRCode(qqAPI.ptqrshow(String.format("%s%sqrcode-%d-%d.jpg", folder.getAbsolutePath(), File.separator, System.currentTimeMillis(), (int) (Math.random() * 1000))));
                    for (HttpCookie cookie : qqAPI.httpOption.cookieManager.getCookieStore().getCookies()) {
                        if ("qrsig".equals(cookie.getName())) {
                            qqAPI.qrsig = cookie.getValue();
                            break;
                        }
                    }
                    //每隔两秒获取一次登录状态
                    while (true) {
                        Thread.sleep(2000);
                        RspQRLogin rspQRLogin = qqAPI.ptqrlogin();
                        switch (rspQRLogin.code) {
                            case 0:
                                QQTools.LOGGER.finer("已授权登录");
                                checkSig = rspQRLogin.uri;
                                qqAPI.check_sig(rspQRLogin.uri);
                                return null;
                            case 66:
                                QQTools.LOGGER.finer("等待操作中");
                                break;
                            case 67:
                                QQTools.LOGGER.finer("等待授权登录");
                                break;
                            default:
                                QQTools.LOGGER.finer("二维码已失效");
                                return LOGIN_EXCEPTION;
                        }
                    }
                } else {
                    qqAPI.check_sig(checkSig);
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                QQTools.LOGGER.severe(String.format("登录异常：%s\n%s", e.getMessage(), Arrays.toString(e.getStackTrace())));
                return LOGIN_EXCEPTION;
            }
        }

        private String initial() {
            try {
                //登录初始化
                qqAPI.getvfwebqq();
                qqAPI.login2();
                //获取自身信息
                BaseRsp<User> rspMe = qqAPI.get_self_info2();
                qqContacts.me = rspMe.result;
                //获取好友信息
                BaseRsp<ResultGetUserFriends> rspFriends = qqAPI.get_user_friends2();
                for (Category category : rspFriends.result.categories) {
                    qqContacts.categories.put(category.index, category);
                }
                for (ResultGetUserFriends.Friend friend : rspFriends.result.friends) {
                    User user = new User();
                    user.uin = friend.uin;
                    user.category = qqContacts.categories.get(friend.categories);
                    user.category.friends.put(user.uin, user);
                    qqContacts.friends.put(friend.uin, user);
                }
                for (ResultGetUserFriends.MarkName markName : rspFriends.result.marknames) {
                    qqContacts.friends.get(markName.uin).markname = markName.markName;
                }
                for (ResultGetUserFriends.VipInfo vipInfo : rspFriends.result.vipinfo) {
                    qqContacts.friends.get(vipInfo.u).vip_level = vipInfo.vip_level;
                }
                for (ResultGetUserFriends.Info info : rspFriends.result.info) {
                    User friend = qqContacts.friends.get(info.uin);
                    friend.face = info.face;
                    friend.nick = info.nick;
                }
                //获取群信息
                BaseRsp<ResultGetGroupNameListMask> rspGroups = qqAPI.get_group_name_list_mask2();
                for (Group group : rspGroups.result.gnamelist) {
                    qqContacts.groups.put(group.gid, group);
                }
                //获取讨论组信息
                BaseRsp<ResultGetDiscusList> rspDiscusses = qqAPI.get_discus_list();
                for (Discuss discuss : rspDiscusses.result.dnamelist) {
                    qqContacts.discusses.put(discuss.did, discuss);
                }
                //获取在线的好友信息
                BaseRsp<ResultGetOnlineBuddies> rspOnline = qqAPI.get_online_buddies2();
                for (ResultGetOnlineBuddies.OnlineBuddy onlineBuddy : rspOnline.result) {
                    if (qqContacts.friends.containsKey(onlineBuddy.uin)) {
                        qqContacts.friends.get(onlineBuddy.uin).online = true;
                    }
                }
                //获取最近联系人
                BaseRsp<ResultGetRecentList> rspRecent = qqAPI.get_recent_list2();
                for (ResultGetRecentList.Recent recent : rspRecent.result) {
                    if (recent.type == 0) {
                        qqContacts.recent.add(qqContacts.friends.get(recent.uin));
                    } else if (recent.type == 1) {
                        qqContacts.recent.add(qqContacts.groups.get(recent.uin));
                    } else if (recent.type == 2) {
                        qqContacts.recent.add(qqContacts.discusses.get(recent.uin));
                    }
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                QQTools.LOGGER.severe(String.format("初始化异常：%s\n%s", e.getMessage(), Arrays.toString(e.getStackTrace())));
                return INIT_EXCEPTION;
            }
        }

        private String listen() {
            int emptyCount = 0;
            int retryCount = 0;
            long lastEmpty = 0;
            try {
                while (!isInterrupted()) {
                    try {
                        BaseRsp<ResultPoll> rspPoll = qqAPI.poll2();
                        if (rspPoll.result != null) {
                            QQTools.LOGGER.finest(String.format("获取到消息：%s", QQTools.GSON.toJson(rspPoll)));
                            for (ResultPoll.Item item : rspPoll.result) {
                                switch (item.poll_type) {
                                    case "message": {
                                        UserMessage userMessage = (UserMessage) item.value;
                                        User user = qqContacts.friends.get(userMessage.fromUser);
                                        if (user == null || (XTools.strEmpty(user.birthday) && XTools.strEmpty(user.gender))) {
                                            BaseRsp<User> rspUser = qqAPI.get_friend_info2(userMessage.fromUser);
                                            QQTools.LOGGER.finest(String.format("获取好友信息：%s", QQTools.GSON.toJson(rspUser)));
                                            user = rspUser.result;
                                            qqContacts.friends.put(user.uin, user);
                                        }
                                        qqChatListener.onUserMessage(userMessage.msgId, user, userMessage.content);
                                        break;
                                    }
                                    case "group_message": {
                                        GroupMessage groupMessage = (GroupMessage) item.value;
                                        Group group = qqContacts.groups.get(groupMessage.fromGroup);
                                        if (group.members == null) {
                                            BaseRsp<ResultGetGroupInfo> rspGroup = qqAPI.get_group_info_ext2(group.code);
                                            QQTools.LOGGER.finest(String.format("获取群信息：%s", QQTools.GSON.toJson(rspGroup)));
                                            group.code = rspGroup.result.ginfo.code;
                                            group.name = rspGroup.result.ginfo.name;
                                            group.flag = rspGroup.result.ginfo.flag;
                                            group.face = rspGroup.result.ginfo.face;
                                            group.level = rspGroup.result.ginfo.level;
                                            group.owner = rspGroup.result.ginfo.owner;
                                            group.memo = rspGroup.result.ginfo.memo;
                                            group.fingermemo = rspGroup.result.ginfo.fingermemo;
                                            group.createtime = rspGroup.result.ginfo.createtime;
                                            group.option = rspGroup.result.ginfo.option;
                                            group.members = new HashMap<>();
                                            for (ResultGetGroupInfo.GInfo.Member member : rspGroup.result.ginfo.members) {
                                                User user = new User();
                                                user.uin = member.muin;
                                                group.members.put(user.uin, user);
                                            }
                                            if (rspGroup.result.minfo != null) {
                                                for (ResultGetGroupInfo.MInfo mInfo : rspGroup.result.minfo) {
                                                    User user = group.members.get(mInfo.uin);
                                                    user.nick = mInfo.nick;
                                                    user.gender = mInfo.gender;
                                                    user.country = mInfo.country;
                                                    user.province = mInfo.province;
                                                    user.city = mInfo.city;
                                                }
                                            }
                                            if (rspGroup.result.stats != null) {
                                                for (ResultGetGroupInfo.Stat stat : rspGroup.result.stats) {
                                                    group.members.get(stat.uin).stat = stat.stat;
                                                }
                                            }
                                            if (rspGroup.result.cards != null) {
                                                for (ResultGetGroupInfo.Card card : rspGroup.result.cards) {
                                                    group.members.get(card.muin).card = card.card;
                                                }
                                            }
                                            if (rspGroup.result.vipinfo != null) {
                                                for (ResultGetGroupInfo.VipInfo vipInfo : rspGroup.result.vipinfo) {
                                                    group.members.get(vipInfo.u).vip_level = vipInfo.vip_level;
                                                }
                                            }
                                        }
                                        qqChatListener.onGroupMessage(groupMessage.msgId, group, group.members.get(groupMessage.fromUser), groupMessage.content);
                                        break;
                                    }
                                    case "discu_message": {
                                        DiscussMessage discussMessage = (DiscussMessage) item.value;
                                        Discuss discuss = qqContacts.discusses.get(discussMessage.fromDiscuss);
                                        if (discuss.members == null) {
                                            discuss.members = new HashMap<>();
                                            BaseRsp<ResultGetDiscuInfo> rspDiscuss = qqAPI.get_discu_info(discuss.did);
                                            QQTools.LOGGER.finest(String.format("获取讨论组信息：%s", QQTools.GSON.toJson(rspDiscuss)));
                                            discuss.name = rspDiscuss.result.info.discu_name;
                                            for (ResultGetDiscuInfo.Info.Member member : rspDiscuss.result.info.mem_list) {
                                                User user = new User();
                                                user.uin = member.mem_uin;
                                                user.account = member.ruin;
                                                discuss.members.put(user.uin, user);
                                            }
                                            for (ResultGetDiscuInfo.MemberInfo memberInfo : rspDiscuss.result.mem_info) {
                                                discuss.members.get(memberInfo.uin).nick = memberInfo.nick;
                                            }
                                        }
                                        qqChatListener.onDiscussMessage(discussMessage.msgId, discuss, discuss.members.get(discussMessage.fromUser), discussMessage.content);
                                        break;
                                    }
                                    default:
                                        QQTools.LOGGER.warning(String.format("获取到未知类型的消息：%s", QQTools.GSON.toJson(item)));
                                        break;
                                }
                            }
                            emptyCount = 0;
                        } else {
                            if (System.currentTimeMillis() - lastEmpty < 10000) {
                                emptyCount++;
                            }
                            if (emptyCount > 30) {
                                QQTools.LOGGER.severe("连接已经失效");
                                return LISTEN_EXCEPTION;
                            } else {
                                QQTools.LOGGER.finer("暂无信息");
                                lastEmpty = System.currentTimeMillis();
                            }
                        }
                        retryCount = 0;
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (retryCount++ < 5) {
                            QQTools.LOGGER.warning(String.format("监听失败，重试第%d次", retryCount));
                        } else {
                            QQTools.LOGGER.severe("监听失败，重试次数过多");
                            return LISTEN_EXCEPTION;
                        }
                    }
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                QQTools.LOGGER.severe(String.format("监听异常：%s\n%s", e.getMessage(), Arrays.toString(e.getStackTrace())));
                return LISTEN_EXCEPTION;
            }
        }
    }
}
