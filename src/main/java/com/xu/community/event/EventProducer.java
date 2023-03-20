package com.xu.community.event;

import com.alibaba.fastjson.JSONObject;
import com.xu.community.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 功能：用kafka创建消息队列。构建生产者方法，向队列中发送数据
 */
@Component
public class EventProducer {

	@Autowired
	private KafkaTemplate kafkaTemplate;

	/**
	 * 功能：将事件信息发送到指定的主题
	 * @param event 事件对象
	 */
	public void fireEvent(Event event) {
		// 将事件对象转换成JSON字符串的形式发送到指定的主题。
		kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
	}

}