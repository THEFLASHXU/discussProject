package com.xu.community.config;


import com.xu.community.quartz.AlphaJob;
import com.xu.community.quartz.PostScoreRefreshJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

/**
 * 功能：配置springboot提供的支持分布式的线程池quartz
 * 核心组件：
 *     Scheduler 接口：核心调度工具，所有任务由这一接口调用
 *     Job：定义任务，重写 execute 方法
 *     JobDetail 接口：对 Job 的配置：名字、组、以及其它参数
 *     Trigger 接口：也是对 Job 的配置：什么时候运行，以什么样的频率运行
 *
 *  配置好以后，程序启动，Quartz 会读取配置信息，并且立刻存入数据库。以后就通过读取相应的表来执行任务，这时程序中的配置就不再使用，只是在第一次启动服务器的时候会用一下。
 */
@Configuration
public class QuartzConfig {

	// FactoryBean可简化Bean的实例化过程:
	// 1.通过FactoryBean封装Bean的实例化过程.
	// 2.将FactoryBean装配到Spring容器里.
	// 3.将FactoryBean注入给其他的Bean.
	// 4.该Bean得到的是FactoryBean所管理的对象实例.

	// 配置JobDetail：对 Job 的配置：名字、组、以及其它参数
	// @Bean
	public JobDetailFactoryBean alphaJobDetail() {
		JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
		factoryBean.setJobClass(AlphaJob.class);
		factoryBean.setName("alphaJob");//声明job的名字
		factoryBean.setGroup("alphaJobGroup");//声明job的组名
		factoryBean.setDurability(true);//声明任务是否是持久的保存。
		factoryBean.setRequestsRecovery(true);//任务是否可恢复
		return factoryBean;
	}

	// 配置Trigger(SimpleTriggerFactoryBean, CronTriggerFactoryBean)
	// @Bean
	public SimpleTriggerFactoryBean alphaTrigger(JobDetail alphaJobDetail) {
		SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
		factoryBean.setJobDetail(alphaJobDetail);
		factoryBean.setName("alphaTrigger");//声明trigger的名字
		factoryBean.setGroup("alphaTriggerGroup");//声明trigger的组名
		factoryBean.setRepeatInterval(3000);//执行任务的频率，每3000ms执行一次
		factoryBean.setJobDataMap(new JobDataMap());//用来存储job状态的对象。
		return factoryBean;
	}
	// 刷新帖子分数任务
	@Bean
	public JobDetailFactoryBean postScoreRefreshJobDetail() {
		JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
		factoryBean.setJobClass(PostScoreRefreshJob.class);
		factoryBean.setName("postScoreRefreshJob");
		factoryBean.setGroup("communityJobGroup");
		factoryBean.setDurability(true);
		factoryBean.setRequestsRecovery(true);
		return factoryBean;
	}

	@Bean
	public SimpleTriggerFactoryBean postScoreRefreshTrigger(JobDetail postScoreRefreshJobDetail) {
		SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
		factoryBean.setJobDetail(postScoreRefreshJobDetail);
		factoryBean.setName("postScoreRefreshTrigger");
		factoryBean.setGroup("communityTriggerGroup");
		factoryBean.setRepeatInterval(1000 * 60 * 5);//执行频率：5分钟执行一遍
		factoryBean.setJobDataMap(new JobDataMap());
		return factoryBean;
	}

}
