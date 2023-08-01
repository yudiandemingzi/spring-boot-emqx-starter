非常简单的一个emqx项目，代码非常清晰，对于初次学习mqtt来讲，这个项目值得一学。

## 一、项目概述

#### 1、技术架构

项目总体技术选型

```
SpringBoot2.3.6 + Maven3.5.4 + mqtt1.2.2 + lombok(插件)
```
#### 2、项目整体结构

```makefile
spring-boot-mqtt-core # 核心实现
spring-boot-mqtt-test # 测试实现
```
<br>

## 一、项目学习点

#### 1、@SneakyThrows注解

@SneakyThrows是Lombok包下的注解，主要作用于针对异常捕获，减少了代码量，让代码看起来更加的整洁。

正常代码

```java
  public static void sleep(long seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
```

加上@SneakyThrows注解可以改成

```java
    @SneakyThrows(InterruptedException.class)
    public static void sleep(long seconds) {
        Thread.sleep(seconds * 1000);
    }
```

这两个编译后的class文件结果是一样的。

#### 2、将Spring容器对象封装成静态工具类

代码如下

```java
@Component
public class ApplicationContextUtil {

    private static ApplicationContextUtil instance;
    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    public void applicationContextUtil() {
        instance = this;
    }

    public static <T> T getBean(Class<T> clazz) {
        return instance.applicationContext.getBean(clazz);
    }
}
```

这样我们可以通过工具类来使用

```java
MqttClient client = ApplicationContextUtil.getBean(MqttClient.class);
```

#### 3、配置类不要用new来直接创建对象，而是通过@Bean注解注入容器

上面这么讲其实还不是很清晰，这里通过代码演示，就以项目中的MqttClient配置类来讲解

可以优化的代码

```java
 @Resource
    private MqttConfigProperties configProperties;

    @Bean
    public IMqttAsyncClient mqttClient() throws MqttException {
        String clientId = configProperties.getAdminClientPrefix();
        IMqttAsyncClient client = new MqttAsyncClient(configProperties.getBrokerUrl(), clientId, new MemoryPersistence());
        // MQTT 连接选项配置
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setUserName("emqx");
        connOpts.setPassword("123456".toCharArray());
        connOpts.setCleanSession(true);
        connOpts.setKeepAliveInterval(60);
        connOpts.setAutomaticReconnect(true);
        client.connect(connOpts);
        return client;
    }
```

这里有两点可以优化:

- MqttConnectOptions在这里面直接new，而没有做成bean项注入容器。
- MqttConfigProperties做为配置项,应该以参数的方式传入mqttClient()方法。

优化后代码

```java
 @Bean
    @Order(1)
    public MqttConnectOptions option(EmqProperties emqProperties) {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(emqProperties.getUserName());
        options.setPassword(emqProperties.getPassword().toCharArray());
        options.setCleanSession(emqProperties.getCleanSession());
        options.setAutomaticReconnect(emqProperties.getReconnect());
        options.setConnectionTimeout(emqProperties.getTimeout());
        options.setKeepAliveInterval(emqProperties.getKeepAlive());
        return options;
    }

    @Bean
    @Order(2)
    @ConditionalOnBean
    public MqttClient mqttClient(MqttConnectOptions options, EmqProperties emqProperties) throws Exception {
        MqttClient client = new MqttClient(emqProperties.getBroker(), Inet4Address.getLocalHost().getHostAddress() + ":" + port, new MemoryPersistence());
        client.connect(options);
        return client;
    }
```


#### 4、抽离思想

主要有以下几点：

#####1、主题抽离
抽离出成一个单独的类Sub01,Sub08，一个主题就像一个不同的策略。
#####2、顶成主题抽离
将每个主题公共方法抽取出来，做一些逻辑处理
```java
@Slf4j
public abstract class SuperConsumer<T> implements IMqttMessageListener, MsgDecoder<T> {
    /**
     * 公共部分
     */
    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) {
        log.debug("\r\n 收到主题 :\r\n" + topic + " 的消息:\r\n" + new String(mqttMessage.getPayload()));
        ThreadUtils.executorService.submit(() -> {
            try {
                T decoder = decoder(mqttMessage);
                msgHandler(topic, decoder);
            } catch (Exception ex) {
                //解决业务处理错误导致断线问题
                log.error(ex.toString());
            }
        });
    }

    /**
     * 具体业务处理由子类去实现
     *
     * @param topic  主题
     * @param entity 参数
     */
    protected abstract void msgHandler(String topic, T entity);
}
```

#####3、编解码抽离

定义了编解码接口，但具体实现确由每个主题去实现，毕竟每个主题的编解码方式不一样，这样抽离的好处扩展性非常好。

`顶层解码接口`

```java
public interface MsgDecoder<T> {
    T decoder(MqttMessage msg);
}
```

`父主题 implements`

```java
@Slf4j
public abstract class SuperConsumer<T> implements IMqttMessageListener, MsgDecoder<T> {
    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) {
        //实现还是子主题实现
        T decoder = decoder(mqttMessage);
    }
}
```

`具体子主题实现`

```java
@Topic(topic = "device/01/up")
public class Sub01 extends SuperConsumer<String> {
    @Override
    protected void msgHandler(String topic, String entity) {
        log.info("收到设备的数据： {}", entity);
    }

    @Override
    public String decoder(MqttMessage msg) {
        return HexConvertUtil.BinaryToHexString(msg.getPayload());
    }
}
```

#### 5、泛行思想<T>

因为每个子主题的对象都不一样，所以采用泛行思想。编解码接口泛行,父主题泛行,子主题泛行。

#### 6、线程池使用

因为主题很多,这里一定要异步操作，不然一旦消息多，系统资源可能耗尽导致OOM,除了用线程池，也可以考虑用消息队列。

```java
    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) {
        log.debug("\r\n 收到主题 :\r\n" + topic + " 的消息:\r\n" + new String(mqttMessage.getPayload()));
        ThreadUtils.executorService.submit(() -> {
            T decoder = decoder(mqttMessage);
            msgHandler(topic, decoder);
            }
        });
    }
```