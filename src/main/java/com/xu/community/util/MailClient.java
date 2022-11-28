package com.xu.community.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * 实现发送邮件的功能
 */
@Component
public class MailClient {
    private static final Logger logger = LoggerFactory.getLogger(MailClient.class);
    //实现发邮件的核心功能，次对象由springboot自动管理
    @Autowired
    private JavaMailSender mailSender;
    //声明发件人
    @Value("${spring.mail.username}")
    private String from;

    /**
     * 功能：给收件人发送邮件
     * 构建JavaMailSender中的MimeMessage，并用JavaMailSender中的send方法发送MimeMessage
     *
     * @param to：收件人邮箱
     * @param subject：邮件标题
     * @param content：邮件内容
     */
    public void sendMail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            //利用MimeMessageHelper创建邮件
            MimeMessageHelper helper=new MimeMessageHelper(message);
            //发件人
            helper.setFrom(from);
            //收件人
            helper.setTo(to);
            //邮件标题
            helper.setSubject(subject);
            //邮件内容
            //helper.setText(content,true);
            helper.setText(content,true);
            mailSender.send(helper.getMimeMessage());
        } catch (MessagingException e) {
            logger.error("发送邮件失败："+e.getMessage());
        }
    }
}

//
//@Component
//public class MailClient {
//
//    private static final Logger logger = LoggerFactory.getLogger(MailClient.class);
//
//    @Autowired
//    private JavaMailSender mailSender;
//
//    @Value("${spring.mail.username}")
//    private String from;
//
//    public void sendMail(String to, String subject, String content) {
//        try {
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message);
//            helper.setFrom(from);
//            helper.setTo(to);
//            helper.setSubject(subject);
//            helper.setText(content, true);
//            mailSender.send(helper.getMimeMessage());
//        } catch (MessagingException e) {
//            logger.error("发送邮件失败:" + e.getMessage());
//        }
//    }
//
//}
