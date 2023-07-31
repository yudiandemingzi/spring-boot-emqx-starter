package com.zhuangpo.mqtt.annotation;

import com.zhuangpo.mqtt.entity.Pattern;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 *  自定义主题注解
 * 
 * @author xub
 * @since 2023/7/31 上午11:14
 */
@Component
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Topic {

    /**
     * topic
     * @return
     */
    String topic() default "";

    /**
     * qos
     * @return
     */
    int qos() default 0;

    /**
     * 订阅模式
     * @return
     */
    Pattern patten() default Pattern.NONE;

    /**
     * 共享订阅组
     * @return
     */
    String group() default "group1";
}