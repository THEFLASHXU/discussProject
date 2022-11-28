package com.xu.community.dao;

import com.xu.community.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper//让spring容器装配这个bean
public interface UserMapper {
    //根据id查询用户
    User selectById(int id);
    //根据用户名查询用户
    User selectByName(String username);
    //根据邮箱查询用户
    User selectByEmail(String email);
    //增加用户，返回一个整数，表示增加数据的行数
    int insertUser(User user);
    //修改用户数据状态，，返回修改的数据行数
    int updateStatus(int id, int status);
    //更新用户头像
    int updateHeader(int id, String headerUrl);
    //更新用户密码
    int updatePassword(int id, String password);
}
