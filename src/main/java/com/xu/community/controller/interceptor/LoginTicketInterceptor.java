package com.xu.community.controller.interceptor;

import com.xu.community.entity.LoginTicket;
import com.xu.community.entity.User;
import com.xu.community.service.UserService;
import com.xu.community.util.CookieUtil;
import com.xu.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * 功能：在请求之前判断用户是否登录
 */
@Component
public class LoginTicketInterceptor implements HandlerInterceptor {
	@Autowired
	private UserService userService;
	@Autowired
	private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {
        // 从cookie中获取凭证
        String ticket = CookieUtil.getValue(request, "ticket");
        if (ticket != null) {
			//通过ticket凭证查找登录信息
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
			// 查询凭证是否有效
            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {
				// 查询用户信息
                User user = userService.findUserById(loginTicket.getUserId());
				// 在本次请求中持有用户
				hostHolder.setUser(user);
            }
        }
        return true;
    }
	// 需要在模板引擎调用之前，加载用户的信息（如果已登录），因此选择在postHandle中处理，因为postHandle正好是在模板引擎加载之前执行的
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
		User user=hostHolder.getUser();//根据线程从hostholder中获取user对象
		if (user!=null&&modelAndView!=null){//空值处理
			modelAndView.addObject("loginUser",user);//传递给前端模板引擎。
		}
	}

	//在全部请求执行完之后，清理线程中的数据。
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		hostHolder.clear();
	}
}
