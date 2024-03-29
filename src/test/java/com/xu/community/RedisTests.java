package com.xu.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTests {
	@Autowired
	private RedisTemplate redisTemplate;

	@Test
	public void testString() {
		String redisKey = "test:count2";
		redisTemplate.opsForValue().set(redisKey, 1);
		System.out.println(redisTemplate.opsForValue().get(redisKey));
		System.out.println(redisTemplate.opsForValue().increment(redisKey));
		System.out.println(redisTemplate.opsForValue().increment(redisKey, 100));
		System.out.println(redisTemplate.opsForValue().decrement(redisKey));
	}
}
