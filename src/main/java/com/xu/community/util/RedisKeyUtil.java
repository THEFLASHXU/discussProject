package com.xu.community.util;

/**
 * 功能：配置Redis的key 操作redis实现数据访问，由于太简单了，就没有写数据访问层dao
 */
public class RedisKeyUtil {
    // 定义冒号分割符,方便后续复用
    private static final String SPLIT = ":";
    // 定义key的前缀
    private static final String PREFIX_ENTITY_LIFE = "like:entity";// 实体收到的赞
    private static final String PREFIX_USER_LIKE = "like:user";// 用户收到的赞
    private static final String PREFIX_FOLLOWEE = "followee";// 某用户的关注者
    private static final String PREFIX_FOLLOWER = "follower";// 某用户的粉丝
	private static final String PREFIX_KAPTCHA = "kaptcha";//验证码

    /**
     * 功能：定义某个实体的赞的key
	 * key的结构:like:entity:entityType:entityId
	 * value的结构：用set数据类型存储给这个帖子或评论点赞的用户id
	 * 例：like:entity:1:109 {
     * 101,102,104,203,239
	 * }
     */
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIFE + SPLIT + entityType + SPLIT + entityId;
    }

    /**
     * 功能：定义某个用户的赞
	 * key的形式：like:user:101
	 * value的形式：点赞的数值
     */
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    /**
     * 功能：某个用户关注的实体
	 * key的形式：followee:userId（谁关注的）:entityType（关注的实体类型）
	 * value的形式 zset(entityId实体id,now当前时间)
     */
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    /**
     * 功能：某个实体拥有的粉丝
	 * key的形式:follower:entityType:entityId
	 * value的形式：zset(userId,now)
     */
    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

	/**
	 * 功能：构造登录验证码的key
	 * 未优化之前验证码是要在每次登陆的时候临时生成，并保存在session中，当用户发起登录请求后，controller检查用户填写的验证码和session中的验证码是否相同
	 * 因此老方法可以通过session得知哪个验证码对应哪个用户。新方法中，owner是一个随机的字符串，用来标识用户，
	 * key的结构：keptcha:ai223187
 	 */
	public static String getKaptchaKey(String owner) {
		return PREFIX_KAPTCHA + SPLIT + owner;
	}


}
