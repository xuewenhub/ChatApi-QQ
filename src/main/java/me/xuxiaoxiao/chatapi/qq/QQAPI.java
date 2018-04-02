package me.xuxiaoxiao.chatapi.qq;

import com.google.gson.reflect.TypeToken;
import me.xuxiaoxiao.chatapi.qq.entity.User;
import me.xuxiaoxiao.chatapi.qq.protocol.*;
import me.xuxiaoxiao.xtools.common.XTools;
import me.xuxiaoxiao.xtools.common.http.XRequest;

import java.io.File;

class QQAPI {
    public final long initTime = System.currentTimeMillis();

    public String pt_login_sig;
    public String qrsig;
    public String vfwebqq;
    public String qqStr;
    public String psessionid;

    public void xlogin() {
        XRequest request = XRequest.GET("https://xui.ptlogin2.qq.com/cgi-bin/xlogin");
        request.query("appid", 501004106);
        request.query("daid", 164);
        request.query("enable_qlogin", 0);
        request.query("f_url", "loginerroralert");
        request.query("login_state", 10);
        request.query("mibao_css", "m_webqq");
        request.query("no_verifyimg", 1);
        request.query("pt_disable_pwd", 1);
        request.query("s_url", "http://web2.qq.com/proxy.html");
        request.query("strong_login", 1);
        request.query("style", 40);
        request.query("t", 20131024001L);
        request.query("target", "self");
        request.header("Host", "xui.ptlogin2.qq.com");
        request.header("Referer", "http://web2.qq.com/");
        XTools.http(QQTools.HTTPOPTION, request);
    }

    public File ptQRShow(String path) {
        XRequest request = XRequest.GET("https://ssl.ptlogin2.qq.com/ptqrshow");
        request.query("appid", 501004106);
        request.query("d", 72);
        request.query("daid", 164);
        request.query("e", 2);
        request.query("l", "M");
        request.query("pt_3rd_aid", 0);
        request.query("s", 3);
        request.query("t", Math.random());
        request.query("v", 4);
        request.header("Host", "ssl.ptlogin2.qq.com");
        request.header("Referer", "https://xui.ptlogin2.qq.com/cgi-bin/xlogin?daid=164&target=self&style=40&pt_disable_pwd=1&mibao_css=m_webqq&appid=501004106&enable_qlogin=0&no_verifyimg=1&s_url=http://web2.qq.com/proxy.html&f_url=loginerroralert&strong_login=1&login_state=10&t=20131024001");
        return XTools.http(QQTools.HTTPOPTION, request).file(path);
    }

    public RspQRLogin ptQRLogin() {
        XRequest request = XRequest.GET("https://ssl.ptlogin2.qq.com/ptqrlogin");
        request.query("action", String.format("0-0-%d", System.currentTimeMillis() - initTime));
        request.query("aid", 501004106);
        request.query("daid", 164);
        request.query("from_ui", 1);
        request.query("g", 1);
        request.query("h", 1);
        request.query("js_type", 1);
        request.query("js_ver", 10230);
        request.query("login_sig", pt_login_sig);
        request.query("mibao_css", "m_webqq");
        request.query("pt_uistyle", 40);
        request.query("ptlang", 2052);
        request.query("ptqrtoken", QQTools.hashQRSig(qrsig));
        request.query("ptredirect", 0);
        request.query("t", 1);
        request.query("u1", "https://w.qq.com/proxy.html");
        return new RspQRLogin(QQTools.request(request, "https://xui.ptlogin2.qq.com/cgi-bin/xlogin?daid=164&target=self&style=40&mibao_css=m_webqq&appid=501004106&enable_qlogin=0&no_verifyimg=1&s_url=https%3A%2F%2Fw.qq.com%2Fproxy.html&f_url=loginerroralert&strong_login=1&login_state=10&t=20131024001"));
    }

    public void checkSig(String uri) {
        XRequest request = XRequest.GET(uri);
        QQTools.request(request, "https://xui.ptlogin2.qq.com/cgi-bin/xlogin?daid=164&target=self&style=40&mibao_css=m_webqq&appid=501004106&enable_qlogin=0&no_verifyimg=1&s_url=https%3A%2F%2Fw.qq.com%2Fproxy.html&f_url=loginerroralert&strong_login=1&login_state=10&t=20131024001");
    }

    public void getVFWebqq() {
        XRequest request = XRequest.GET("https://s.web2.qq.com/api/getvfwebqq");
        request.query("clientid", 53999199);
        request.query("psessionid", "");
        request.query("ptwebqq", "");
        request.query("t", System.currentTimeMillis());
        BaseRsp<ResultVFWebqq> resultVFWebqq = QQTools.GSON.fromJson(QQTools.request(request, "https://s.web2.qq.com/proxy.html?v=20130916001&callback=1&id=1"), new TypeToken<BaseRsp<ResultVFWebqq>>() {
        }.getType());
        this.vfwebqq = resultVFWebqq.result.vfwebqq;
    }

