package com.xu.community.controller;

import com.xu.community.entity.DiscussPost;
import com.xu.community.entity.Page;
import com.xu.community.entity.User;
import com.xu.community.service.DiscussPostService;
import com.xu.community.service.LikeService;
import com.xu.community.service.UserService;
import com.xu.community.util.CommunityConstant;
import com.xu.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController implements CommunityConstant {
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private UserService userService;
    @Autowired
    private LikeService likeService;

    /**
     * 方法功能：展示首页的帖子
     * path = "/index" 指的是访问路径，浏览器访问这个路径的时候就会调用这个函数。 method= RequestMethod.GET 指的是请求方式，查询
     * 此方法响应的是网页，所以不用写responseBody了 方法的返回值可以使ModelandView或者是String，如果用String，返回的就是视图的名字
     * @RequestParam(name="orderMode",defaultValue = "0") int orderMode  其中，orderMode代表排序方式，最初访问index页面时不会传回orderMode信息，因此为了防止报错，需要设置默认值
     */
    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public String getIndexPage(Model model, Page page,@RequestParam(name="orderMode",defaultValue = "0") int orderMode) {
        // 方法调用前，SpringMVC会自动实例化方法参数中的所有对象（这里是Model和Page），并将Page注入到Model
        // 所以，在thymeleaf中可以直接访问Page对象中的数据，而不需要手动的使用model.addAttribute();将page对象加入到Model中。
        page.setRows(discussPostService.findDiscussPostRows(0));// 计算总帖子数，userid=0时代表不以用户id来查询帖子
        page.setStaticPath("/index?orderMode="+orderMode);// 设置分页链接
        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit(),orderMode);
        // 如果只通过discussPostService.findDiscussPosts方法查找帖子，则只能找到DiscussPost对象，该对象中不包含用户的信息，但是包含用户的userId，因此利用userId在user表中查找user对象
        // List中存放着很多Map，每个map都代表着一个帖子对象和一个用户信息对象，多个map组成了一个list
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list!=null){
            for (DiscussPost post : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                // 利用帖子对象中的userId查找user对象，并传入到map中
                User user = userService.findUserById(post.getUserId());
                map.put("user", user);
                //查找当前帖子对象的点赞数量，并传入到map中
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);

                discussPosts.add(map);
            }
        }
        // 往model中装入数据，传递给前端，让前端访问model中的数据。
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("orderMode", orderMode);
        return "/index";
    }

    /**
     * 功能：跳转到错误信息展示页面
     */
    @RequestMapping(path = "/error", method = RequestMethod.GET)
    public String getErrorPage() {
        return "/error/500";
    }

    /**
     * 功能：检测到用户没有权限时，跳转到无权访问页面
     */
    @RequestMapping(path = "/denied", method = RequestMethod.GET)
    public String getDeniedPage() {
        return "/error/404";
    }


}
