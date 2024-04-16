package com.huayu.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 错误消息处理器
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.rabbitmq.listener.simple.retry", name = "enabled", havingValue = "true")
public class ErrorConfiguration {


    /**
     * 定义处理少数错误的交换机
     *
     * @return direct交换机
     */
    @Bean
    public DirectExchange errorExchange() {
        return new DirectExchange("error.direct");
    }

    /**
     * 定义处理少数错误的errorQueue
     *
     * @return 错误队列
     */
    @Bean
    public Queue errorQueue() {
        return new Queue("error.queue");
    }

    /**
     * 定义交换机和队列的绑定关系
     *
     * @param errorQueue 错误队列
     * @param errorExchange 错误交换机
     * @return 队列与交换机的绑定关系
     */
    @Bean
    public Binding errorBinding(Queue errorQueue, DirectExchange errorExchange) {
        return BindingBuilder.bind(errorQueue).to(errorExchange).with("error");
    }

    /**
     * 将错误交换机和错误队列设置为错误信息处理
     *
     * @param rabbitTemplate RabbitMQ客户端
     * @return MessageRecover
     */
    @Bean
    public MessageRecoverer messageRecoverer(RabbitTemplate rabbitTemplate) {
        return new RepublishMessageRecoverer(rabbitTemplate, "error.direct", "error");
    }
}
