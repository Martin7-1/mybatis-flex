# SQL 审计

SQL 审计是一项非常重要的工作，是企业数据安全体系的重要组成部分，通过 SQL 审计功能为数据库请求进行全程记录，为事后追溯溯源提供了一手的信息，同时可以通过可以对恶意访问及时警告管理员，为防护策略优化提供数据支撑。

同时、提供 SQL 访问日志长期存储，满足等保合规要求。

## 开启审计功能<Badge type="tip" text="^1.0.5" />

Mybaits-Flex 的 SQL 审计功能，默认是关闭的，若开启审计功能，许添加如下配置。

```java
AuditManager.setAuditEnable(true)
```

默认情况下，Mybaits-Flex 的审计消息（日志）只会输出到控制台，如下所示：

```
>>>>>>Sql Audit: {platform='mybatis-flex', module='null', url='null', user='null', userIp='null', hostIp='192.168.3.24', query='SELECT * FROM `tb_account` WHERE `id` = ?', queryParams=[1], queryTime=1679991024523, elapsedTime=1}
>>>>>>Sql Audit: {platform='mybatis-flex', module='null', url='null', user='null', userIp='null', hostIp='192.168.3.24', query='SELECT * FROM `tb_account` WHERE `id` = ?', queryParams=[1], queryTime=1679991024854, elapsedTime=3}
>>>>>>Sql Audit: {platform='mybatis-flex', module='null', url='null', user='null', userIp='null', hostIp='192.168.3.24', query='SELECT * FROM `tb_account` WHERE `id` = ?', queryParams=[1], queryTime=1679991025100, elapsedTime=2}
```

Mybaits-Flex 消息包含了如下内容：

- **platform**：平台，或者是运行的应用
- **module**：应用模块
- **url**：执行这个 Sql 涉及的 URL 地址
- **user**：执行这个 Sql 涉及的 平台用户
- **userIp**：执行这个 sql 的平台用户 IP 地址
- **hostIp**：执行这个 sql 的服务器 IP 地址
- **query**：sql 内容
- **queryParams**：sql 参数
- **queryTime**：sql 执行的时间
- **elapsedTime**：sql 执行消耗的时间
- **metas**：其他扩展元信息

::: tip 提示
> 通过以上的消息内容可知：每个 SQL 的执行，都包含了：哪个访问用户、哪个 IP 地址访问，访问的是哪个 URL 地址，这个 SQL 的参数是什么，执行的时间是什么，执行
> 花费了多少时间等等。这样，通过 Mybatis-flex 的 SQL 审计功能，我们能全盘了解到每个 SQL 的执行情况。
:::


## 自定义 SQL 审计内容

Mybatis-Flex 内置了一个名为 `MessageCreator` 的接口，我们只需实现该接口，并为 `AuditManager` 配置新的 `MessageCreator` 即可，如下所示：

```java
public class MyMessageCreator implements MessageCreator {
    
    @Override
    public AuditMessage create() {
        AuditMessage message = new AuditMessage();
       
        // 在这里
        // 设置 message 的基础内容，包括 platform、module、url、user、userIp、hostIp 内容
        // 剩下的 query、queryParams、queryTime、elapsedTime 为 mybatis-flex 设置
        
        return message;
    }
}
```

并为 `AuditManager` 配置新写的 `MyMessageCreator`：

```java
MessageCreator creator = new MyMessageCreator();
AuditManager.setMessageCreator(creator);
```



## 自定义 SQL 审计 Reporter

Reporter 负责把 Mybaits-Flex 收集的 SQL 审计日志发送到指定位置，在当前的版本中，只内置了一个 `ConsoleMessageReporter` 用于把 SQL 审计日志发送到控制台的，代码如下：

```java
public class ConsoleMessageReporter implements MessageReporter {

    @Override
    public void sendMessages(List<AuditMessage> messages) {
        for (AuditMessage message : messages) {
            System.out.println(">>>>>>Sql Audit: " + message.toString());
        }
    }

}
```

通过去实现 `MessageReporter` 接口，我们便可以自定义自己的 Reporter，示例代码如下：

```java
public class MyMessageReporter implements MessageReporter {

    @Override
    public void sendMessages(List<AuditMessage> messages) {
        //在这里把 messages 审计日志发送到指定位置
        //比如 
        // 1、通过 http 协议发送到指定服务器
        // 2、通过日志工具发送到日志平台
        // 3、通过 Kafka 等 MQ 发送到指定平台
    }

}
```

编写好 `MyMessageReporter` ，在应用启动的时候，为 `AuditManager` 配置新的 `MessageCollector`，示例如下：

```java
MessageCollector collector = new ScheduledMessageCollector(10, new MyMessageReporter());
AuditManager.setMessageCollector(collector);
```

## ScheduledMessageCollector

`ScheduledMessageCollector` 是用于收集 SQL 审计消息（日志），并 **定时的** 把收集的日志通过 `MessageReporter` 发送到指定位置。
我们也可以通过调用 `AuditManager.setMessageCollector()` 方法配置自己的消息收集器（MessageCollector）。
