package com.zhuangpo.mqtt.common;

import org.eclipse.paho.client.mqttv3.MqttMessage;


/**
 *  将解码接口抽离出来
 * 
 * @author xub
 * @since 2023/7/31 上午11:15
 */
public interface MsgDecoder<T> {
    /**
     * 下位机消息解码器
     * @param msg
     * @return
     */
    T decoder(MqttMessage msg);
}
