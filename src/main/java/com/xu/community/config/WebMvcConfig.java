package com.xu.community.config;

import com.xu.community.controller.interceptor.DataInterceptor;
import com.xu.community.controller.interceptor.LoginRequiredInterceptor;
import com.xu.community.controller.interceptor.LoginTicketInterceptor;
import com.xu.community.controller.interceptor.MessageInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 功能：用来配置拦截器。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
	@Autowired
	private LoginTicketInterceptor loginTicketInterceptor;
	@Autowired
	private LoginRequiredInterceptor loginRequiredInterceptor;//用户权限控制，后续使用spring security替代。
	@Autowired
	private MessageInterceptor messageInterceptor;
	@Autowired
	private DataInterceptor dataInterceptor;
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(loginTicketInterceptor)
				.excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");
		// registry.addInterceptor(loginRequiredInterceptor)
		// 		.excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");
		registry.addInterceptor(messageInterceptor)
				.excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");
		registry.addInterceptor(dataInterceptor)
				.excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");
	}

}
