package com.xu.community.controller;

import com.xu.community.util.CommunityUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.net.http.HttpResponse;

@Controller
@RequestMapping("/alpha")
public class AlphaController {
    @ResponseBody
    @RequestMapping("/hello")
    public String sayHello() {
        return "say hello";
    }

    @RequestMapping(path = "/cookie/set", method = RequestMethod.GET)
    @ResponseBody
    public String setCookie(HttpServletResponse response){//cookie必须由HttpServletResponse传递
        //创建cookie
        Cookie cookie=new Cookie("code", CommunityUtil.generateUUID());
        //设置cookie的生效范围(在该路径之下生效)
        cookie.setPath("/community/alpha");
        //设置cookie的生存时间
        cookie.setMaxAge(60 * 10);
        //发送cookie
        response.addCookie(cookie);//把cookie放进response中才能进行传递
        return "set cookies";
    }

}
