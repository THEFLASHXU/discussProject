package com.xu.community.event;

import com.alibaba.fastjson.JSONObject;
import com.xu.community.entity.DiscussPost;
import com.xu.community.entity.Event;
import com.xu.community.entity.Message;
import com.xu.community.service.DiscussPostService;
import com.xu.community.service.ElasticsearchService;
import com.xu.community.service.MessageService;
import com.xu.community.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * kafka消息队列的消费者。消费者设置完毕后就会一直对目标topic进行监听，无需显式调用
 */
@Component
public class EventConsumer implements CommunityConstant {

	private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

	@Autowired
	private MessageService messageService;
	@Autowired
	private DiscussPostService discussPostService;
	@Autowired
	private ElasticsearchService elasticsearchService;

	/**
	 * 功能：处理kafka通知队列中的评论通知，点赞通知，关注通知。
	 * 同时处理kafka通知队列中的不同主题的通知内容。
	 */
	@KafkaListener(topics = {TOPIC_COMMENT, TOPIC_LIKE, TOPIC_FOLLOW})//声明处理的kafka主题
	public void handleCommentMessage(ConsumerRecord record) {
		if (record == null || record.value() == null) {//判断监听的内容是否为空
			logger.error("消息的内容为空!");
			return;
		}

		Event event = JSONObject.parseObject(record.value().toString(), Event.class);//把JSON字符串形式的数据解析为event对象
		if (event == null) {
			logger.error("消息格式错误!");
			return;
		}

		// 发送站内通知
		Message message = new Message();
		message.setFromId(SYSTEM_USER_ID);//消息发送者id，发送者为系统
		message.setToId(event.getEntityUserId());//消息接收者id
		message.setConversationId(event.getTopic());//会话id，是由fromId和toId拼接的，拼接逻辑是小id-大id。但是系统通知的fronid永远都是1，系统通知如果继续用数字拼接的方式就没意思了，因此系统通知的ConversationId采用通知类型进行存储。比如"comment""like""follow"
		message.setCreateTime(new Date());

		//系统通知的内容和用户私信的内容不同。用户私信的内容是私信文本，而系统通知的形式是一个包含了多种信息的额json字符串。{"entityType":2,"entityId":66,"postId":270,"userId":138}
		Map<String, Object> content = new HashMap<>();
		content.put("userId", event.getUserId());
		content.put("entityType", event.getEntityType());
		content.put("entityId", event.getEntityId());

		if (!event.getData().isEmpty()) {//把event里的map形式的data对象取出来
			for (Map.Entry<String, Object> entry : event.getData().entrySet()) {//遍历map中的每一个key-value
				content.put(entry.getKey(), entry.getValue());
			}
		}

		message.setContent(JSONObject.toJSONString(content));//把内容转换成json字符串保存
		messageService.addMessage(message);
	}

	/**
	 * 功能：消费发帖事件，获取发帖事件中的帖子id，通过帖子id查询帖子，将帖子数据存储到elasticsearch
 	 */

	@KafkaListener(topics = {TOPIC_PUBLISH})
	public void handlePublishMessage(ConsumerRecord record) {
		if (record == null || record.value() == null) {
			logger.error("消息的内容为空!");
			return;
		}

		Event event = JSONObject.parseObject(record.value().toString(), Event.class);
		if (event == null) {
			logger.error("消息格式错误!");
			return;
		}

		DiscussPost post = discussPostService.findDiscussPostById(event.getEntityId());
		elasticsearchService.saveDiscussPost(post);
	}


}
