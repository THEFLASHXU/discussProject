package com.xu.community.dao;

import com.xu.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Service;

/**
 * @mapper注解可以让接口方法映射到sql语句
 * sql语句可以单独在xml文件中书写，也可以通过注解书写，本次练习通过注解书写
 */
@Mapper
public interface LoginTicketMapper {
    // 插入一条登陆数据
    @Insert({"insert into login_ticket(user_id,ticket,status,expired) ",
        "values(#{userId},#{ticket},#{status},#{expired}) "}) // values中的#{}代表引用形参的值
    @Options(useGeneratedKeys = true, keyProperty = "id") // 指定主键自动增长，并且告诉程序谁是主键
    int insertLoginTicket(LoginTicket loginTicket);

    // 改变登陆数据(通过改变status的值实现删除或激活用户登录状态)
	@Update({
			"update login_ticket ",
			"set status=#{status} ",
			"where ticket=#{ticket} "
	})
    int updateStatus(String ticket, int status);

    // 查询登陆数据(通过ticket字段进行查询，因为ticket字段是唯一凭证)
	@Select({
			"select id,user_id,ticket,status,expired ",
			"from login_ticket ",
			"where ticket=#{ticket} "
	})
    LoginTicket selectByTicket(String ticket);
}
