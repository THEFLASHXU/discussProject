package com.xu.community.controller;

import com.xu.community.entity.Message;
import com.xu.community.entity.Page;
import com.xu.community.entity.User;
import com.xu.community.service.MessageService;
import com.xu.community.service.UserService;
import com.xu.community.util.CommunityUtil;
import com.xu.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
public class MessageController {
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private MessageService messageService;
    @Autowired
    private UserService userService;

    /**
     * 功能：显示会话列表
     */
    @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
    public String getLetterList(Model model, Page page) {
        User user = hostHolder.getUser();
        // 配置分页信息
        page.setLimit(5);
        page.setStaticPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));//查询当前用户的会话数量

        // 会话列表
        List<Message> conversationList =
            messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> conversations = new ArrayList<>();
        if (conversationList != null) {
            for (Message message : conversationList) {
                // 遍历每一条会话
                Map<String, Object> map = new HashMap<>();
                map.put("conversation", message);
                map.put("letterCount", messageService.findLetterCount(message.getConversationId()));// 私信数量
                map.put("unreadCount", messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));// 未读私信数量
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();// 发信人和收信人至少有一个是对方，从中获取对方的id
                map.put("target", userService.findUserById(targetId));// 对方用户头像

                conversations.add(map);
            }
        }
        model.addAttribute("conversations", conversations);

        // 查询未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        // int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        // model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "/site/letter";
    }

    /**
     * 功能：显示某一会话中的所有私信，支持分页
     */
    @RequestMapping(path = "/letter/detail/{conversationId}", method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Page page, Model model) {
        // 分页信息
        page.setLimit(5);
        page.setStaticPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        // 私信列表
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> letters = new ArrayList<>();
        if (letterList != null) {
            // 因为每条都要伴随显示发送方的头像，名字。因此需要提取发送方的信息。
            for (Message message : letterList) {
                Map<String, Object> map = new HashMap<>();
                map.put("letter", message);
                map.put("fromUser", userService.findUserById(message.getFromId()));
                letters.add(map);
            }
        }
        model.addAttribute("letters", letters);

        // 私信目标
        model.addAttribute("target", getLetterTarget(conversationId));

        // 设置已读
        List<Integer> ids = getLetterIds(letterList);//获取未读私信id
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);//将未读私信改为已读
        }

        return "/site/letter-detail";
    }

    /**
     * 功能：获取一堆私信中当前用户未读的私信对象id
      */
    private List<Integer> getLetterIds(List<Message> letterList) {
        List<Integer> ids = new ArrayList<>();
        if (letterList != null) {
            for (Message message : letterList) {//遍历传入进来的私信列表
                if (hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0) {//如果收信方是当前用户，并且状态是未读。
                    ids.add(message.getId());//则记录此条私信的id
                }
            }
        }

        return ids;
    }

    /**
     * 功能：根据conversationId，识别会话的另一方用户id。
     * 先将conversationId根据横线划分成两个数，识别这两个数哪一个是当前用户的id，那么另一个数就是会话另一方用户的id
     * @param conversationId 会话id  格式:111_112
     */
    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if (hostHolder.getUser().getId() == id0) {
            return userService.findUserById(id1);
        } else {
            return userService.findUserById(id0);
        }
    }

    /**
     * 功能：发送私信
     */
    @RequestMapping(path = "/letter/send", method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName, String content) {
        // 根据用户名获取收件人用户信息
        User target = userService.findUserByName(toName);
        if (target == null) {
            return CommunityUtil.getJSONString(1, "目标用户不存在!");
        }

        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());// 发件人id
        message.setToId(target.getId());// 收件人id
        if (message.getFromId() < message.getToId()) {// 根据小id拼大id的逻辑，拼接成会话id
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        } else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        message.setContent(content);
        message.setCreateTime(new Date());
        messageService.addMessage(message);

        return CommunityUtil.getJSONString(0);// 返回代表正常发送的代码0.
    }
}
