package com.xu.community.service;

import com.xu.community.dao.elasticsearch.DiscussPostRepository;
import com.xu.community.entity.DiscussPost;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ElasticsearchService {

	@Autowired
	private DiscussPostRepository discussRepository;

	@Autowired
	private ElasticsearchTemplate elasticTemplate;

	/**
	 * 功能：保存数据到elasticsearch
	 */
	public void saveDiscussPost(DiscussPost post) {
		discussRepository.save(post);
	}

	/**
	 * 功能：删除elasticsearch的数据
	 */
	public void deleteDiscussPost(int id) {
		discussRepository.deleteById(id);
	}

	/**
	 * 功能：在elasticsearch中搜索信息，查询时拥有分页查询的逻辑，keyword：查询的关键字，current：在第几页查询，limit：每页多少条数据
	 */
	public Page<DiscussPost> searchDiscussPost(String keyword, int current, int limit) {
		SearchQuery searchQuery = new NativeSearchQueryBuilder()
				.withQuery(QueryBuilders.multiMatchQuery(keyword, "title", "content"))//在哪些词条中搜索
				.withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))//搜索结果的排序方式
				.withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
				.withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
				.withPageable(PageRequest.of(current, limit))//搜索的范围
				.withHighlightFields(//搜索的高亮设置
						new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
						new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
				).build();//这样查出来的数据是隐藏高亮信息的，需要再次处理才能展示高亮信息
		//将隐藏的高亮信息挖掘出来并且展示
		return elasticTemplate.queryForPage(searchQuery, DiscussPost.class, new SearchResultMapper() {
			@Override
			public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> aClass, Pageable pageable) {
				SearchHits hits = response.getHits();//hits是搜索结果
				if (hits.getTotalHits() <= 0) {//查询是否有搜索结果
					return null;
				}

				List<DiscussPost> list = new ArrayList<>();
				for (SearchHit hit : hits) {//遍历搜索结果
					DiscussPost post = new DiscussPost();
					//搜索结果中的数据是以map形式保存的，将数据提取出来保存在相应实体中
					String id = hit.getSourceAsMap().get("id").toString();
					post.setId(Integer.valueOf(id));

					String userId = hit.getSourceAsMap().get("userId").toString();
					post.setUserId(Integer.valueOf(userId));

					String title = hit.getSourceAsMap().get("title").toString();
					post.setTitle(title);

					String content = hit.getSourceAsMap().get("content").toString();
					post.setContent(content);

					String status = hit.getSourceAsMap().get("status").toString();
					post.setStatus(Integer.valueOf(status));

					String createTime = hit.getSourceAsMap().get("createTime").toString();
					post.setCreateTime(new Date(Long.valueOf(createTime)));

					String commentCount = hit.getSourceAsMap().get("commentCount").toString();
					post.setCommentCount(Integer.valueOf(commentCount));

					// 处理高亮显示的结果
					HighlightField titleField = hit.getHighlightFields().get("title");
					if (titleField != null) {
						post.setTitle(titleField.getFragments()[0].toString());
					}

					HighlightField contentField = hit.getHighlightFields().get("content");
					if (contentField != null) {
						post.setContent(contentField.getFragments()[0].toString());
					}

					list.add(post);
				}

				return new AggregatedPageImpl(list, pageable,
						hits.getTotalHits(), response.getAggregations(), response.getScrollId(), hits.getMaxScore());
			}
		});
	}

}