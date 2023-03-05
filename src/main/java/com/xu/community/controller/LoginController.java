package com.xu.community.controller;

import com.google.code.kaptcha.Producer;
import com.xu.community.entity.User;
import com.xu.community.service.UserService;
import com.xu.community.util.CommunityConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.print.MultiDoc;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

// 实现CommunityConstant接口是为了使用该接口中代表激活状态的成员变量
@Controller
public class LoginController implements CommunityConstant {
    //将项目的名字注入给变量，注解实现将application.properties文件中的值注入到这里。
    @Value("${server.servlet.context-path}")
    private String contextPath;
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    @Autowired
    private Producer kaptchaProducer;
    @Autowired
    UserService userService;

    /**
     * 功能：用户访问注册功能的路径后，向用户返回一个注册页面的模板路径
     * 
     * @return
     */
    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    /**
     * 功能：向用户返回一个登录页面的模板路径
     * 
     * @return
     */
    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }

    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) {// 用户注册信息未出错
            model.addAttribute("msg", "我们已经向你的邮箱发送了一封激活邮件，请尽快激活");
            model.addAttribute("target", "/index");
            return "/site/operate-result";// 页面跳转
        } else {// 用户注册信息有错误,则将错误信息发送回页面，错误情况有很多种可能，具体可能性分析不在这里处理，只需要把错误信息全部返回
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";// 页面跳转
        }
    }

    /**
     * 处理激活邮件，将请求url中的对应位置参数封装为userId和code @PathVariable("userId") int userId 的意思是：将路径中封装成userId的内容以int形式存储在userId中。
     * 
     * @param model
     * @param userId
     * @param code
     * @return
     */
    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        int result = userService.activation(userId, code);
        // System.out.println("result:"+result);
        if (result == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "激活成功，您的帐号现在可以正常使用了！");
            model.addAttribute("target", "/login");// 设置跳转界面路径
        } else if (result == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "无效操作，该账号已经激活过了");
            model.addAttribute("target", "/index");// 设置跳转界面路径
        } else {
            model.addAttribute("msg", "激活失败，您提供的激活密码不正确！");
            model.addAttribute("target", "/index");// 设置跳转界面路径
        }
        return "/site/operate-result";
    }

    /**
     * 功能：生成验证码
     */
    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response, HttpSession session) {// 为了保证安全性， 要使用session在服务器端存储验证码信息
        // 生成验证码
        String text = kaptchaProducer.createText();// 利用kaptchaProducer中之前的配置策略，生成验证码字符
        BufferedImage image = kaptchaProducer.createImage(text);// 根据验证码字符生成验证码图片
        // 将验证码（文本数据）存入session
        session.setAttribute("kaptcha", text);
        // 将验证码（图片数据）输出给浏览器,设置返回信息的类型，告诉浏览器返回的是图片，png形式的图片
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);// 把image对象,以png格式,用outputstream输出
        } catch (IOException e) {
            logger.error("响应验证码失败:" + e.getMessage());
        }
    }

    /**
     * 功能：处理登录请求
     * @param username
     * @param password
     * @param code 验证码
     * @param remenberme 是否保存登录信息
     * 方法中的形参，比如username,password,code，都是存在于Requset对象中的，前端可以通过request对象访问这些参数。
     * 除此之外，springMVC也会将这些参数存储，但是非原生类型的数据，比如用户自创的类Student，则会存储到model中，前端需要访问model来获取这类对象的数值。
     */
    @RequestMapping(path = "/login",method = RequestMethod.POST)
    public String login(String username,String password,String code,boolean remenberme,
                        Model model,HttpSession session,HttpServletResponse response){
        //检查验证码
        String kaptcha= (String) session.getAttribute("kaptcha");
        //如果验证码不正确，就跳转回登陆页面
        if (StringUtils.isBlank(kaptcha)||StringUtils.isBlank(code)||!kaptcha.equalsIgnoreCase(code)){
            model.addAttribute("codeMsg","验证码不正确！");
            return "/site/login";
        }
        // 检查账号密码,将数据传送给userservice进行判断，根据返回的内容就可以判断账号密码是否正确
        int expiredSeconds=remenberme?REMEMBER_EXPIRED_SECONDS:DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")){//如果返回了ticket字段证明登录信息没有错误
            //用cookie保存登录信息，将ticket字段输入给cookie
            Cookie cookie=new Cookie("ticket",map.get("ticket").toString());
            cookie.setPath(contextPath);//设置cookie的有效路径
            cookie.setMaxAge(expiredSeconds);//设置cookie的生效时间长度
            response.addCookie(cookie);
            return "redirect:/index";
        }else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));//返回用户名错误信息
            model.addAttribute("passwordMsg",map.get("passwordMsg"));//返回密码错误信息
            return "/site/login";
        }

    }

    @RequestMapping(path = "/logout",method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket){//@CookieValue("ticket") String ticket 代表把名称为“ticket”的cookie值拿出来，传给ticket
        userService.logout(ticket);
        return "redirect:/login";//有两个login的请求处理函数，一个是GET,一个是POST，默认传给GET请求。
    }

}
