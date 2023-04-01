package com.xu.community.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;

@Configuration
public class WkConfig {

	private static final Logger logger = LoggerFactory.getLogger(WkConfig.class);

	@Value("${wk.image.storage}")
	private String wkImageStorage;

	/**
	 * 功能：在服务启动时，创建存长图的文件夹。
	 */
	@PostConstruct//表示该方法是初始化方法
	public void init() {
		// 创建WK图片目录
		File file = new File(wkImageStorage);
		if (!file.exists()) {
			file.mkdir();//如果不存在，就创建路径
			logger.info("创建WK图片目录: " + wkImageStorage);
		}
	}

}