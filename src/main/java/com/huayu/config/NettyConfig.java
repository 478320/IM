package com.huayu.config;

import com.huayu.handler.WebSocketHandler;
import com.huayu.service.ITransactionalNettyService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * netty配置类
 */
@Configuration
public class NettyConfig {

    /**
     * 将WebSocketHandle交给Spring管理，以使用Spring事务功能
     *
     * @param transactionalNettyService WebSocket事务对象
     * @return WebSocket事务对象
     */
    @Bean
    @Scope("prototype")
    public WebSocketHandler myNettyHandler(ITransactionalNettyService transactionalNettyService) {
        return new WebSocketHandler(transactionalNettyService);
    }

}
