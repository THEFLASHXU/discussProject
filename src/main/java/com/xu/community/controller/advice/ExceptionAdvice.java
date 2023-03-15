package com.xu.community.controller.advice;

import com.xu.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @ControllerAdvice
 * 用于修饰类，表示该类是Controller的全局配置类
 */
@ControllerAdvice(annotations = Controller.class)//统一处理异常，处理的范围是所有带有@controller注解的类
public class ExceptionAdvice {
	private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

	@ExceptionHandler({Exception.class})
	public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
		logger.error("服务器发生异常: " + e.getMessage());
		for (StackTraceElement element : e.getStackTrace()) {//打印异常栈中的每个异常信息
			logger.error(element.toString());
		}
		//判断当前请求的类型，如果是异步请求则不应该返回网页，应该返回json数据。如果不是异步请求则跳转网页
		String xRequestedWith = request.getHeader("x-requested-with");//获取请求类型的固定写法
		if ("XMLHttpRequest".equals(xRequestedWith)) {//如果是异步请求
			response.setContentType("application/plain;charset=utf-8");//设置返回的数据类型，application/plain代表返回普通类型，需要后续自己手动转成json类型
			PrintWriter writer = response.getWriter();
			writer.write(CommunityUtil.getJSONString(1, "服务器异常!"));
		} else {//如果是普通请求，则重定向到错误页面
			response.sendRedirect(request.getContextPath() + "/error");
		}
	}
}
