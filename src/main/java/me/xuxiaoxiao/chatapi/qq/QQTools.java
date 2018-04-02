package me.xuxiaoxiao.chatapi.qq;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.xuxiaoxiao.chatapi.qq.entity.User;
import me.xuxiaoxiao.chatapi.qq.protocol.ResultPoll;
import me.xuxiaoxiao.xtools.common.http.XOption;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.logging.Logger;

public class QQTools {
    static final Logger LOGGER = Logger.getLogger("me.xuxiaoxiao.chatapi.qq");
    static final Gson GSON = new GsonBuilder().registerTypeAdapter(User.class, new User.UserParser()).registerTypeAdapter(ResultPoll.class, new ResultPoll.MessageParser()).create();
    static final XOption HTTPOPTION = new XOption(60000, 90000) {
        @Override
        public CookieManager cookieManager() {
            return new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        }
    };

    public static String hash(String qqStr, String ptWebqq) {
        int[] ptWebqqHash = new int[4];
        for (int i = 0; i < ptWebqq.length(); i++) {
            ptWebqqHash[i % 4] ^= ptWebqq.charAt(i);
        }
        int[] qqStrHash = new int[4];
        long lb = Long.parseLong(qqStr);
        qqStrHash[0] = (int) (lb >> 24 & 255 ^ 'E');
        qqStrHash[1] = (int) (lb >> 16 & 255 ^ 'C');
        qqStrHash[2] = (int) (lb >> 8 & 255 ^ 'O');
        qqStrHash[3] = (int) (lb & 255 ^ 'K');
        int[] hash = new int[8];
        for (int i = 0; i < hash.length; i++) {
            hash[i] = i % 2 == 0 ? ptWebqqHash[i >> 1] : qqStrHash[i >> 1];
        }
        String[] hex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
        String str = "";
        for (int integer : hash) {
            str += hex[integer >> 4 & 15];
            str += hex[integer & 15];
        }
        return str;
    }

    static int hashQRSig(String qrsig) {
        int e = 0, n = qrsig.length();
        for (int i = 0; n > i; ++i)
            e += (e << 5) + qrsig.charAt(i);
        return 2147483647 & e;
    }
}
