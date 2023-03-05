package com.xu.community.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

/**
 * 功能：生成随机字符串以用来给各种数据命名；加密字符串
 */
public class CommunityUtil {
    //生成随机字符串
    public static String generateUUID(){//静态方法可以直接通过类名调用，任何的实例也都可以调用
        return UUID.randomUUID().toString().replaceAll("_","");//将随机字符串中所有的下划线去掉
    }

    /**
     * 功能：实现MD5加密，将传统密码直接加密并不安全，需要在密码之后拼上一个随机字符串 盐 再进行加
     * @param key
     * @return
     */
    public static String md5(String key){
        if (StringUtils.isBlank(key)){//利用commons提供方法排除空串
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }
}