    public BaseRsp<ResultLogin> login() {
        XRequest request = XRequest.POST("https://d1.web2.qq.com/channel/login2");
        request.content("r", QQTools.GSON.toJson(new BaseReq(null, "", vfwebqq, "")));
        BaseRsp<ResultLogin> resultLogin = QQTools.GSON.fromJson(QQTools.request(request, "https://d1.web2.qq.com/proxy.html?v=20151105001&callback=1&id=2"), new TypeToken<BaseRsp<ResultLogin>>() {
        }.getType());
        this.qqStr = String.valueOf(resultLogin.result.uin);
        this.psessionid = resultLogin.result.psessionid;
        return resultLogin;
    }

    public BaseRsp<ResultGetUserFriends> getUserFriends() {
        XRequest request = XRequest.POST("https://s.web2.qq.com/api/get_user_friends2");
        request.content("r", QQTools.GSON.toJson(new BaseReq(QQTools.hash(qqStr, ""), null, vfwebqq, null)));
        return QQTools.GSON.fromJson(QQTools.request(request, "https://s.web2.qq.com/proxy.html?v=20130916001&callback=1&id=1"), new TypeToken<BaseRsp<ResultGetUserFriends>>() {
        }.getType());
    }

    public BaseRsp<ResultGetGroupNameListMask> getGroupNameListMask() {
        XRequest request = XRequest.POST("https://s.web2.qq.com/api/get_group_name_list_mask2");
        request.content("r", QQTools.GSON.toJson(new BaseReq(QQTools.hash(qqStr, ""), null, vfwebqq, null)));
        return QQTools.GSON.fromJson(QQTools.request(request, "https://s.web2.qq.com/proxy.html?v=20130916001&callback=1&id=1"), new TypeToken<BaseRsp<ResultGetGroupNameListMask>>() {
        }.getType());
    }

    public BaseRsp<ResultGetDiscusList> getDiscusList() {
        XRequest request = XRequest.GET("https://s.web2.qq.com/api/get_discus_list");
        request.query("clientid", 53999199);
        request.query("psessionid", psessionid);
        request.query("t", System.currentTimeMillis());
        request.query("vfwebqq", vfwebqq);
        return QQTools.GSON.fromJson(QQTools.request(request, "https://s.web2.qq.com/proxy.html?v=20130916001&callback=1&id=1"), new TypeToken<BaseRsp<ResultGetDiscusList>>() {
        }.getType());
    }

    public BaseRsp<User> getSelfInfo() {
        XRequest request = XRequest.GET("https://s.web2.qq.com/api/get_self_info2");
        request.query("t", System.currentTimeMillis());
        return QQTools.GSON.fromJson(QQTools.request(request, "https://s.web2.qq.com/proxy.html?v=20130916001&callback=1&id=1"), new TypeToken<BaseRsp<User>>() {
        }.getType());
    }

    public BaseRsp<ResultGetOnlineBuddies> getOnlineBuddies() {
        XRequest request = XRequest.GET("https://d1.web2.qq.com/channel/get_online_buddies2");
        request.query("clientid", 53999199);
        request.query("psessionid", psessionid);
        request.query("t", System.currentTimeMillis());
        request.query("vfwebqq", vfwebqq);
        return QQTools.GSON.fromJson(QQTools.request(request, "https://d1.web2.qq.com/proxy.html?v=20151105001&callback=1&id=2"), new TypeToken<BaseRsp<ResultGetOnlineBuddies>>() {
        }.getType());
    }

    public BaseRsp<ResultGetRecentList> getRecentList() {
        XRequest request = XRequest.POST("https://d1.web2.qq.com/channel/get_recent_list2");
        request.content("r", QQTools.GSON.toJson(new BaseReq(null, null, vfwebqq, psessionid)));
        return QQTools.GSON.fromJson(QQTools.request(request, "https://d1.web2.qq.com/proxy.html?v=20151105001&callback=1&id=2"), new TypeToken<BaseRsp<ResultGetRecentList>>() {
        }.getType());
    }

    public BaseRsp<ResultPoll> poll() {
        XRequest request = XRequest.POST("https://d1.web2.qq.com/channel/poll2");
        request.content("r", QQTools.GSON.toJson(new BaseReq(null, "", null, psessionid)));
        return QQTools.GSON.fromJson(QQTools.request(request, "https://d1.web2.qq.com/proxy.html?v=20151105001&callback=1&id=2"), new TypeToken<BaseRsp<ResultPoll>>() {
        }.getType());
    }

