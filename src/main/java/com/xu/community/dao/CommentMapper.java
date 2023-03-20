package com.xu.community.dao;

import com.xu.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {
    // 查询某一实体下的评论
    List<Comment> selectCommentByEntity(int entityType, int entityId, int offset, int limit);

    // 查询某一实体下的评论数量
    int selectCountByEntity(int entityType, int entityId);

    // 添加评论
    int insertComment(Comment comment);
    //根据评论的id查找评论
    Comment selectCommentById(int id);
}
