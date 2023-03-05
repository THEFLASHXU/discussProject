package com.xu.community.config;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * @Configuration:放在类的上面， 这个类相当于 xml 配置文件，可以在其中声明 bean
 * @Bean:放在方法的上面， 方法的返回值是对象类型， 这个对象注入到 spring ioc 容器
 * 创建配置类（等同于 xml 配置文件）
 */
@Configuration
public class KaptchaConfig {
    @Bean
    public Producer kaptchaProducer(){
        //在这里写propertie是一次性的，相当于把那些应该写在application.properties文件中的参数提取到这里进行一次性声明
        Properties properties=new Properties();
        properties.setProperty("kaptcha.image.width","100");
        properties.setProperty("kaptcha.image.height","40");
        properties.setProperty("kaptcha.textproducer.font.size","32");
        properties.setProperty("kaptcha.textproducer.font.color","0,0,0");
        properties.setProperty("kaptcha.textproducer.char.string","0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        properties.setProperty("kaptcha.textproducer.char.length","4");
        properties.setProperty("kaptcha.noise.impl","com.google.code.kaptcha.impl.NoNoise");

        DefaultKaptcha kaptcha=new DefaultKaptcha();
        //Config是kaptcha.util包中带的对象，用于配置参数并传入进kaptcha
        Config config=new Config(properties);
        kaptcha.setConfig(config);
        return kaptcha;
    }

}
