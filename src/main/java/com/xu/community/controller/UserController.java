package com.xu.community.controller;

import com.xu.community.annotation.LoginRequired;
import com.xu.community.entity.User;
import com.xu.community.service.UserService;
import com.xu.community.util.CommunityConstant;
import com.xu.community.util.CommunityUtil;
import com.xu.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {
    // 配置logger
    public static final Logger logger = LoggerFactory.getLogger(UserController.class);
    // 头像图片存储路径
    @Value("${community.path.upload}")
    private String uploadPath;
    // 域名
    @Value("${community.path.domain}")
    private String domain;
    // 项目名
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Autowired
    private UserService userService;
    @Autowired
    private HostHolder hostHolder;

    /**
     * 功能：跳转到用户信息设置的页面
     */
	@LoginRequired//自定义注解，代表只有登录的用户才能访问这个方法
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "/site/setting";
    }

	/**
	 * 功能：将用户上传的头像图片存储在服务器特定位置
	 * 获取上传的图片，请求方式必须是post，springmvc提供了MultipartFile类来存放上传的文件。
	 */
	@LoginRequired
    @RequestMapping(path = "/upload",method = RequestMethod.POST)
	public String uploadHeader(MultipartFile headerImage, Model model){
		if (headerImage==null){
			model.addAttribute("error","您还没有选择图片！");
			return "/site/setting";
		}
		// 获取文件名字
		String fileName=headerImage.getOriginalFilename();
		//获取文件的文件类型（后缀名）
		String suffix=fileName.substring(fileName.lastIndexOf("."));

		if (StringUtils.isBlank(suffix)){
			model.addAttribute("error","文件的格式不正确！");
			return "/site/setting";
		}
		//设置最终要存储的文件名字，由随机字符串+文件后缀名组成
		fileName= CommunityUtil.generateUUID()+suffix;
		//设置文件的存储路径
		File dest = new File(uploadPath + "/" + fileName);
		try {
			// 存储文件
			headerImage.transferTo(dest);
		} catch (IOException e) {
			logger.error("上传文件失败"+e.getMessage());
			throw new RuntimeException("上传文件失败，服务器发生异常！",e);
		}

		//更新当前用户的头像路径(web访问路径  如：http://localhost:8080/community/user/header/xxx.png)
		User user=hostHolder.getUser();
		String headUrl=domain+contextPath+"/user/header/"+fileName;
		userService.updateHeader(user.getId(),headUrl);
		return "redirect:/index";

	}

	/**
	 * 功能：浏览器访问头像时，显示头像图片。
	 */
	@RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
	public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
		// 服务器存放路径
		fileName=uploadPath+"/"+fileName;
	    //文件后缀,为了告诉response对象返回的图片类型是什么
		String suffix = fileName.substring(fileName.lastIndexOf("."));
		// 响应图片
		response.setContentType("image/" + suffix);
		//放到try小括号里的值会自动在程序运行结束后销毁
		try (
				FileInputStream fis = new FileInputStream(fileName);
				OutputStream os = response.getOutputStream();
		) {
			byte[] buffer = new byte[1024];
			int b = 0;
			while ((b = fis.read(buffer)) != -1) {
				os.write(buffer, 0, b);
			}
		} catch (IOException e) {
			logger.error("读取头像失败: " + e.getMessage());
		}

	}
}