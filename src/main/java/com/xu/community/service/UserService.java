package com.xu.community.service;

import com.xu.community.dao.LoginTicketMapper;
import com.xu.community.dao.UserMapper;
import com.xu.community.entity.LoginTicket;
import com.xu.community.entity.User;
import com.xu.community.util.CommunityConstant;
import com.xu.community.util.CommunityUtil;
import com.xu.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 实现CommunityConstant是为了使用该接口中代表激活状态的成员变量
 */
@Service
public class UserService implements CommunityConstant {
    @Autowired
    private UserMapper userMapper;
    // 邮件工具
    @Autowired
    private MailClient mailClient;
    // 模板引擎
    @Autowired
    private TemplateEngine templateEngine;
    // 域名，通过${}的方式从配置文件中获取数据
    @Value("${community.path.domain}")
    private String doamin;
    // 项目名
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Autowired
    LoginTicketMapper loginTicketMapper;

    // 根据userId查找user对象
    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();
        // 空值处理
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        // 判断用户输入的数据本身有没有逻辑问题
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空");
            return map;
        }

        // 和数据库对比，判断用户的注册信息是否可以提交
        // 验证账号
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "该账号已存在");
            return map;
        }
        // 验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "该邮箱已被注册 ");
            return map;
        }

        // 注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));// 盐：使用工具类，生成一串随机字符，只取5位来使用
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));// 设置密码：使用用户输入的密码+盐，最终由md5加密后封装
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());// 设置随机数组成的激活码
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));// 使用牛客网头像库，用随机数在1001个头像中为用户生成初始头像
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // 激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // 激活链接格式：http://localhost:8080/community/activation/101/AO@#D9URJ7激活码
        String url = doamin + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号邮件", content);
        return map;
    }

    /**
     * 点击邮箱激活链接激活账号
     * 
     * @param userId
     * @param code
     * @return
     */
    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        // System.out.println("激活码："+user.getActivationCode());
        // System.out.println(code);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    /**
     * 验证用户登录信息，验证账号密码是否正确，根据用户是否保存登录信息而设置不同的超时时间。
     * @param username 用户名
     * @param password 密码
     * @param expiredSeconds 超时时间
     * @return
     */
    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();
        // 空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }
        // 验证账号
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "该账号不存在！");
            return map;
        }
        // 验证账号激活状态
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活！");
            return map;
        }
        // 验证密码是否正确
        password = CommunityUtil.md5(password + user.getSalt());// 将明文密码拼接salt后，用md5加密，和数据库中的加密形式密码进行比对
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码不正确！");
            return map;
        }
        // 走到这一步就说明上面的错误情况都不存在
        // 生成登陆凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());// 生成随机字符串作为ticket
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));//设置超时时间
        loginTicketMapper.insertLoginTicket(loginTicket);

        map.put("ticket",loginTicket.getTicket());//将ticket放入到map返回
        return map;
    }

    /**
     * 功能：退出登录
     * @param ticket 登陆凭证
     */
    public void logout(String ticket){
        loginTicketMapper.updateStatus(ticket,1);//根据ticket标识找到对应的用户，将他的登陆状态变成失效
    }

    /**
     * 功能：根据ticket值在login_ticket表单中查找一行数据
     */
    public LoginTicket findLoginTicket(String ticket){
        return loginTicketMapper.selectByTicket(ticket);
    }

    /**
     * 功能：更新用户头像
     */
    public int updateHeader(int userId,String headerUrl){
        return userMapper.updateHeader(userId,headerUrl);
    }



}



















