package com.xu.community.service;

import com.xu.community.entity.User;
import com.xu.community.util.CommunityConstant;
import com.xu.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FollowService implements CommunityConstant {
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private UserService userService;

	//功能：关注实体
	public void follow(int userId, int entityType, int entityId) {
		redisTemplate.execute(new SessionCallback() {
			@Override
			public Object execute(RedisOperations operations) throws DataAccessException {
				String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
				String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

				operations.multi();

				operations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
				operations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());

				return operations.exec();
			}
		});
	}
	//功能：取消关注实体
	public void unfollow(int userId, int entityType, int entityId) {
		redisTemplate.execute(new SessionCallback() {
			@Override
			public Object execute(RedisOperations operations) throws DataAccessException {
				String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
				String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

				operations.multi();

				operations.opsForZSet().remove(followeeKey, entityId);
				operations.opsForZSet().remove(followerKey, userId);

				return operations.exec();
			}
		});
	}

	// 查询关注的实体的数量
	public long findFolloweeCount(int userId, int entityType) {
		String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);//获取redis的key
		return redisTemplate.opsForZSet().zCard(followeeKey);//zCard能查询出zSet的数量
	}

	// 查询实体的粉丝的数量
	public long findFollowerCount(int entityType, int entityId) {
		String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
		return redisTemplate.opsForZSet().zCard(followerKey);
	}

	// 查询当前用户是否已关注该实体
	public boolean hasFollowed(int userId, int entityType, int entityId) {
		String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
		return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;//score是在zset中寻找当前value的分数，如果找不到就返回null
	}

	/**
		功能：查询某用户关注的人
	 	支持分页
 	 */
	public List<Map<String, Object>> findFollowees(int userId, int offset, int limit) {
		String followeeKey = RedisKeyUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);//获取查询的redis key
		// reverseRange是按照zSet中的score的值从大到小进行范围查询的一种，可以指定查询开始的行数和查询终止的行数。
		Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);
		//redis中的value都是一堆id值，需要根据这个id值查出user对象实体
		if (targetIds == null) {
			return null;
		}

		List<Map<String, Object>> list = new ArrayList<>();
		for (Integer targetId : targetIds) {//遍历查询出来的redis对象
			Map<String, Object> map = new HashMap<>();
			User user = userService.findUserById(targetId);//根据userId查找user实体
			map.put("user", user);
			Double score = redisTemplate.opsForZSet().score(followeeKey, targetId);//从zset中查找出score值，作为时间状态来显示
			map.put("followTime", new Date(score.longValue()));
			list.add(map);
		}

		return list;
	}

	// 查询某用户的粉丝
	public List<Map<String, Object>> findFollowers(int userId, int offset, int limit) {
		String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER, userId);
		Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);
		if (targetIds == null) {
			return null;
		}
		List<Map<String, Object>> list = new ArrayList<>();
		for (Integer targetId : targetIds) {
			Map<String, Object> map = new HashMap<>();
			User user = userService.findUserById(targetId);
			map.put("user", user);
			Double score = redisTemplate.opsForZSet().score(followerKey, targetId);
			map.put("followTime", new Date(score.longValue()));
			list.add(map);
		}
		return list;
	}

}
