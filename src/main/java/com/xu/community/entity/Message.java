package com.xu.community.entity;

import java.util.Date;

public class Message {
	private int id;//私信id，主键
	private int fromId;//发送人的userid, 1代表系统用户发的系统通知
	private int toId;//接收者的userid
	private String conversationId;//会话id，用来标识会话，方便查询，是由fromId和toId拼接的，拼接逻辑是小id-大id。
	private String content;//私信内容
	private int status;//私信状态'0-未读;1-已读;2-删除;
	private Date createTime;//私信创建时间

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getFromId() {
		return fromId;
	}

	public void setFromId(int fromId) {
		this.fromId = fromId;
	}

	public int getToId() {
		return toId;
	}

	public void setToId(int toId) {
		this.toId = toId;
	}

	public String getConversationId() {
		return conversationId;
	}

	public void setConversationId(String conversationId) {
		this.conversationId = conversationId;
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
		return "Message{" +
				"id=" + id +
				", fromId=" + fromId +
				", toId=" + toId +
				", conversationId='" + conversationId + '\'' +
				", content='" + content + '\'' +
				", status=" + status +
				", createTime=" + createTime +
				'}';
	}
}
