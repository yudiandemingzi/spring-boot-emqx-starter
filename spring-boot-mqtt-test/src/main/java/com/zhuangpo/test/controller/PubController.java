package com.zhuangpo.test.controller;

import com.zhuangpo.test.utils.HexConvertUtil;
import com.zhuangpo.mqtt.utils.PubMessageUtils;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *  测试接口
 * 
 * @author xub
 * @since 2023/7/31 上午11:27
 */
@RestController
@RequestMapping("/mqtt")
public class PubController {

    @GetMapping("/pub")
    public String pub(String topic, String hexmessage) throws MqttException {
        boolean pub = PubMessageUtils.pub(topic, HexConvertUtil.hexStringToBytes(hexmessage));
        if (pub) {
            return "ok";
        } else {
            return "error";
        }
    }
}
