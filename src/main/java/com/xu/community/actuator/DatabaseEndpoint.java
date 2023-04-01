package com.xu.community.actuator;


import com.xu.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 功能:自定义的端点,用于监控数据库,看看数据库是否正常
 */
@Component
@Endpoint(id = "database")//给端点取个名字,以后访问端点用
public class DatabaseEndpoint {

	private static final Logger logger = LoggerFactory.getLogger(DatabaseEndpoint.class);
	//尝试获取数据库链接,如果能获取到链接,就证明这个端点正常
	@Autowired
	private DataSource dataSource;//获取数据库连接池对象

	@ReadOperation//表示这个方法是一个get请求
	public String checkConnection() {
		try (
				Connection conn = dataSource.getConnection();//尝试获取连接
		) {
			return CommunityUtil.getJSONString(0, "获取连接成功!");
		} catch (SQLException e) {
			logger.error("获取连接失败:" + e.getMessage());
			return CommunityUtil.getJSONString(1, "获取连接失败!");
		}
	}

}
