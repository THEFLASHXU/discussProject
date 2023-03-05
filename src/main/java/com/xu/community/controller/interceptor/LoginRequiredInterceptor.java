package com.xu.community.controller.interceptor;

import com.xu.community.annotation.LoginRequired;
import com.xu.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * 功能：防止未登录用户访问需要登陆后才能访问的功能。
 * 拦截程序的请求方法，检查方法中是否带有自定义的注解@loginRequired，如果检测到自定义注解，则从hostholder中查找是否存在登录信息。
 */
@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {
	@Autowired
	private HostHolder hostHolder;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		if (handler instanceof HandlerMethod) {//判断拦截对象是否是方法，不是方法不拦截
			HandlerMethod handlerMethod=(HandlerMethod) handler;//把拦截对象转换成方法处理
			Method method = handlerMethod.getMethod();//获取方法信息
			LoginRequired loginRequired=method.getAnnotation(LoginRequired.class);//判断方法中是否有自定义注解
			if (loginRequired != null && hostHolder.getUser() == null) {//如果有自定义注解却没登录
				response.sendRedirect(request.getContextPath() + "/login");// 跳转回登陆页面。
				return false;
			}
		}
		return true;
	}
}
