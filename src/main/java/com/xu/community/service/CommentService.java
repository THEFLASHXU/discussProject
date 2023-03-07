package com.xu.community.service;

import com.xu.community.dao.CommentMapper;
import com.xu.community.entity.Comment;
import com.xu.community.util.CommunityConstant;
import com.xu.community.util.CommunityUtil;
import com.xu.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService implements CommunityConstant {
	@Autowired
	private CommentMapper commentMapper;
	@Autowired
	private SensitiveFilter sensitiveFilter;
	@Autowired
	private DiscussPostService discussPostService;
	// 查询某一实体下的评论
	public List<Comment> findCommentByEntity(int entityType, int entityId, int offset, int limit) {
		return commentMapper.selectCommentByEntity(entityType, entityId, offset, limit);
	}
	// 查询某一实体下的评论数量
	public int findCommentCount(int entityType, int entityId) {
		return commentMapper.selectCountByEntity(entityType,entityId);
	}
	//添加评论
	@Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
	public int addComment(Comment comment) {
		if (comment == null) {
			throw new IllegalArgumentException("参数不能为空！");
		}
		//添加评论
		comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
		comment.setContent(sensitiveFilter.filter(comment.getContent()));
		int rows = commentMapper.insertComment(comment);
		if (comment.getEntityType() == ENTITY_TYPE_POST) {
			//查询这条评论的目标帖子的评论数量
			int count=commentMapper.selectCountByEntity(comment.getEntityType(),comment.getEntityId());
			//更新帖子评论数量
			discussPostService.updateCommentCount(comment.getEntityId(), count);
		}
		return rows;
	}

}
