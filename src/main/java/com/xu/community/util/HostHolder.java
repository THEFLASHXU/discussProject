package com.xu.community.util;

import com.xu.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * 功能：持有用户信息，实现线程隔离
 * ThreadLocal可以实现线程隔离，内容是按照<线程名--数据>的形式存储的
 */
@Component
public class HostHolder {
	private ThreadLocal<User> users=new ThreadLocal<>();
	public void setUser(User user){
		users.set(user);
	}

	public User getUser(){
		return users.get();
	}
	public void clear(){
		users.remove();
	}
}
