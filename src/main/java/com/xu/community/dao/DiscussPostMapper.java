package com.xu.community.dao;

import com.xu.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 实现与帖子有关的数据库交互功能
 */
//加了mapper注解才能让容器找到这个接口并装配
@Mapper
public interface DiscussPostMapper {
    /**
     * 功能：查询帖子
     * 这里是否传入userId进行查询需要看情况,在个人中心查看用户自己的帖子时需要靠userid查询，其他时候不需要，因此需要编写动态sql
     * offset：分页后当前页第一个帖子的行号
     * limit：每一页分多少个帖子
     * orderMode:排序方式 0：按时间排  1：按热度排
     */
    List<DiscussPost> selectDiscussPosts(int userId,int offset,int limit,int orderMode);

    /**
     * 功能：查询帖子数量
     * userId:在个人帖子查询时才用得上，要使用动态sql
     * @Param注解用于给参数取别名
     * 如果只有一个参数，并且在<if>里使用，则必须加别名。
     */
    int selectDiscussPostRows(@Param("userId") int userId);

    /**
     * 功能：发布帖子
     * @param discussPost 帖子实体
     */
    int insertDiscussPost(DiscussPost discussPost);

    /**
     * 功能：查询帖子详情
     * @param id
     * @return
     */
    DiscussPost selectDiscussPostById(int id);

    /**
     * 更新帖子评论的数量
     */
    int updateCommentCount(int id, int commentCount);

    /**
     * 功能：修改帖子的类型
     */
    int updateType(int id, int type);

    /**
     * 功能：修改帖子的状态
     */
    int updateStatus(int id, int status);

    /**
     * 修改帖子的分数
     */
    int updateScore(int id, double score);
}
