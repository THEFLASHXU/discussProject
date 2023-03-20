package com.xu.community.controller.interceptor;

import com.xu.community.entity.User;
import com.xu.community.service.MessageService;
import com.xu.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 功能：实时更新未读消息的数量
 */
@Component
public class MessageInterceptor implements HandlerInterceptor {

	@Autowired
	private HostHolder hostHolder;

	@Autowired
	private MessageService messageService;

	//postHandle 在调用controller之后，调用模板之前，这时候更新数据
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		User user = hostHolder.getUser();
		if (user != null && modelAndView != null) {
			int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);//未读私信数量
			int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);//未读通知数量
			modelAndView.addObject("allUnreadCount", letterUnreadCount + noticeUnreadCount);
		}
	}
}