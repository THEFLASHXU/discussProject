package com.xu.community.service;

import com.xu.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

/**
 * 功能：通过redis数据库处理点赞问题
 */
@Service
public class LikeService {
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 功能：对某一个实体进行点赞，若已点赞则取消点赞，若未点赞则增加点赞
     * @param userId 点赞用户的id
     * @param entityType
     * @param entityId
     * @param entityUserId 实体发布者的id
     */
    public void like(int userId, int entityType, int entityId,int entityUserId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);//获取redis中点赞数据的key
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);

                boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);//判断是否点过赞

                operations.multi();

                if (isMember) {
                    redisTemplate.opsForSet().remove(entityLikeKey, userId);
                    operations.opsForValue().decrement(userLikeKey);
                } else {
                    redisTemplate.opsForSet().add(entityLikeKey, userId);
                    operations.opsForValue().increment(userLikeKey);
                }

                return operations.exec();
            }
        });
    }

    // 查询某实体点赞的数量
    public long findEntityLikeCount(int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);//获取redis中的key
        return redisTemplate.opsForSet().size(entityLikeKey);//查询点赞key中有多少个值，代表有多少条赞
    }

    // 查询某人对某实体的点赞状态
    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey, userId) ? 1 : 0;
    }
    // 查询某个用户获得的赞
    public int findUserLikeCount(int userId) {
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count.intValue();
    }
}
