package com.xu.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * 设置redis序列化的方式，key和value的序列化，还要单独考虑hash的序列化
 */
@Configuration
public class RedisConfig {
	/**
	 * 配置redis模板对象，以后用这个对象中的功能访问redis
	 * @param factory
	 * @return
	 */
	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(factory);

		// 设置key的序列化方式
		template.setKeySerializer(RedisSerializer.string());
		// 设置value的序列化方式
		template.setValueSerializer(RedisSerializer.json());
		// 设置hash的key的序列化方式
		template.setHashKeySerializer(RedisSerializer.string());
		// 设置hash的value的序列化方式
		template.setHashValueSerializer(RedisSerializer.json());

		template.afterPropertiesSet();
		return template;
	}
}
