package com.xu.community.event;

import com.alibaba.fastjson.JSONObject;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.xu.community.entity.DiscussPost;
import com.xu.community.entity.Event;
import com.xu.community.entity.Message;
import com.xu.community.entity.User;
import com.xu.community.service.DiscussPostService;
import com.xu.community.service.ElasticsearchService;
import com.xu.community.service.MessageService;
import com.xu.community.util.CommunityConstant;
import com.xu.community.util.CommunityUtil;
import com.xu.community.util.HostHolder;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

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
	@Value("${wk.image.command}")
	private String wkImageCommand;

	@Value("${wk.image.storage}")
	private String wkImageStorage;

	@Value("${qiniu.key.access}")
	private String accessKey;

	@Value("${qiniu.key.secret}")
	private String secretKey;

	@Value("${qiniu.bucket.share.name}")
	private String shareBucketName;
	@Autowired
	private ThreadPoolTaskScheduler taskScheduler;//一个可以执行定时任务的线程池

	// @Autowired//+
	// private HostHolder hostHolder;//+
	/**
	 * 功能：处理kafka通知队列中的评论通知，点赞通知，关注通知。
	 * 同时处理kafka通知队列中的不同主题的通知内容。
	 */
	@KafkaListener(topics = {TOPIC_COMMENT, TOPIC_LIKE, TOPIC_FOLLOW})//声明处理的kafka主题
	public void handleCommentMessage(ConsumerRecord record) {
		// User user = hostHolder.getUser();
		// if (user!=null){
			if (record == null || record.value() == null) {//判断监听的内容是否为空
				logger.error("消息的内容为空!");
				return;
			}

			Event event = JSONObject.parseObject(record.value().toString(), Event.class);//把JSON字符串形式的数据解析为event对象
			if (event == null) {
				logger.error("消息格式错误!");
				return;
			}
			// if (event.getEntityUserId()!=user.getId()){//+  自己给自己操作没通知
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
			// }//+
		// }


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
	/**
	 * 功能：消费删帖事件,获取发帖事件中的帖子id，通过帖子id查询帖子，将帖子数据从elasticsearch中删除
 	 */
	@KafkaListener(topics = {TOPIC_DELETE})
	public void handleDeleteMessage(ConsumerRecord record) {
		if (record == null || record.value() == null) {
			logger.error("消息的内容为空!");
			return;
		}

		Event event = JSONObject.parseObject(record.value().toString(), Event.class);
		if (event == null) {
			logger.error("消息格式错误!");
			return;
		}

		elasticsearchService.deleteDiscussPost(event.getEntityId());
	}
	/**
	 * 功能：消费生成长图事件，生成并保存长图
	 * @param record
	 */
	@KafkaListener(topics = TOPIC_SHARE)
	public void handleShareMessage(ConsumerRecord record) {
		if (record == null || record.value() == null) {
			logger.error("消息的内容为空!");
			return;
		}

		Event event = JSONObject.parseObject(record.value().toString(), Event.class);
		if (event == null) {
			logger.error("消息格式错误!");
			return;
		}

		String htmlUrl = (String) event.getData().get("htmlUrl");
		String fileName = (String) event.getData().get("fileName");
		String suffix = (String) event.getData().get("suffix");

		String cmd = wkImageCommand + " --quality 75 "
				+ htmlUrl + " " + wkImageStorage + "/" + fileName + suffix;//利用各项数据，拼接wk的指令
		System.out.println(cmd);
		try {
			Runtime.getRuntime().exec(cmd);//执行命令
			logger.info("生成长图成功: " + cmd);
		} catch (IOException e) {
			logger.error("生成长图失败: " + e.getMessage());
		}

		// 启用定时器,监视该图片,一旦生成了,则上传至七牛云.
		UploadTask task = new UploadTask(fileName, suffix);//声明任务，用于配合定时器监听本地图片是否生成，生成了就上传七牛云
		Future future = taskScheduler.scheduleAtFixedRate(task, 500);//每隔500ms执行一遍task任务，通过在task内改变future的状态来停止定时器的执行，否则会一只定时执行下去
		task.setFuture(future);
	}

	class UploadTask implements Runnable {

		// 文件名称
		private String fileName;
		// 文件后缀
		private String suffix;
		// 启动任务的返回值，用来停止定时器
		private Future future;
		// 开始时间
		private long startTime;
		// 上传次数
		private int uploadTimes;

		// 有参构造器，强制调用的时候必须传入文件名和后缀
		public UploadTask(String fileName, String suffix) {
			this.fileName = fileName;
			this.suffix = suffix;
			this.startTime = System.currentTimeMillis();
		}

		public void setFuture(Future future) {
			this.future = future;
		}

		@Override
		public void run() {
			// 上传时间超过30秒，生成失败
			if (System.currentTimeMillis() - startTime > 30000) {
				logger.error("执行时间过长,终止任务:" + fileName);
				future.cancel(true);//停止定时器
				return;
			}
			// 上传次数太多，上传失败
			if (uploadTimes >= 3) {
				logger.error("上传次数过多,终止任务:" + fileName);
				future.cancel(true);//停止定时器
				return;
			}

			String path = wkImageStorage + "/" + fileName + suffix;//存储在本地的图片路径
			File file = new File(path);
			if (file.exists()) {//如果文件存在，则开始上传，不存在就等会
				logger.info(String.format("开始第%d次上传[%s].", ++uploadTimes, fileName));
				// 设置响应信息
				StringMap policy = new StringMap();
				policy.put("returnBody", CommunityUtil.getJSONString(0));//设置响应信息
				// 生成上传凭证
				Auth auth = Auth.create(accessKey, secretKey);
				String uploadToken = auth.uploadToken(shareBucketName, fileName, 3600, policy);//凭证3600秒过期
				// 指定上传到华北机房
				UploadManager manager = new UploadManager(new Configuration(Zone.zone1()));
				try {
					// 开始上传图片
					Response response = manager.put(
							path, fileName, uploadToken, null, "image/" + suffix, false);
					// 处理响应结果
					JSONObject json = JSONObject.parseObject(response.bodyString());
					if (json == null || json.get("code") == null || !json.get("code").toString().equals("0")) {
						logger.info(String.format("第%d次上传失败[%s].", uploadTimes, fileName));
					} else {
						logger.info(String.format("第%d次上传成功[%s].", uploadTimes, fileName));
						future.cancel(true);
					}
				} catch (QiniuException e) {
					logger.info(String.format("第%d次上传失败[%s].", uploadTimes, fileName));
				}
			} else {
				logger.info("等待图片生成[" + fileName + "].");
			}
		}
	}
}
