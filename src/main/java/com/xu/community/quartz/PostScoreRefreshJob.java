package com.xu.community.quartz;


import com.xu.community.entity.DiscussPost;
import com.xu.community.service.DiscussPostService;
import com.xu.community.service.ElasticsearchService;
import com.xu.community.service.LikeService;
import com.xu.community.util.CommunityConstant;
import com.xu.community.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 功能：设置一个定时执行的任务，定期更新帖子分数。
 */
public class PostScoreRefreshJob implements Job, CommunityConstant {

	private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private DiscussPostService discussPostService;

	@Autowired
	private LikeService likeService;

	@Autowired
	private ElasticsearchService elasticsearchService;

	//牛客纪元：牛客成立的时间。 后续计算分数需要用到 发布时间-牛客纪元。
	private static final Date epoch;

	static {//在静态块里初始化，因为只需要初始化一次
		try {
			epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");//初始化时间
		} catch (ParseException e) {
			throw new RuntimeException("初始化牛客纪元失败!", e);
		}
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		String redisKey = RedisKeyUtil.getPostScoreKey();
		BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);//把对应key的value全取出来，这些value都是各个帖子的id

		if (operations.size() == 0) {//如果没有取到数据
			logger.info("[任务取消] 没有需要刷新的帖子!");
			return;
		}

		logger.info("[任务开始] 正在刷新帖子分数: " + operations.size());
		while (operations.size() > 0) {
			this.refresh((Integer) operations.pop());//进行计算分数
		}
		logger.info("[任务结束] 帖子分数刷新完毕!");
	}

	/**
	 * 功能：计算帖子的分数
	 */
	private void refresh(int postId) {
		DiscussPost post = discussPostService.findDiscussPostById(postId);//查找对应的帖子

		if (post == null) {
			logger.error("该帖子不存在: id = " + postId);
			return;
		}

		// 是否精华
		boolean wonderful = post.getStatus() == 1;
		// 评论数量
		int commentCount = post.getCommentCount();
		// 点赞数量
		long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);

		// 计算权重
		double w = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
		// 分数 = 帖子权重 + 距离天数
		double score = Math.log10(Math.max(w, 1))
				+ (post.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24);
		// 更新帖子分数
		discussPostService.updateScore(postId, score);//更新帖子分数
		// 同步搜索数据
		post.setScore(score);
		elasticsearchService.saveDiscussPost(post);
	}

}