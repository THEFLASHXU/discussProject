package com.xu.community.controller;

import com.xu.community.entity.Comment;
import com.xu.community.entity.DiscussPost;
import com.xu.community.entity.Event;
import com.xu.community.event.EventProducer;
import com.xu.community.service.CommentService;
import com.xu.community.service.DiscussPostService;
import com.xu.community.util.CommunityConstant;
import com.xu.community.util.HostHolder;
import com.xu.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {
	@Autowired
	private HostHolder hostHolder;
	@Autowired
	private CommentService commentService;
	@Autowired
	private DiscussPostService discussPostService;
	@Autowired
	private EventProducer eventProducer;
	@Autowired
	private RedisTemplate redisTemplate;

	/**
	 * 功能：向帖子添加评论
	 */
	@RequestMapping(path = "/add/{discussPostId}", method = RequestMethod.POST)
	public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {
		comment.setUserId(hostHolder.getUser().getId());
		comment.setStatus(0);
		comment.setCreateTime(new Date());
		commentService.addComment(comment);

		// 触发评论事件
		// Event event = new Event()
		// 		.setTopic(TOPIC_COMMENT)
		// 		.setUserId(hostHolder.getUser().getId())
		// 		.setEntityType(comment.getEntityType())
		// 		.setEntityId(comment.getEntityId())
		// 		.setData("postId", discussPostId);
		// if (comment.getEntityType() == ENTITY_TYPE_POST) {//给帖子的评论
		// 	DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId());
		// 	event.setEntityUserId(target.getUserId());
		// } else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {//给评论的回复
		// 	Comment target = commentService.findCommentById(comment.getEntityId());
		// 	event.setEntityUserId(target.getUserId());
		// }
		// eventProducer.fireEvent(event);
		//
		// 触发评论事件
		Event event = new Event();
		if (comment.getEntityType() == ENTITY_TYPE_POST) {//给帖子的评论
			DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId());
			if (target.getUserId()!=hostHolder.getUser().getId()){
				event.setEntityUserId(target.getUserId())
						.setTopic(TOPIC_COMMENT)
						.setUserId(hostHolder.getUser().getId())
						.setEntityType(comment.getEntityType())
						.setEntityId(comment.getEntityId())
						.setData("postId", discussPostId);
				eventProducer.fireEvent(event);
			}
		} else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {//给评论的回复
			Comment target = commentService.findCommentById(comment.getEntityId());
			if (target.getUserId()!=hostHolder.getUser().getId()){
				event.setEntityUserId(target.getUserId())
						.setTopic(TOPIC_COMMENT)
						.setUserId(hostHolder.getUser().getId())
						.setEntityType(comment.getEntityType())
						.setEntityId(comment.getEntityId())
						.setData("postId", discussPostId);
				eventProducer.fireEvent(event);
			}
		}




		//给帖子评论之后，需要重新写入发帖事件.因为需要在搜索的时候展示帖子所获得评论的数量.这个数据是要从es中获取，所以需要更新
		if (comment.getEntityType() == ENTITY_TYPE_POST) {
			// 触发发帖事件
			event = new Event()
					.setTopic(TOPIC_PUBLISH)
					.setUserId(comment.getUserId())
					.setEntityType(ENTITY_TYPE_POST)
					.setEntityId(discussPostId);
			eventProducer.fireEvent(event);
			//计算帖子分数
			String redisKey = RedisKeyUtil.getPostScoreKey();
			redisTemplate.opsForSet().add(redisKey, discussPostId);
		}


		return "redirect:/discuss/detail/"+discussPostId;
	}
}
