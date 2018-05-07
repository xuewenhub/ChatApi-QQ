package me.xuxiaoxiao.chatapi.qq;

import com.google.gson.reflect.TypeToken;
import me.xuxiaoxiao.chatapi.qq.entity.User;
import me.xuxiaoxiao.chatapi.qq.protocol.*;
import me.xuxiaoxiao.xtools.common.XTools;
import me.xuxiaoxiao.xtools.common.http.XOption;
import me.xuxiaoxiao.xtools.common.http.XRequest;

import java.io.File;
import java.net.CookieManager;
import java.net.CookiePolicy;

class QQAPI {
    String pt_login_sig;
    String qrsig;
    String vfwebqq;
    String qqStr;
    String psessionid;
    XOption httpOption = new XOption(60000, 120000) {
        @Override
        public CookieManager cookieManager() {
            return new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        }
    };

    /**
     * 获取登录所需的各参数。这些参数都在Cookie中返回。
     * 获取登录二维码和轮询登录状态时用到。
     */
    void xlogin() {
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
        request.header("Referer", "http://web2.qq.com/");
        XTools.http(httpOption, request);
    }

    /**
     * 获取登录二维码文件
     *
     * @param path 文件保存的路径
     * @return 获取到的登录二维码
     */
    File ptqrshow(String path) {
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
        request.header("Referer", "https://xui.ptlogin2.qq.com/cgi-bin/xlogin?daid=164&target=self&style=40&pt_disable_pwd=1&mibao_css=m_webqq&appid=501004106&enable_qlogin=0&no_verifyimg=1&s_url=http://web2.qq.com/proxy.html&f_url=loginerroralert&strong_login=1&login_state=10&t=20131024001");
        return XTools.http(httpOption, request).file(path);
    }

    /**
     * 轮询获取登录状态，轮询间隔时间约2秒，包括1：等待操作中（未扫描二维码和未确认登录），2：扫描二维码，3：确认登录，4：二维码过期
     *
     * @return 登录状态
     */
    RspQRLogin ptqrlogin() {
        XRequest request = XRequest.GET("https://ssl.ptlogin2.qq.com/ptqrlogin");
        request.query("action", String.format("0-0-%d", System.currentTimeMillis()));
        request.query("aid", 501004106);
        request.query("daid", 164);
        request.query("from_ui", 1);
        request.query("g", 1);
        request.query("h", 1);
        request.query("js_type", 1);
        request.query("js_ver", 10270);
        request.query("login_sig", pt_login_sig);
        request.query("mibao_css", "m_webqq");
        request.query("pt_uistyle", 40);
        request.query("ptlang", 2052);
        request.query("ptqrtoken", QQTools.hashQRSig(qrsig));
        request.query("ptredirect", 0);
        request.query("t", 1);
        request.query("u1", "http://web2.qq.com/proxy.html");
        request.header("Referer", "https://xui.ptlogin2.qq.com/cgi-bin/xlogin?daid=164&target=self&style=40&pt_disable_pwd=1&mibao_css=m_webqq&appid=501004106&enable_qlogin=0&no_verifyimg=1&s_url=http%3A%2F%2Fweb2.qq.com%2Fproxy.html&f_url=loginerroralert&strong_login=1&login_state=10&t=20131024001");
        return new RspQRLogin(XTools.http(httpOption, request).string());
    }

    public void check_sig(String uri) {
        XRequest request = XRequest.GET(uri);
        XTools.http(httpOption, request);
    }

    public void getvfwebqq() {
        XRequest request = XRequest.GET("http://s.web2.qq.com/api/getvfwebqq");
        request.query("clientid", 53999199);
        request.query("psessionid", "");
        request.query("ptwebqq", "");
        request.query("t", System.currentTimeMillis());
        request.header("Referer", "http://s.web2.qq.com/proxy.html?v=20130916001&callback=1&id=1");
        BaseRsp<ResultVFWebqq> resultVFWebqq = QQTools.GSON.fromJson(XTools.http(httpOption, request).string(), new TypeToken<BaseRsp<ResultVFWebqq>>() {
        }.getType());
        this.vfwebqq = resultVFWebqq.result.vfwebqq;
    }

    public BaseRsp<ResultLogin> login2() {
        XRequest request = XRequest.POST("http://d1.web2.qq.com/channel/login2");
        request.header("Origin", "http://d1.web2.qq.com");
        request.header("Referer", "http://d1.web2.qq.com/proxy.html?v=20151105001&callback=1&id=2");
        request.content("r", QQTools.GSON.toJson(new BaseReq(null, "", vfwebqq, "")));
        BaseRsp<ResultLogin> resultLogin = QQTools.GSON.fromJson(XTools.http(httpOption, request).string(), new TypeToken<BaseRsp<ResultLogin>>() {
        }.getType());
        this.qqStr = String.valueOf(resultLogin.result.uin);
        this.psessionid = resultLogin.result.psessionid;
        return resultLogin;
    }

