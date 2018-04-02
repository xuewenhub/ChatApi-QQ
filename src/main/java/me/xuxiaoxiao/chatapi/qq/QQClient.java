package me.xuxiaoxiao.chatapi.qq;

import me.xuxiaoxiao.chatapi.qq.entity.Category;
import me.xuxiaoxiao.chatapi.qq.entity.Discuss;
import me.xuxiaoxiao.chatapi.qq.entity.Group;
import me.xuxiaoxiao.chatapi.qq.entity.User;
import me.xuxiaoxiao.chatapi.qq.protocol.*;
import me.xuxiaoxiao.xtools.common.XTools;

import java.io.File;
import java.net.HttpCookie;
import java.util.Arrays;
import java.util.HashMap;
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
    private final QQChatListener qqChatListener;
    private final File folder;

    public QQClient(QQChatListener qqChatListener, File folder, Handler handler) {
        this.qqChatListener = qqChatListener;
        this.folder = folder == null ? new File("") : folder;
        if (handler != null) {
            QQTools.LOGGER.setLevel(handler.getLevel());
            QQTools.LOGGER.setUseParentHandlers(false);
            QQTools.LOGGER.addHandler(handler);
        }
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

    public void sendFriend(long friend, String content) {
        QQTools.LOGGER.finer(String.format("向好友 %d 发送消息：%s", friend, content));
        qqAPI.send_buddy_msg2(friend, content);
    }

    public void sendGroup(long group, String content) {
        QQTools.LOGGER.finer(String.format("向群 %d 发送消息：%s", group, content));
        qqAPI.send_buddy_msg2(group, content);
    }

    public void sendDiscuss(long discuss, String content) {
        QQTools.LOGGER.finer(String.format("向讨论组 %d 发送消息：%s", discuss, content));
        qqAPI.send_discu_msg2(discuss, content);
    }

    public interface QQChatListener {
        void onQRCode(File qrCode);

        void onAvatar(String base64Avatar);

        void onFailure(String reason);

        void onLogin();

        void onUserMessage(int msgId, User from, String content);

        void onGroupMessage(int msgId, Group group, User from, String content);

        void onDiscussMessage(int msgId, Discuss discuss, User from, String content);

        void onLogout();
    }

    private class QQThread extends Thread {

        @Override
        public void run() {
            //用户登录
            QQTools.LOGGER.fine("正在登录");
            String loginErr = login();
            if (!XTools.strEmpty(loginErr)) {
                QQTools.LOGGER.severe(String.format("登录出现错误：%s", loginErr));
                qqChatListener.onFailure(loginErr);
                return;
            }
            //用户初始化
            QQTools.LOGGER.fine("正在初始化");
            String initErr = initial();
            if (!XTools.strEmpty(initErr)) {
                QQTools.LOGGER.severe(String.format("初始化出现错误：%s", initErr));
                qqChatListener.onFailure(initErr);
                return;
            }
            qqChatListener.onLogin();
            //同步消息
            QQTools.LOGGER.fine("正在监听消息");
            String listenErr = listen();
            if (!XTools.strEmpty(listenErr)) {
                QQTools.LOGGER.severe(String.format("监听消息出现错误：%s", listenErr));
                qqChatListener.onFailure(listenErr);
                return;
            }
            qqChatListener.onLogout();
        }

        private String login() {
            try {
                //获取pt_login_sig
                qqAPI.xlogin();
                for (HttpCookie cookie : QQTools.HTTPOPTION.cookieManager.getCookieStore().getCookies()) {
                    if ("pt_login_sig".equals(cookie.getName())) {
                        qqAPI.pt_login_sig = cookie.getValue();
                    }
                }
                //获取登录二维码
                String qrCode = String.format("%s%sqrcode-%d-%d.jpg", folder.getAbsolutePath(), File.separator, System.currentTimeMillis(), (int) (Math.random() * 1000));
                QQTools.LOGGER.finer(String.format("等待扫描二维码：%s", qrCode));
                qqChatListener.onQRCode(qqAPI.ptqrshow(qrCode));
                for (HttpCookie cookie : QQTools.HTTPOPTION.cookieManager.getCookieStore().getCookies()) {
                    if ("qrsig".equals(cookie.getName())) {
                        qqAPI.qrsig = cookie.getValue();
                        break;
                    }
                }
                while (true) {
                    Thread.sleep(2000);
                    RspQRLogin rspQRLogin = qqAPI.ptqrlogin();
                    switch (rspQRLogin.code) {
                        case 0:
                            QQTools.LOGGER.finer("已授权登录");
                            qqAPI.check_sig(rspQRLogin.uri);
                            return null;
                        case 66:
                            QQTools.LOGGER.finer("已扫描二维码");
                            break;
                        case 67:
                            QQTools.LOGGER.finer("等待授权登录");
                            break;
                        default:
                            QQTools.LOGGER.finer("登录超时");
                            return LOGIN_EXCEPTION;
                    }
                }
            } catch (Exception e) {
                QQTools.LOGGER.warning(String.format("登录异常：%s", e.getMessage()));
                e.printStackTrace();
                return LOGIN_EXCEPTION + Arrays.toString(e.getStackTrace());
            }
        }

        private String initial() {
            try {
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
                BaseRsp<ResultGetOnlineBuddies> rspOnline = qqAPI.get_online_buddies2();
                for (ResultGetOnlineBuddies.OnlineBuddy onlineBuddy : rspOnline.result) {
                    if (qqContacts.friends.containsKey(onlineBuddy.uin)) {
                        qqContacts.friends.get(onlineBuddy.uin).online = true;
                    }
                }
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
                return INIT_EXCEPTION;
            }
        }

        private String listen() {
            try {
                int empty = 0;
                while (!isInterrupted()) {
                    BaseRsp<ResultPoll> rspPoll = qqAPI.poll2();
                    if (rspPoll.result != null) {
                        empty = 0;
                        QQTools.LOGGER.finer("获取到消息");
                        for (ResultPoll.Item item : rspPoll.result) {
                            switch (item.poll_type) {
                                case "message": {
                                    ResultPoll.UserMessage userMessage = (ResultPoll.UserMessage) item.value;
                                    User user = qqContacts.friends.get(userMessage.fromUser);
                                    if (user == null || (XTools.strEmpty(user.birthday) && XTools.strEmpty(user.gender))) {
                                        user = qqAPI.get_friend_info2(userMessage.fromUser).result;
                                        qqContacts.friends.put(user.uin, user);
                                    }
                                    qqChatListener.onUserMessage(userMessage.msgId, user, userMessage.content);
                                    break;
                                }
                                case "group_message": {
                                    ResultPoll.GroupMessage groupMessage = (ResultPoll.GroupMessage) item.value;
                                    Group group = qqContacts.groups.get(groupMessage.fromGroup);
                                    if (group.members == null) {
                                        BaseRsp<ResultGetGroupInfo> rspGroup = qqAPI.get_group_info_ext2(group.code);
                                        QQTools.LOGGER.finer("获取群信息：" + QQTools.GSON.toJson(rspGroup));
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
                                    ResultPoll.DiscussMessage discussMessage = (ResultPoll.DiscussMessage) item.value;
                                    Discuss discuss = qqContacts.discusses.get(discussMessage.fromDiscuss);
                                    if (discuss.members == null) {
                                        discuss.members = new HashMap<>();
                                        BaseRsp<ResultGetDiscuInfo> rspDiscuss = qqAPI.get_discu_info(discuss.did);
                                        QQTools.LOGGER.finer("获取群信息：" + QQTools.GSON.toJson(rspDiscuss));
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
                            }
                        }
                    } else {
                        QQTools.LOGGER.finer("轮空：" + (empty++));
                        if (empty > 10000) {
                            QQTools.LOGGER.warning("轮空超过3000次，停止");
                            return LISTEN_EXCEPTION;
                        }
                    }
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return LISTEN_EXCEPTION;
            }
        }
    }
}
