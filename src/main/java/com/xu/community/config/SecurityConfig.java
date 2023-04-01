package com.xu.community.config;

import com.xu.community.util.CommunityConstant;
import com.xu.community.util.CommunityUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 功能：基于springsecurity进行权限管理。
 * 需要重写3个configure方法，第一个configure方法负责管理拦截范围，第二个configure负责管理登录和退出验证，第三个configure负责管理各个功能的权限验证。
 * 由于之前已经做过了登录退出验证，那么就不再配置第二个configure
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {
	/**
	 * 配置：让springsecurity忽略对静态资源的拦截
	 */
	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/resources/**");
	}

	/**
	 * 配置：管理各个功能的权限验证。
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		//声明需要授权管理的conrtoller访问路径
		http.authorizeRequests()
				//声明需要授权管理的conrtoller访问路径
				.antMatchers(
						"/user/setting",
						"/user/upload",
						"/discuss/add",
						"/comment/add/**",
						"/letter/**",
						"/notice/**",
						"/like",
						"/follow",
						"/unfollow"
				)
				//这些路径只要拥有以下任意一种权限就可以访问。
				.hasAnyAuthority(
						AUTHORITY_USER,
						AUTHORITY_ADMIN,
						AUTHORITY_MODERATOR
				)
				.antMatchers(
						"/discuss/top",//置顶帖子
						"/discuss/wonderful"//加精帖子
				)
				.hasAnyAuthority(
						AUTHORITY_MODERATOR//版主
				)
				.antMatchers(
						"/discuss/delete",//删除帖子
						"/data/**",
						"/actuator/**"
				)
				.hasAnyAuthority(
						AUTHORITY_ADMIN//管理员
				)
				.anyRequest().permitAll()//除上述请求以外的所有其他请求都可以自由访问。
				.and().csrf().disable();//不启用csrf攻击检查，如需启用，可以把这行去掉之后，自行配置。

		// 权限不够时的处理。需要考虑不同的请求类型。如果是普通请求权限不够，那么可以返回一个html页面。如果是异步请求权限不够，则不可以返回html页面，需要返回json格式的数据
		http.exceptionHandling()
				.authenticationEntryPoint(new AuthenticationEntryPoint() {
					// authenticationEntryPoint：用于判断没有登录时如何处理
					@Override
					public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
						String xRequestedWith = request.getHeader("x-requested-with");//通过请求的消息头获取x-requested-with的值
						if ("XMLHttpRequest".equals(xRequestedWith)) {//x-requested-with的值如果是XMLHttpRequest，则代表异步请求
							//异步请求：提示未登录信息。
							response.setContentType("application/plain;charset=utf-8");
							PrintWriter writer = response.getWriter();
							writer.write(CommunityUtil.getJSONString(403, "你还没有登录哦!"));
							System.out.println("异步请求，提示未登录信息");
						} else {
							//普通请求，返回登陆页面
							System.out.println("普通请求，返回登陆页面");
							response.sendRedirect(request.getContextPath() + "/login");
						}
					}
				})
				.accessDeniedHandler(new AccessDeniedHandler() {
					// accessDeniedHandler：用于判断权限不足时如何处理
					@Override
					public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException, ServletException {
						String xRequestedWith = request.getHeader("x-requested-with");
						if ("XMLHttpRequest".equals(xRequestedWith)) {
							//异步请求的处理
							response.setContentType("application/plain;charset=utf-8");
							PrintWriter writer = response.getWriter();
							writer.write(CommunityUtil.getJSONString(403, "你没有访问此功能的权限!"));
							System.out.println("异步请求，提示没有权限信息");
						} else {
							//普通请求的处理
							System.out.println("普通请求，返回没有权限页面");
							response.sendRedirect(request.getContextPath() + "/denied");
						}
					}
				});

		// Security底层默认会拦截/logout请求,进行退出处理.
		// 覆盖它默认的逻辑,才能执行我们自己的退出代码.修改之后springsecurity默认拦截的就是/securitylogout这个路径。
		http.logout().logoutUrl("/securitylogout");
	}

}