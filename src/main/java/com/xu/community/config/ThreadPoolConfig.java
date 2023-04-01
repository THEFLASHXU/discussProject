package com.xu.community.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 功能：配置spring提供的线程池
 */
@Configuration
@EnableScheduling//允许启动定时执行线程功能
@EnableAsync
public class ThreadPoolConfig {
}
