package com.xu.community.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
//使用面向切面编程的方式统一处理异常
@Component
@Aspect//实现spring AOP面向切面的功能
public class ServiceLogAspect {
	private static final Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);

	/**
	 * 声明切入点，切入点就是切面方法要塞入的地方，简单来说就是需要进行切面处理的目标
	 * * com.xu.community.service.*.*(..))  代表  所有返回值，com.xu.community.service下的 所有类的 所有方法的 所有对象
	 */
	@Pointcut("execution(* com.xu.community.service.*.*(..))")
	public void pointcut() {
	}

	/**
	 * 声明（相对切入点）进行切面的位置
	 * @Before("切入点")：代表在切入点都西昂执行之前加入切面
	 */
	@Before("pointcut()")
	public void before(JoinPoint joinPoint) {
		// 用户ip[1.2.3.4],在[xxx],访问了[com.nowcoder.community.service.xxx()].
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();//在切面编程时，不能直接通过在方法中声明Request对象，因此需要通过RequestContextHolder获取request对象，获取后强制类型转换到ServletRequestAttributes
		if (attributes == null) {
			return;
		}
		HttpServletRequest request = attributes.getRequest();
		String ip = request.getRemoteHost();//获取ip
		String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());//获取时间
		String target = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();//通过连接点获取类名和方法名，获取方式固定。
		logger.info(String.format("用户[%s],在[%s],访问了[%s].", ip, now, target));
	}
}
