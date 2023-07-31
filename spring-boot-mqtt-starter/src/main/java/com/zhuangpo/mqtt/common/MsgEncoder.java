package com.zhuangpo.mqtt.common;

/**
 *  将编码接口抽离出来
 * 
 * @author xub
 * @since 2023/7/31 上午11:15
 */
public interface MsgEncoder<T> {
    /**
     * 消息编码为字节数组
     * @param t
     * @return
     */
    byte[] encoder(T t);
}
