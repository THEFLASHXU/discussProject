package com.xu.community.service;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.xu.community.dao.DiscussPostMapper;
import com.xu.community.entity.DiscussPost;
import com.xu.community.util.SensitiveFilter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DiscussPostService {
    private static final Logger logger = LoggerFactory.getLogger(DiscussPostService.class);
    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Value("${caffeine.posts.max-size}")
    private int maxSize;//本地缓存最大的数量

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;// 超时时间

    // Caffeine核心接口: Cache, LoadingCache, AsyncLoadingCache


    private LoadingCache<String, List<DiscussPost>> postListCache;// 帖子列表缓存,第一个参数是key,格式为offset:limit

    private LoadingCache<Integer, Integer> postRowsCache;// 帖子总数缓存,第一个参数是key,key的形式为userid,目前只有0的状态.其他的还没做.

    /**
     * 功能:初始化帖子列表缓存.该方法在service类加载完毕后自动执行.
     */
    @PostConstruct
    public void init() {
        // 初始化帖子列表缓存
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {//告诉caffeine,缓存里没有想要的数据时,该怎么去查询
                    @Nullable
                    @Override
                    public List<DiscussPost> load(@NonNull String key) throws Exception {
                        if (key == null || key.length() == 0) {//判断key是否为空
                            throw new IllegalArgumentException("参数错误!");
                        }

                        String[] params = key.split(":");//以:分割key
                        if (params == null || params.length != 2) {
                            throw new IllegalArgumentException("参数错误!");
                        }
                        //提取key中的参数
                        int offset = Integer.valueOf(params[0]);
                        int limit = Integer.valueOf(params[1]);

                        // 可以做二级缓存: Redis -> mysql

                        logger.debug("load post list from DB.");
                        return discussPostMapper.selectDiscussPosts(0, offset, limit, 1);//从mysql中查想要的数据
                    }
                });
        // 初始化帖子总数缓存
        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Nullable
                    @Override
                    public Integer load(@NonNull Integer key) throws Exception {
                        logger.debug("load post rows from DB.");
                        return discussPostMapper.selectDiscussPostRows(key);
                    }
                });
    }

    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit,int orderMode) {
        if (userId == 0 && orderMode == 1) {//如果访问的是首页,按"热度"排序时,才访问缓存数据
            return postListCache.get(offset + ":" + limit);//访问缓存,key的形式是offset:limit
        }
        //其他情况还是访问数据库
        logger.debug("load post list from DB.");
        return discussPostMapper.selectDiscussPosts(userId, offset, limit, orderMode);
    }
    public int findDiscussPostRows(int userId) {
        if (userId == 0) {//如果查找的是首页的帖子总数,则从缓存数据中查找
            return postRowsCache.get(userId);
        }
        //否则还是从数据库中找
        logger.debug("load post rows from DB.");
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    /**
     * 功能：发布帖子，检查帖子文字内容是否合法
     * @param post
     * @return
     */
    public int addDiscussPost(DiscussPost post){
        if (post == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        //转义html标签
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        // 过滤敏感词
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));
        //将帖子存入数据库
        return discussPostMapper.insertDiscussPost(post);
    }

    /**
     *功能：按帖子id查询帖子
     * @param id 帖子id
     * @return
     */
    public DiscussPost findDiscussPostById(int id) {
        return discussPostMapper.selectDiscussPostById(id);
    }

    /**
     * 功能：更新帖子的评论数量
     * @param id
     * @param commentCount
     * @return
     */
    public int updateCommentCount(int id, int commentCount) {
        return discussPostMapper.updateCommentCount(id, commentCount);
    }

    /**
     * 功能：更新帖子的类型
     */
    public int updateType(int id, int type) {
        return discussPostMapper.updateType(id, type);
    }

    /**
     * 功能：更新帖子的状态
     */
    public int updateStatus(int id, int status) {
        return discussPostMapper.updateStatus(id, status);
    }

    /**
     * 功能：更新帖子的分数
     */
    public int updateScore(int id, double score) {
        return discussPostMapper.updateScore(id, score);
    }
}
