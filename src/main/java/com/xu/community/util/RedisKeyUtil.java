package com.xu.community.util;

/**
 * 功能：配置Redis的key
 * 操作redis实现数据访问，由于太简单了，就没有写数据访问层dao
 */
public class RedisKeyUtil {
	//定义冒号分割符,方便后续复用
	private static  final String SPLIT=":";
	//定义key的前缀
	private static final String PREFIX_ENTITY_LIFE = "like:entity";
	private static final String PREFIX_USER_LIKE = "like:user";

	/**
	 * 功能：定义某个实体的赞的key
		 * key的结构:like:entity:entityType:entityId
		 * value的结构：用set数据类型存储给这个帖子或评论点赞的用户id
		 * 例：like:entity:1:109  {
		 * 101,102,104,203,239
	 * }
	 */
	public static String getEntityLikeKey(int entityType, int entityId) {
		return PREFIX_ENTITY_LIFE + SPLIT + entityType + SPLIT + entityId;
	}

	/**
	 * 功能：定义某个用户的赞
	 * key的形式：like:user:101
	 */
	public static String getUserLikeKey(int userId) {
		return PREFIX_USER_LIKE + SPLIT + userId;
	}
}
