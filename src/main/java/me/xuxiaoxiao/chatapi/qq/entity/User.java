package me.xuxiaoxiao.chatapi.qq.entity;

import com.google.gson.*;

import java.lang.reflect.Type;

public class User {
    public long uin;//mf
    public long account;//md
    public int face;//mf
    public String nick;//mf
    public String lnick;//m
    public String gender;//mf
    public int allow;//mf
    public int stat;//f
    public boolean online;//f
    public String card;//g
    public String markname;//f
    public Category category;//f
    public String phone;//mf
    public String mobile;//mf
    public String email;//mf
    public String country;//mf
    public String province;//mf
    public String city;//mf
    public int blood;//mf
    public int constel;//mf
    public int shengxiao;//mf
    public String birthday;//mf
    public String college;//mf
    public String occupation;//mf
    public String homepage;//mf
    public String personal;//mf
    public int vip_level;//mf

    public static final class UserParser implements JsonDeserializer<User> {

        @Override
        public User deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            User user = new User();
            JsonObject userJObj = jsonElement.getAsJsonObject();
            user.uin = userJObj.get("uin").getAsLong();
            if (userJObj.has("account")) {
                user.account = userJObj.get("account").getAsLong();
            }
            user.face = userJObj.get("face").getAsInt();
            user.nick = userJObj.get("nick").getAsString();
            if (userJObj.has("lnick")) {
                user.lnick = userJObj.get("lnick").getAsString();
            }
            user.gender = userJObj.get("gender").getAsString();
            user.allow = userJObj.get("allow").getAsInt();
            if (userJObj.has("stat")) {
                user.stat = userJObj.get("stat").getAsInt();
            }
            user.phone = userJObj.get("phone").getAsString();
            user.mobile = userJObj.get("mobile").getAsString();
            user.email = userJObj.get("email").getAsString();
            user.country = userJObj.get("country").getAsString();
            user.province = userJObj.get("province").getAsString();
            user.city = userJObj.get("city").getAsString();
            user.blood = userJObj.get("blood").getAsInt();
            user.constel = userJObj.get("constel").getAsInt();
            user.shengxiao = userJObj.get("shengxiao").getAsInt();
            JsonObject birthdayJObj = userJObj.getAsJsonObject("birthday");
            user.birthday = String.format("%d-%d-%d", birthdayJObj.get("year").getAsInt(), birthdayJObj.get("month").getAsInt(), birthdayJObj.get("day").getAsInt());
            user.college = userJObj.get("college").getAsString();
            user.occupation = userJObj.get("occupation").getAsString();
            user.homepage = userJObj.get("homepage").getAsString();
            user.personal = userJObj.get("personal").getAsString();
            user.vip_level = userJObj.get("vip_info").getAsInt();
            return user;
        }
    }
}
