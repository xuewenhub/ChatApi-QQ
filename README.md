# ChatApi-QQ
这是一个Java版本的QQ聊天客户端，使用的全部接口来自于网页版QQ，让你能够开发自己的QQ聊天机器人

## 有何优点？
* 使用了整理后的实体类，屏蔽了网页版QQ杂乱的数据
* 对接口和流程进行了封装，更加简单易用
* 暴露了一个监听器，可以自己实现监听器以开发自己的业务功能
* 网页版QQ全部功能的支持
    * 监听好友、群、讨论组消息
    * 向好友、群、讨论组发送消息
    * 改变登录状态

## 如何使用
* 使用maven下载必要依赖
```xml
<dependencies>
    <!--xtools基础工具库-->
    <dependency>
        <groupId>me.xuxiaoxiao</groupId>
        <artifactId>xtools-common</artifactId>
        <version>1.1.1</version>
    </dependency>
    <!--gson工具类-->
    <dependency>
        <groupId>com.google.code.gson</groupId>
        <artifactId>gson</artifactId>
        <version>2.8.2</version>
    </dependency>
</dependencies>
```

* 以下是一个学别人说话的小机器人，用到了该库提供的大部分功能
```java

public class QQDemo {
    
    public static final QQClient QQ_CLIENT = new QQClient(new QQClient.QQChatListener() {
        @Override
        public void onQRCode(File qrCode) {
            System.out.println(String.format("获取到登录二维码：%s", qrCode.getAbsolutePath()));
        }
        
        @Override
        public void onAvatar(String base64Avatar) {
            System.out.println(String.format("获取到用户头像：%s", base64Avatar));
        }
        
        @Override
        public void onException(String reason) {
            System.out.println(String.format("程序异常：%s", reason));
        }
        
        @Override
        public void onLogin() {
            System.out.println("登录成功");
        }
        
        @Override
        public void onMessage(QQMessage qqMessage) {
            if (qqMessage.fromGroup != null && qqMessage.fromGroupMember.id != QQ_CLIENT.userMe().id) {
                QQ_CLIENT.sendText(qqMessage.fromGroup, qqMessage.content);
            }
            if (qqMessage.fromDiscuss != null && qqMessage.fromDiscussMember.id != QQ_CLIENT.userMe().id) {
                QQ_CLIENT.sendText(qqMessage.fromDiscuss, qqMessage.content);
            }
            if (qqMessage.fromUser != null && qqMessage.fromUser.id != QQ_CLIENT.userMe().id) {
                QQ_CLIENT.sendText(qqMessage.fromUser, qqMessage.content);
            }
        }
        
        @Override
        public void onLogout() {
            System.out.println("退出登录");
        }
    });
    
    public static void main(String[] args) {
        QQ_CLIENT.startup();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("请输入指令");
            switch (scanner.nextLine()) {
                case "sendFriend": {
                    System.out.println("friendId:");
                    long friendId = Long.parseLong(scanner.nextLine());
                    System.out.println("content:");
                    String content = scanner.nextLine();
                    QQ_CLIENT.sendText(QQ_CLIENT.userFriend(friendId), content);
                }
                break;
                case "sendGroup": {
                    System.out.println("groupId:");
                    long groupId = Long.parseLong(scanner.nextLine());
                    System.out.println("content:");
                    String content = scanner.nextLine();
                    QQ_CLIENT.sendText(QQ_CLIENT.userGroup(groupId), content);
                }
                break;
                case "sendDiscuss": {
                    System.out.println("discussId:");
                    long discussId = Long.parseLong(scanner.nextLine());
                    System.out.println("content:");
                    String content = scanner.nextLine();
                    QQ_CLIENT.sendText(QQ_CLIENT.userDiscuss(discussId), content);
                }
                break;
                case "quit":
                    System.out.println("logging out");
                    QQ_CLIENT.shutdown();
                    return;
                default:
                    System.out.println("未知指令");
                    break;
            }
        }
    }
}
```