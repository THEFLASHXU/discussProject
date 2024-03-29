package com.xu.community.controller;

import com.xu.community.entity.*;
import com.xu.community.event.EventProducer;
import com.xu.community.service.CommentService;
import com.xu.community.service.DiscussPostService;
import com.xu.community.service.LikeService;
import com.xu.community.service.UserService;
import com.xu.community.util.CommunityConstant;
import com.xu.community.util.CommunityUtil;
import com.xu.community.util.HostHolder;
import com.xu.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.annotation.Target;
import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private UserService userService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private CommentService commentService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private EventProducer eventProducer;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 功能：发布帖子 ，数据来自于jQuery发送过来的ajax数据。 @ResponseBody的作用其实是将java对象转为json格式的数据。
     */
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(403, "你还没有登录哦");
        }
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);
        System.out.println("discusspostController发帖成功");
        //触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(post.getId());
        eventProducer.fireEvent(event);//发布发帖事件到kafka

        //对于新发布的帖子，给一个初始的分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, post.getId());//把需要计算分数的帖子存入redis，value数据类型是set，set可以去重，在一个时间段内，无需重复计算分数信息。

        // 报错的情况，将来统一处理
        return CommunityUtil.getJSONString(0, "发布成功");
    }

    /**
     * 功能：查询并向前端返回帖子信息，评论信息，回复信息
     * 
     * @param page 分页信息,（像这种的自定义形参，会自动注入到modelandview中）
     */
    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {
        // 查询帖子信息
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", post);
        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount", likeCount);
        // 点赞状态（判断下是否登录，未登录：显示未点赞 已登录：查询点赞状态）
        int likeStatus = hostHolder.getUser() == null ? 0
            : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeStatus", likeStatus);
        // 查询作者信息
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);
        // 评论分页信息
        page.setLimit(5);// 每页显示五条
        page.setStaticPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount());// 直接从帖子的数据库查询该帖子有多少评论，也可以进行连表查询，就是麻烦。

        // 评论：给帖子的评论
        // 回复：给评论的评论
        // 查询当前帖子当前页面下的所有评论
        List<Comment> commentList =
            commentService.findCommentByEntity(ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        // 评论VO(view object显示的对象)列表
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                Map<String, Object> commentVo = new HashMap<>();
                commentVo.put("comment", comment);
                commentVo.put("user", userService.findUserById(comment.getUserId()));
                // 点赞数量
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount",likeCount);
                // 点赞状态（判断下是否登录，未登录：显示未点赞 已登录：查询点赞状态）
                likeStatus = hostHolder.getUser() == null ? 0
                        : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeStatus", likeStatus);
                // 当前评论的回复列表
                List<Comment> replyList =
                    commentService.findCommentByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                // 当前评论的回复vo列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) {// 遍历当前评论的每一条回复
                        Map<String, Object> replyVo = new HashMap<>();
                        replyVo.put("reply", reply);
                        replyVo.put("user", userService.findUserById(reply.getUserId()));
                        // 获取回复的对象
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", target);
                        // 点赞数量
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount",likeCount);
                        // 点赞状态（判断下是否登录，未登录：显示未点赞 已登录：查询点赞状态）
                        likeStatus = hostHolder.getUser() == null ? 0
                                : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeStatus", likeStatus);
                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys", replyVoList);
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                // 回复的数量
                commentVo.put("replyCount", replyCount);
                commentVoList.add(commentVo);
            }
        }
        model.addAttribute("comments", commentVoList);
        return "/site/discuss-detail";
    }

    /**
     * 功能：将帖子置顶
     */
    @RequestMapping(path = "/top", method = RequestMethod.POST)
    @ResponseBody//置顶之后无需整体刷新，所以异步请求。
    public String setTop(int id) {
        discussPostService.updateType(id, 1);//把帖子的type值改为1，1代表置顶

        // 置顶后，帖子发生了变化，为了让es搜索到最新的帖子信息，因此需要重新发布帖子事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

    /**
     * 功能：将帖子加精
     */
    @RequestMapping(path = "/wonderful", method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int id) {
        discussPostService.updateStatus(id, 1);//帖子的状态设置为”精选“

        // 加精后，帖子发生了变化，为了让es搜索到最新的帖子信息，因此需要重新发布帖子事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);
        //把帖子放到计算分数的队列。
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, id);

        return CommunityUtil.getJSONString(0);
    }

    // 删除
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int id) {
        discussPostService.updateStatus(id, 2);

        // 删除帖子后，为了不让es搜索到他，触发删帖事件
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }
}
