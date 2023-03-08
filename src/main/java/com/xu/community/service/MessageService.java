package com.xu.community.service;

import com.xu.community.dao.MessageMapper;
import com.xu.community.entity.Message;
import com.xu.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class MessageService {
	@Autowired
	private MessageMapper messageMapper;
	@Autowired
	private SensitiveFilter sensitiveFilter;

	/**
	 * 功能：查询当前用户的会话列表，针对每个会话只返回一条最新的私信，支持分页设计
	 */
	public List<Message> findConversations(int userId, int offset, int limit) {
		return messageMapper.selectConversations(userId, offset, limit);
	}
	/**
	 * 功能：查询当前用户的会话数量
	 */
	public int findConversationCount(int userId) {
		return messageMapper.selectConversationCount(userId);
	}

	/**
	 * 功能：查询某个会话所包含的私信列表
	 */
	public List<Message> findLetters(String conversationId, int offset, int limit) {
		return messageMapper.selectLetters(conversationId, offset, limit);
	}

	/**
	 * 功能：查询某个会话所包含的私信数量
	 */
	public int findLetterCount(String conversationId) {
		return messageMapper.selectLetterCount(conversationId);
	}
	/**
	 * 功能：查询未读私信的数量
	 */
	public int findLetterUnreadCount(int userId, String conversationId) {
		return messageMapper.selectLetterUnreadCount(userId, conversationId);
	}

	/**
	 * 功能：新增一条消息
	 * @param message
	 * @return
	 */
	public int addMessage(Message message) {
		message.setContent(HtmlUtils.htmlEscape(message.getContent()));
		message.setContent(sensitiveFilter.filter(message.getContent()));
		return messageMapper.insertMessage(message);
	}

	/**
	 * 功能：将私信状态改为已读
	 * @param ids
	 * @return
	 */
	public int readMessage(List<Integer> ids) {
		return messageMapper.updateStatus(ids, 1);
	}
}
