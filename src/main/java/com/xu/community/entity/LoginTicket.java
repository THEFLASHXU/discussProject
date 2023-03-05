package com.xu.community.entity;

import java.util.Date;

/**
 * 登陆凭证表
 * 包含字段：
 * id:作为表的主键
 * userId：用户的id
 * ticket：登录凭证，唯一标识，一个随机的字符串
 * status：判断登录凭证是否有效。 0：正常，1：过期
 * expired：登陆凭证过期时间
 */
public class LoginTicket {
	private int id;
	private int userId;
	private String ticket;
	private int status;
	private Date expired;

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

	public String getTicket() {
		return ticket;
	}

	public void setTicket(String ticket) {
		this.ticket = ticket;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Date getExpired() {
		return expired;
	}

	public void setExpired(Date expired) {
		this.expired = expired;
	}

	@Override
	public String toString() {
		return "LoginTicket{" +
				"id=" + id +
				", userId=" + userId +
				", ticket='" + ticket + '\'' +
				", status=" + status +
				", expired=" + expired +
				'}';
	}
}
