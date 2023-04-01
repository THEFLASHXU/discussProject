package com.xu.community.service;


import com.xu.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 功能：和用户访问量UV和活跃用户DAU有关的数据操作
 */
@Service
public class DataService {

	@Autowired
	private RedisTemplate redisTemplate;

	private SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");//用于格式化时间


	/**
	 * 功能：将指定的ip存入uv
	 */
	public void recordUV(String ip) {
		String redisKey = RedisKeyUtil.getUVKey(df.format(new Date()));//获取当日点击量的rediskey
		redisTemplate.opsForHyperLogLog().add(redisKey, ip);//将用户的ip存入到uv中
	}

	/**
	 * 功能：统计指定日期范围内的UV
	 */
	public long calculateUV(Date start, Date end) {
		if (start == null || end == null) {
			throw new IllegalArgumentException("参数不能为空!");
		}

		// 整理该日期范围内的key，把指定日期范围内的key都放到一个集合中
		List<String> keyList = new ArrayList<>();
		Calendar calendar = Calendar.getInstance();//用于累加日期，对日期做运算的工具
		calendar.setTime(start);//用起始日期给calendar赋值
		while (!calendar.getTime().after(end)) {//遍历日期，直到当前日期晚于停止日期
			String key = RedisKeyUtil.getUVKey(df.format(calendar.getTime()));//获取当前日期的key
			keyList.add(key);//把key放到集合中
			calendar.add(Calendar.DATE, 1);//查看下一天
		}

		// 合并这些数据
		String redisKey = RedisKeyUtil.getUVKey(df.format(start), df.format(end));//获取合并以后数据的key
		redisTemplate.opsForHyperLogLog().union(redisKey, keyList.toArray());//把keyList集合中的数据合并到一起，存储到redisKey中。

		// 返回统计的结果
		return redisTemplate.opsForHyperLogLog().size(redisKey);
	}

	/**
	 * 功能：将指定用户计入DAU
	 */
	public void recordDAU(int userId) {
		String redisKey = RedisKeyUtil.getDAUKey(df.format(new Date()));
		redisTemplate.opsForValue().setBit(redisKey, userId, true);//把userid作为setBit的索引，将这一索引位置上的boolen值改为true
	}

	// 统计指定日期范围内的DAU

	/**
	 * 功能：统计指定日期范围内的日活跃用户数量
	 */
	public long calculateDAU(Date start, Date end) {
		if (start == null || end == null) {
			throw new IllegalArgumentException("参数不能为空!");
		}

		// 整理该日期范围内的key
		List<byte[]> keyList = new ArrayList<>();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(start);
		while (!calendar.getTime().after(end)) {
			String key = RedisKeyUtil.getDAUKey(df.format(calendar.getTime()));
			keyList.add(key.getBytes());
			calendar.add(Calendar.DATE, 1);
		}

		// 只要在这个时间范围内登录都算活跃，所以进行OR运算
		return (long) redisTemplate.execute(new RedisCallback() {
			@Override
			public Object doInRedis(RedisConnection connection) throws DataAccessException {
				String redisKey = RedisKeyUtil.getDAUKey(df.format(start), df.format(end));//用于存储一段时间dau数据的key
				connection.bitOp(RedisStringCommands.BitOperation.OR,
						redisKey.getBytes(), keyList.toArray(new byte[0][0]));//把keylist集合中的数据转换成二维的byte数组做or运算。
				return connection.bitCount(redisKey.getBytes());
			}
		});
	}

}
