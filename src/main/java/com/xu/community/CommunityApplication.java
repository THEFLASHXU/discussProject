package com.xu.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class CommunityApplication {

    @PostConstruct
    public void init() {
        // 解决netty启动冲突问题
        // see Netty4Utils.setAvailableProcessors()
        // 把这个参数设置为false中就会跳过netty的安全检查
        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }
    public static void main(String[] args) {
        SpringApplication.run(CommunityApplication.class, args);
    }

}
