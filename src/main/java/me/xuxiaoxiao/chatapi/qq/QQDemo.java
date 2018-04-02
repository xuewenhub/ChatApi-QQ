package me.xuxiaoxiao.chatapi.qq;

import me.xuxiaoxiao.chatapi.qq.entity.Discuss;
import me.xuxiaoxiao.chatapi.qq.entity.Group;
import me.xuxiaoxiao.chatapi.qq.entity.User;

import java.io.File;
import java.util.Scanner;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

public class QQDemo {
    public static ConsoleHandler handler = new ConsoleHandler();

    public static final QQClient qqClient = new QQClient(new QQClient.QQChatListener() {
        @Override
        public void onQRCode(File qrCode) {
        }

        @Override
        public void onAvatar(String base64Avatar) {
        }

        @Override
        public void onFailure(String reason) {
        }

        @Override
        public void onLogin() {
        }

        @Override
        public void onUserMessage(int msgId, User from, String content) {
            System.out.println(String.format("好友 %s 说：%s", from.nick, content));
        }

        @Override
        public void onGroupMessage(int msgId, Group group, User from, String content) {
            if (from == null) {
                System.out.println(String.format("我在群 %s 中说：%s", group.name, content));
            } else {
                System.out.println(String.format("群 %s 中的 %s 说：%s", group.name, from.nick, content));
            }
        }

        @Override
        public void onDiscussMessage(int msgId, Discuss discuss, User from, String content) {
            if (from == null) {
                System.out.println(String.format("我在讨论组 %s 中说：%s", discuss.name, content));
            } else {
                System.out.println(String.format("讨论组 %s 中的 %s 说：%s", discuss.name, from.nick, content));
            }
        }

        @Override
        public void onLogout() {
        }
    }, new File("D:\\"), handler);

    static {
        handler.setLevel(Level.FINER);
    }

    public static void main(String[] args) {
        qqClient.startup();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("请输入指令");
            switch (scanner.nextLine()) {
                case "sendFriend": {
                    System.out.println("friend:");
                    long friend = Long.parseLong(scanner.nextLine());
                    System.out.println("content:");
                    String content = scanner.nextLine();
                    qqClient.sendFriend(friend, content);
                }
                break;
                case "sendGroup": {
                    System.out.println("group:");
                    long group = Long.parseLong(scanner.nextLine());
                    System.out.println("content:");
                    String content = scanner.nextLine();
                    qqClient.sendGroup(group, content);
                }
                break;
                case "sendDiscuss": {
                    System.out.println("discuss:");
                    long discuss = Long.parseLong(scanner.nextLine());
                    System.out.println("content:");
                    String content = scanner.nextLine();
                    qqClient.sendDiscuss(discuss, content);
                }
                break;
                case "quit":
                    System.out.println("logging out");
                    qqClient.shutdown();
                    return;
                default:
                    System.out.println("未知指令");
                    break;
            }
        }
    }
}