    public BaseRsp<ResultGetUserFriends> get_user_friends2() {
        XRequest request = XRequest.POST("http://s.web2.qq.com/api/get_user_friends2");
        request.header("Origin", "http://s.web2.qq.com");
        request.header("Referer", "http://s.web2.qq.com/proxy.html?v=20130916001&callback=1&id=1");
        request.content("r", QQTools.GSON.toJson(new BaseReq(QQTools.hash(qqStr, ""), null, vfwebqq, null)));
        return QQTools.GSON.fromJson(XTools.http(httpOption, request).string(), new TypeToken<BaseRsp<ResultGetUserFriends>>() {
        }.getType());
    }

    public BaseRsp<ResultGetGroupNameListMask> get_group_name_list_mask2() {
        XRequest request = XRequest.POST("http://s.web2.qq.com/api/get_group_name_list_mask2");
        request.header("Origin", "http://s.web2.qq.com");
        request.header("Referer", "http://s.web2.qq.com/proxy.html?v=20130916001&callback=1&id=1");
        request.content("r", QQTools.GSON.toJson(new BaseReq(QQTools.hash(qqStr, ""), null, vfwebqq, null)));
        return QQTools.GSON.fromJson(XTools.http(httpOption, request).string(), new TypeToken<BaseRsp<ResultGetGroupNameListMask>>() {
        }.getType());
    }

    public BaseRsp<ResultGetDiscusList> get_discus_list() {
        XRequest request = XRequest.GET("http://s.web2.qq.com/api/get_discus_list");
        request.query("clientid", 53999199);
        request.query("psessionid", psessionid);
        request.query("t", System.currentTimeMillis());
        request.query("vfwebqq", vfwebqq);
        request.header("Referer", "http://s.web2.qq.com/proxy.html?v=20130916001&callback=1&id=1");
        return QQTools.GSON.fromJson(XTools.http(httpOption, request).string(), new TypeToken<BaseRsp<ResultGetDiscusList>>() {
        }.getType());
    }

    public BaseRsp<User> get_self_info2() {
        XRequest request = XRequest.GET("http://s.web2.qq.com/api/get_self_info2");
        request.query("t", System.currentTimeMillis());
        request.header("Referer", "http://s.web2.qq.com/proxy.html?v=20130916001&callback=1&id=1");
        return QQTools.GSON.fromJson(XTools.http(httpOption, request).string(), new TypeToken<BaseRsp<User>>() {
        }.getType());
    }

    public BaseRsp<ResultGetRecentList> get_recent_list2() {
        XRequest request = XRequest.POST("http://d1.web2.qq.com/channel/get_recent_list2");
        request.header("Origin", "http://d1.web2.qq.com");
        request.header("Referer", "http://d1.web2.qq.com/proxy.html?v=20151105001&callback=1&id=2");
        request.content("r", QQTools.GSON.toJson(new BaseReq(null, null, vfwebqq, psessionid)));

        return QQTools.GSON.fromJson(XTools.http(httpOption, request).string(), new TypeToken<BaseRsp<ResultGetRecentList>>() {
        }.getType());
    }

    public BaseRsp<ResultGetOnlineBuddies> get_online_buddies2() {
        XRequest request = XRequest.GET("http://d1.web2.qq.com/channel/get_online_buddies2");
        request.query("clientid", 53999199);
        request.query("psessionid", psessionid);
        request.query("t", System.currentTimeMillis());
        request.query("vfwebqq", vfwebqq);
        request.header("Referer", "http://d1.web2.qq.com/proxy.html?v=20151105001&callback=1&id=2");
        return QQTools.GSON.fromJson(XTools.http(httpOption, request).string(), new TypeToken<BaseRsp<ResultGetOnlineBuddies>>() {
        }.getType());
    }


    public BaseRsp<ResultPoll> poll2() {
        XRequest request = XRequest.POST("http://d1.web2.qq.com/channel/poll2");
        request.header("Origin", "http://d1.web2.qq.com");
        request.header("Referer", "http://d1.web2.qq.com/proxy.html?v=20151105001&callback=1&id=2");
        request.content("r", QQTools.GSON.toJson(new BaseReq(null, "", null, psessionid)));
        return QQTools.GSON.fromJson(XTools.http(httpOption, request).string(), new TypeToken<BaseRsp<ResultPoll>>() {
        }.getType());
    }

