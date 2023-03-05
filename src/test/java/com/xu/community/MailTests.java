package com.xu.community;

import com.xu.community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTests {
    @Autowired
    private MailClient mailClient;
    //在测试类中引入template模板
    @Autowired
    private TemplateEngine templateEngine;
    @Test
    public void testTextMail(){
        //mailClient.sendMail("1726367040@qq.com","TEST","welcome");
        mailClient.sendMail("xukaihang55555@live.com","TEST","welcome");
    }
    @Test
    public void testHtmlMail(){
        //传入模板的内容
        Context context=new Context();
        context.setVariable("username","sunday");
        //利用模板引擎，将内容发送给模板，content代表接受了内容的模板完全体
        String content=templateEngine.process("/mail/demo",context);

        mailClient.sendMail("xukaihang55555@live.com","html",content);
    }
}
