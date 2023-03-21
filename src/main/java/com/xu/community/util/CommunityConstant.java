package com.xu.community.util;

public interface CommunityConstant {
    // 激活成功
    int ACTIVATION_SUCCESS = 0;
    // 重复激活
    int ACTIVATION_REPEAT = 1;
    // 激活失败
    int ACTIVATION_FAILURE = 2;
    // 默认状态的登陆凭证超时时间
    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;
    // “记住我”状态下的登陆凭证超时时间
    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 100;
    // 实体类型：帖子
    int ENTITY_TYPE_POST = 1;
    // 实体类型：评论
    int ENTITY_TYPE_COMMENT = 2;
    // 实体类型：用户
    int ENTITY_TYPE_USER=3;
    // kafka的topic：评论
    String TOPIC_COMMENT = "comment";
    // kafka的topic：点赞
    String TOPIC_LIKE = "like";
    // kafka的topic：关注
    String TOPIC_FOLLOW = "follow";
    //kafka的topic：发帖
    String TOPIC_PUBLISH = "publish";
    // 系统用户的id
    int SYSTEM_USER_ID=1;
}
