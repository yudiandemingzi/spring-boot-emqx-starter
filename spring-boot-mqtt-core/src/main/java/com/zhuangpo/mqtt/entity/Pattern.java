package com.zhuangpo.mqtt.entity;

/**
 *  订阅模式
 * 
 * @author xub
 * @since 2023/7/31 上午11:22
 */
public enum Pattern {
    /**
     * 普通订阅
     */
    NONE,
    /**
     * 不带群组的共享订阅
     */
    QUEUE,
    /**
     * 带群组的共享订阅
     */
    SHARE;
}
