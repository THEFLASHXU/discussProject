package com.xu.community.controller;

import com.xu.community.entity.DiscussPost;
import com.xu.community.entity.Page;
import com.xu.community.service.ElasticsearchService;
import com.xu.community.service.LikeService;
import com.xu.community.service.UserService;
import com.xu.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {
	@Autowired
	private ElasticsearchService elasticsearchService;

	@Autowired
	private UserService userService;

	@Autowired
	private LikeService likeService;


	/**
	 * 功能：处理对帖子信息的搜索请求。请求形式：search?keyword=xxx
	 * @param keyword 搜索关键词。由url自动注入
	 */
	@RequestMapping(path = "/search", method = RequestMethod.GET)
	public String search(String keyword, Page page, Model model) {
		// 搜索帖子
		org.springframework.data.domain.Page<DiscussPost> searchResult =
				elasticsearchService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());
		// 分页信息
		page.setStaticPath("/search?keyword=" + keyword);
		page.setRows(searchResult == null ? 0 : (int) searchResult.getTotalElements());
		// 聚合数据，生成最重要返回的结果
		List<Map<String, Object>> discussPosts = new ArrayList<>();
		if (searchResult != null) {
			for (DiscussPost post : searchResult) {
				Map<String, Object> map = new HashMap<>();
				// 帖子
				map.put("post", post);
				// 作者
				map.put("user", userService.findUserById(post.getUserId()));
				// 点赞数量
				map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));

				discussPosts.add(map);
			}
		}
		model.addAttribute("discussPosts", discussPosts);
		model.addAttribute("keyword", keyword);


		return "/site/search";
	}
}
