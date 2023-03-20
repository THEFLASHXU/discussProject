package com.xu.community.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * 功能：封装了用于在消息通知中使用的事件信息
 */
public class Event {
	private String topic;//kafka消息队列的主题
	private int userId;//发送消息的用户id
	private int entityType;//发送消息的实体类型
	private int entityId;//发送消息的实体id
	private int entityUserId;//实体作者id
	private Map<String, Object> data = new HashMap<>();//用于扩展存储，方便存储以后附加的扩展信息

	public String getTopic() {
		return topic;
	}

	public Event setTopic(String topic) {
		this.topic = topic;
		return this;
	}

	public int getUserId() {
		return userId;
	}

	public Event setUserId(int userId) {
		this.userId = userId;
		return this;
	}

	public int getEntityType() {
		return entityType;
	}

	public Event setEntityType(int entityType) {
		this.entityType = entityType;
		return this;
	}

	public int getEntityId() {
		return entityId;
	}

	public Event setEntityId(int entityId) {
		this.entityId = entityId;
		return this;
	}

	public int getEntityUserId() {
		return entityUserId;
	}

	public Event setEntityUserId(int entityUserId) {
		this.entityUserId = entityUserId;
		return this;
	}

	public Map<String, Object> getData() {
		return data;
	}

	public Event setData(String key, Object value) {
		this.data.put(key, value);
		return this;
	}
}
