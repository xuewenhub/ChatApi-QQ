package me.xuxiaoxiao.chatapi.qq.protocol;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class ResultPoll extends ArrayList<ResultPoll.Item> {

    public static class Item {
        public String poll_type;
        public Object value;
    }

    public static class Font {
        public String name = "宋体";
        public String color = "000000";
        public int size = 10;
        public int[] style = {0, 0, 0};
    }

    public static class UserMessage {
        public int msgId;
        public int msgType;
        public long fromUser;
        public long toUser;
        public String content;
        public Font font;
        public long time;
    }

    public static class GroupMessage {
        public int msgId;
        public int msgType;
        public long fromGroup;
        public long fromUser;
        public long toUser;
        public String content;
        public Font font;
        public long time;
    }

    public static class DiscussMessage {
        public int msgId;
        public int msgType;
        public long fromDiscuss;
        public long fromUser;
        public long toUser;
        public String content;
        public Font font;
        public long time;
    }

    public static class MessageParser implements JsonDeserializer {

        @Override
        public Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            ResultPoll resultPoll = new ResultPoll();
            JsonArray jArr = jsonElement.getAsJsonArray();
            for (int i = 0; i < jArr.size(); i++) {
                Item item = new Item();
                JsonObject itemObj = jArr.get(i).getAsJsonObject();
                item.poll_type = itemObj.get("poll_type").getAsString();
                JsonObject valueObj = itemObj.get("value").getAsJsonObject();
                switch (item.poll_type) {
                    case "message": {
                        UserMessage message = new UserMessage();
                        message.msgId = valueObj.get("msg_id").getAsInt();
                        message.msgType = valueObj.get("msg_type").getAsInt();
                        message.fromUser = valueObj.get("from_uin").getAsLong();
                        message.toUser = valueObj.get("to_uin").getAsLong();
                        JsonArray content = valueObj.get("content").getAsJsonArray();
                        message.font = parseFont(content.get(0));
                        StringBuilder sbStr = new StringBuilder();
                        for (int ci = 1; ci < content.size(); ci++) {
                            if (content.get(ci).isJsonPrimitive()) {
                                sbStr.append(content.get(ci).getAsString());
                            }
                        }
                        message.content = sbStr.toString();
                        message.time = valueObj.get("time").getAsLong();
                        item.value = message;
                        break;
                    }
                    case "group_message": {
                        GroupMessage message = new GroupMessage();
                        message.msgId = valueObj.get("msg_id").getAsInt();
                        message.msgType = valueObj.get("msg_type").getAsInt();
                        message.fromGroup = valueObj.get("from_uin").getAsLong();
                        message.fromUser = valueObj.get("send_uin").getAsLong();
                        message.toUser = valueObj.get("to_uin").getAsLong();
                        JsonArray content = valueObj.get("content").getAsJsonArray();
                        message.font = parseFont(content.get(0));
                        StringBuilder sbStr = new StringBuilder();
                        for (int ci = 1; ci < content.size(); ci++) {
                            if (content.get(ci).isJsonPrimitive()) {
                                sbStr.append(content.get(ci).getAsString());
                            }
                        }
                        message.content = sbStr.toString();
                        message.time = valueObj.get("time").getAsLong();
                        item.value = message;
                        break;
                    }
                    case "discu_message": {
                        DiscussMessage message = new DiscussMessage();
                        message.msgId = valueObj.get("msg_id").getAsInt();
                        message.msgType = valueObj.get("msg_type").getAsInt();
                        message.fromDiscuss = valueObj.get("from_uin").getAsLong();
                        message.fromUser = valueObj.get("send_uin").getAsLong();
                        message.toUser = valueObj.get("to_uin").getAsLong();
                        JsonArray content = valueObj.get("content").getAsJsonArray();
                        message.font = parseFont(content.get(0));
                        StringBuilder sbStr = new StringBuilder();
                        for (int ci = 1; ci < content.size(); ci++) {
                            if (content.get(ci).isJsonPrimitive()) {
                                sbStr.append(content.get(ci).getAsString());
                            }
                        }
                        message.content = sbStr.toString();
                        message.time = valueObj.get("time").getAsLong();
                        item.value = message;
                        break;
                    }
                }
                resultPoll.add(item);
            }
            return resultPoll;
        }

        public Font parseFont(JsonElement element) {
            JsonObject jObj = element.getAsJsonArray().get(1).getAsJsonObject();
            Font font = new Font();
            font.name = jObj.get("name").getAsString();
            font.color = jObj.get("color").getAsString();
            font.size = jObj.get("size").getAsInt();
            JsonArray styleArr = jObj.get("style").getAsJsonArray();
            font.style = new int[styleArr.size()];
            for (int i = 0; i < font.style.length; i++) {
                font.style[i] = styleArr.get(i).getAsInt();
            }
            return font;
        }
    }
}
