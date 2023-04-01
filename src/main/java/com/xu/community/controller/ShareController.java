package com.xu.community.controller;

import com.xu.community.entity.Event;
import com.xu.community.event.EventProducer;
import com.xu.community.util.CommunityConstant;
import com.xu.community.util.CommunityUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 功能：生成网页长图。 由于生成长图会比较慢，所以采用异步请求，使用事件驱动，将任务生成事件，放进kafka。kafka后续异步处理。
 */
@Controller
public class ShareController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(ShareController.class);

    @Autowired
    private EventProducer eventProducer;

    @Value("${community.path.domain}")
    private String domain;// 域名

    @Value("${server.servlet.context-path}")
    private String contextPath;// 项目名

    @Value("${wk.image.storage}")
    private String wkImageStorage;// 长图存储路径

    @Value("${qiniu.bucket.share.url}")//七牛云存储分享图片的路径
    private String shareBucketUrl;

    @RequestMapping(path = "/share", method = RequestMethod.GET)
    @ResponseBody
    public String share(String htmlUrl) {
        // 生成长图的文件名随机
        String fileName = CommunityUtil.generateUUID();

        // 生成kafka事件，为了后续异步生成长图
        Event event = new Event()
				.setTopic(TOPIC_SHARE)
				.setData("htmlUrl", htmlUrl)
				.setData("fileName", fileName)
            	.setData("suffix", ".png");
        eventProducer.fireEvent(event);

        // 返回给用户生成的长图的七牛云访问路径
        Map<String, Object> map = new HashMap<>();
        // map.put("shareUrl", domain + contextPath + "/share/image/" + fileName);
        map.put("shareUrl", shareBucketUrl + "/" + fileName);

        return CommunityUtil.getJSONString(0, null, map);
    }

    // 废弃
    /**
     * 功能：获取用户生成的网页长图
     */
    @RequestMapping(path = "/share/image/{fileName}", method = RequestMethod.GET)
    public void getShareImage(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        if (StringUtils.isBlank(fileName)) {
            throw new IllegalArgumentException("文件名不能为空!");
        }

        response.setContentType("image/png");
        File file = new File(wkImageStorage + "/" + fileName + ".png");//获取存储的文件
        try {
            OutputStream os = response.getOutputStream();
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("获取长图失败: " + e.getMessage());
        }
    }

}
