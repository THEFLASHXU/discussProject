package com.xu.community.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;

/**
 * 讨论内容表
 * 包含字段：
 *      帖子id
 *      发帖用户id
 *      帖子标题
 *      评论
 *      帖子类型（控制是否置顶）
 *      帖子状态（正常，精华，拉黑）
 *      创建时间
 *      评论数量
 *      帖子热度评分
 */
//配置elasticsearch，设置索引名字，类型，分片，副本
// @Document(indexName = "discusspost", type = "_doc", shards = 6, replicas = 3)
@Document(indexName = "discusspost", shards = 6, replicas = 3)
public class DiscussPost {
    /**
     * 想使用elasticsearch进行查询，需要对实体的属性进行配置，声明他们在es中做存储时的状态
     * type：存储的数据类型
     * analyzer：存储时的解析器   使用分词器从文本中提取关键字，将这些关键字作为搜索时的索引依据  ik_max_word能尽可能多的分词，用于存储
     * searchAnalyzer：搜索时的解析器   ik_smart能尽可能少的分词，用于高性能搜索
      */

    @Id
    private int id;

    @Field(type = FieldType.Integer)
    private int userId;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;

    @Field(type = FieldType.Integer)
    private int type;

    @Field(type = FieldType.Integer)
    private int status;

    @Field(type = FieldType.Date)
    private Date createTime;

    @Field(type = FieldType.Integer)
    private int commentCount;

    @Field(type = FieldType.Double)
    private double score;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "DiscussPost{" +
                "id=" + id +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", createTime=" + createTime +
                ", commentCount=" + commentCount +
                ", score=" + score +
                '}';
    }
}
