package com.xu.community.dao.elasticsearch;

import com.xu.community.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * 功能：作为elasticsearch的数据访问层，把数据存储到elasticsearch中
 * 无需声明任何方法，只需要继承于spring提供的elasticsearch接口,需要声明好接口处理的实体类是谁，实体类的主键是什么类型。
 */
@Repository//声明这个接口是elasticsearch的数据访问层
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost, Integer> {

}
