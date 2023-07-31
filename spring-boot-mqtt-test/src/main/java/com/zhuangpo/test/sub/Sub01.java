package com.zhuangpo.test.sub;

import com.zhuangpo.test.utils.HexConvertUtil;
import com.zhuangpo.mqtt.annotation.Topic;
import com.zhuangpo.mqtt.common.SuperConsumer;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 *  订阅主题
 * 
 * @author xub
 * @since 2023/7/31 上午11:27
 */
@Slf4j
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