    public BaseRsp<User> get_friend_info2(long uin) {
        XRequest request = XRequest.GET("http://s.web2.qq.com/api/get_friend_info2");
        request.query("clientid", 53999199);
        request.query("psessionid", psessionid);
        request.query("t", System.currentTimeMillis());
        request.query("tuin", uin);
        request.query("vfwebqq", vfwebqq);
        request.header("Referer", "http://s.web2.qq.com/proxy.html?v=20130916001&callback=1&id=1");
        return QQTools.GSON.fromJson(XTools.http(httpOption, request).string(), new TypeToken<BaseRsp<User>>() {
        }.getType());
    }

    public BaseRsp<ResultLongNick> get_single_long_nick2(long uin) {
        XRequest request = XRequest.GET("http://s.web2.qq.com/api/get_single_long_nick2");
        request.query("t", System.currentTimeMillis());
        request.query("tuin", uin);
        request.query("vfwebqq", vfwebqq);
        request.header("Referer", "http://s.web2.qq.com/proxy.html?v=20130916001&callback=1&id=1");
        return QQTools.GSON.fromJson(XTools.http(httpOption, request).string(), new TypeToken<BaseRsp<ResultLongNick>>() {
        }.getType());
    }

    public BaseRsp<ResultGetGroupInfo> get_group_info_ext2(long gcode) {
        XRequest request = XRequest.GET("http://s.web2.qq.com/api/get_group_info_ext2");
        request.query("gcode", gcode);
        request.query("t", System.currentTimeMillis());
        request.query("vfwebqq", vfwebqq);
        request.header("Referer", "http://s.web2.qq.com/proxy.html?v=20130916001&callback=1&id=1");
        return QQTools.GSON.fromJson(XTools.http(httpOption, request).string(), new TypeToken<BaseRsp<ResultGetGroupInfo>>() {
        }.getType());
    }

    public BaseRsp<ResultGetDiscuInfo> get_discu_info(long did) {
        XRequest request = XRequest.GET("http://d1.web2.qq.com/channel/get_discu_info");
        request.query("clientid", 53999199);
        request.query("did", did);
        request.query("psessionid", psessionid);
        request.query("t", System.currentTimeMillis());
        request.query("vfwebqq", vfwebqq);
        request.header("Referer", "http://d1.web2.qq.com/proxy.html?v=20151105001&callback=1&id=2");
        return QQTools.GSON.fromJson(XTools.http(httpOption, request).string(), new TypeToken<BaseRsp<ResultGetDiscuInfo>>() {
        }.getType());
    }

    public BaseRsp send_qun_msg2(long group, String content) {
        XRequest request = XRequest.POST("http://d1.web2.qq.com/channel/send_qun_msg2");
        request.header("Origin", "http://d1.web2.qq.com");
        request.header("Referer", "http://d1.web2.qq.com/cfproxy.html?v=20151105001&callback=1");
        request.content("r", new ReqSendGroupMsg(group, content, psessionid));
        return QQTools.GSON.fromJson(XTools.http(httpOption, request).string(), BaseRsp.class);
    }

    public BaseRsp send_discu_msg2(long discuss, String content) {
        XRequest request = XRequest.POST("http://d1.web2.qq.com/channel/send_discu_msg2");
        request.header("Origin", "http://d1.web2.qq.com");
        request.header("Referer", "http://d1.web2.qq.com/cfproxy.html?v=20151105001&callback=1");
        request.content("r", new ReqSendDiscussMsg(discuss, content, psessionid));
        return QQTools.GSON.fromJson(XTools.http(httpOption, request).string(), BaseRsp.class);
    }

    public BaseRsp send_buddy_msg2(long friend, String content) {
        XRequest request = XRequest.POST("http://d1.web2.qq.com/channel/send_buddy_msg2");
        request.header("Origin", "http://d1.web2.qq.com");
        request.header("Referer", "http://d1.web2.qq.com/cfproxy.html?v=20151105001&callback=1");
        request.content("r", new ReqSendFriendMsg(friend, content, psessionid));
        return QQTools.GSON.fromJson(XTools.http(httpOption, request).string(), BaseRsp.class);
    }

    public BaseRsp change_status2(String status) {
        XRequest request = XRequest.GET("http://d1.web2.qq.com/channel/change_status2");
        request.query("clientid", 53999199);
        request.query("newstatus", status);
        request.query("psessionid", psessionid);
        request.query("t", System.currentTimeMillis());
        request.header("Referer", "http://d1.web2.qq.com/proxy.html?v=20151105001&callback=1&id=2");
        return QQTools.GSON.fromJson(XTools.http(httpOption, request).string(), BaseRsp.class);
    }
}