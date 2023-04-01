package com.xu.community.controller;

import com.xu.community.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
public class DataController {

	@Autowired
	private DataService dataService;

	/**
	 * 功能：跳转到统计页面
	 */
	@RequestMapping(path = "/data", method = {RequestMethod.GET, RequestMethod.POST})
	public String getDataPage() {
		return "/site/admin/data";
	}

	/**
	 * 功能：查询一段时间的用户访问量uv
	 */
	@RequestMapping(path = "/data/uv", method = RequestMethod.POST)
	public String getUV(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,//告诉服务器前端传递过来的日期格式，服务器才能自动转换成date
						@DateTimeFormat(pattern = "yyyy-MM-dd") Date end, Model model) {
		long uv = dataService.calculateUV(start, end);//查询该时间段的网站访问量
		model.addAttribute("uvResult", uv);
		model.addAttribute("uvStartDate", start);//方便页面默认显示时间
		model.addAttribute("uvEndDate", end);
		return "forward:/data";
	}

	// 统计活跃用户

	/**
	 * 功能：查询一段时间的活跃用户量dau
	 */
	@RequestMapping(path = "/data/dau", method = RequestMethod.POST)
	public String getDAU(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
						 @DateTimeFormat(pattern = "yyyy-MM-dd") Date end, Model model) {
		long dau = dataService.calculateDAU(start, end);
		model.addAttribute("dauResult", dau);
		model.addAttribute("dauStartDate", start);
		model.addAttribute("dauEndDate", end);
		return "forward:/data";
	}

}