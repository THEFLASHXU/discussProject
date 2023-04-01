package com.xu.community.controller.interceptor;


import com.xu.community.entity.User;
import com.xu.community.service.DataService;
import com.xu.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 功能：在用户访问网站时，记录网站访问量和活跃用户数据
 */
@Component
public class DataInterceptor implements HandlerInterceptor {

	@Autowired
	private DataService dataService;

	@Autowired
	private HostHolder hostHolder;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		// 统计UV
		String ip = request.getRemoteHost();
		dataService.recordUV(ip);

		// 统计DAU
		User user = hostHolder.getUser();
		if (user != null) {
			dataService.recordDAU(user.getId());
		}

		return true;
	}
}