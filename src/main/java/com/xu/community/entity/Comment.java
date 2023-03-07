package com.xu.community.entity;

import java.util.Date;

public class Comment {


	private int id;//帖子id
	private int userId;//用户id
	private int entityType;//评论的目标类别，1：帖子；2评论；3用户；4题；5课程；
	private int entityId;//评论目标的id
	private int targetId;//回复目标用户的userid
	private String content;//评论内容
	private int status;//评论的状态。0：正常  1：禁用
	private Date createTime;//评论创建时间

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

	public int getEntityType() {
		return entityType;
	}

	public void setEntityType(int entityType) {
		this.entityType = entityType;
	}

	public int getEntityId() {
		return entityId;
	}

	public void setEntityId(int entityId) {
		this.entityId = entityId;
	}

	public int getTargetId() {
		return targetId;
	}

	public void setTargetId(int targetId) {
		this.targetId = targetId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
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

	@Override
	public String toString() {
		return "Comment{" +
				"id=" + id +
				", userId=" + userId +
				", entityType=" + entityType +
				", entityId=" + entityId +
				", targetId=" + targetId +
				", content='" + content + '\'' +
				", status=" + status +
				", createTime=" + createTime +
				'}';
	}
}