    public BaseRsp<User> getFriendInfo(long uin) {
        XRequest request = XRequest.GET("https://s.web2.qq.com/api/get_friend_info2");
        request.query("clientid", 53999199);
        request.query("psessionid", psessionid);
        request.query("t", System.currentTimeMillis());
        request.query("tuin", uin);
        request.query("vfwebqq", vfwebqq);
        return QQTools.GSON.fromJson(QQTools.request(request, "https://s.web2.qq.com/proxy.html?v=20130916001&callback=1&id=1"), new TypeToken<BaseRsp<User>>() {
        }.getType());
    }

    public BaseRsp<ResultLongNick> getLongNick(long uin) {
        XRequest request = XRequest.GET("https://s.web2.qq.com/api/get_single_long_nick2");
        request.query("t", System.currentTimeMillis());
        request.query("tuin", uin);
        request.query("vfwebqq", vfwebqq);
        return QQTools.GSON.fromJson(QQTools.request(request, "https://s.web2.qq.com/proxy.html?v=20130916001&callback=1&id=1"), new TypeToken<BaseRsp<ResultLongNick>>() {
        }.getType());
    }

    public BaseRsp<ResultGetGroupInfo> getGroupInfo(long gcode) {
        XRequest request = XRequest.GET("https://s.web2.qq.com/api/get_group_info_ext2");
        request.query("gcode", gcode);
        request.query("t", System.currentTimeMillis());
        request.query("vfwebqq", vfwebqq);
        return QQTools.GSON.fromJson(QQTools.request(request, "https://s.web2.qq.com/proxy.html?v=20130916001&callback=1&id=1"), new TypeToken<BaseRsp<ResultGetGroupInfo>>() {
        }.getType());
    }

    public BaseRsp<ResultGetDiscuInfo> getDiscuInfo(long did) {
        XRequest request = XRequest.GET("https://d1.web2.qq.com/channel/get_discu_info?did=1338668515&vfwebqq=10c1ff9b2be942539e02e4b382f525e2c8869237c1421a19e7907157bb9cd7ce9911ef2a7b2dcbd8&clientid=53999199&psessionid=8368046764001d636f6e6e7365727665725f77656271714031302e3133332e34312e383400001ad00000066b026e040015808a206d0000000a406172314338344a69526d0000002859185d94e66218548d1ecb1a12513c86126b3afb97a3c2955b1070324790733ddb059ab166de6857&t=1506085861398");
        request.query("clientid", 53999199);
        request.query("did", did);
        request.query("psessionid", psessionid);
        request.query("t", System.currentTimeMillis());
        request.query("vfwebqq", vfwebqq);
        return QQTools.GSON.fromJson(QQTools.request(request, "https://d1.web2.qq.com/proxy.html?v=20151105001&callback=1&id=2"), new TypeToken<BaseRsp<ResultGetDiscuInfo>>() {
        }.getType());
    }

    public BaseRsp sendGroupMsg(long group, String content) {
        XRequest request = XRequest.POST("https://d1.web2.qq.com/channel/send_qun_msg2");
        request.content("r", new ReqSendGroupMsg(group, content, psessionid));
        return QQTools.GSON.fromJson(QQTools.request(request, "https://d1.web2.qq.com/cfproxy.html?v=20151105001&callback=1"), BaseRsp.class);
    }

    public BaseRsp sendDiscussMsg(long discuss, String content) {
        XRequest request = XRequest.POST("https://d1.web2.qq.com/channel/send_discu_msg2");
        request.content("r", new ReqSendDiscussMsg(discuss, content, psessionid));
        return QQTools.GSON.fromJson(QQTools.request(request, "https://d1.web2.qq.com/cfproxy.html?v=20151105001&callback=1"), BaseRsp.class);
    }

    public BaseRsp sendFriendMsg(long friend, String content) {
        XRequest request = XRequest.POST("https://d1.web2.qq.com/channel/send_buddy_msg2");
        request.content("r", new ReqSendFriendMsg(friend, content, psessionid));
        return QQTools.GSON.fromJson(QQTools.request(request, "https://d1.web2.qq.com/cfproxy.html?v=20151105001&callback=1"), BaseRsp.class);
    }

    public BaseRsp changeStatus(String status) {
        XRequest request = XRequest.GET("https://d1.web2.qq.com/channel/change_status2");
        request.query("clientid", 53999199);
        request.query("newstatus", status);
        request.query("psessionid", psessionid);
        request.query("t", System.currentTimeMillis());
        return QQTools.GSON.fromJson(QQTools.request(request, "https://d1.web2.qq.com/proxy.html?v=20151105001&callback=1&id=2"), BaseRsp.class);
    }
}