package retwis.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import retwis.pojo.Post;
import retwis.pojo.Range;
import retwis.pojo.User;
import retwis.service.UserService;

import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


@Slf4j
@Controller
public class UserController {

    private UserService userService;

    private static final String ERROR_MSG = "errorMsg";

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }


    /**
     * 个人信息页
     * @param username
     * @param page
     * @param model
     * @return
     */
    @GetMapping("/profile/{name}")
    public String getProfile(@PathVariable("name") String username,
                             @RequestParam(required = false, defaultValue = "1") Integer page,
                             Model model,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        if (!userService.checkValid(username)) {
            // 用户不存在!
            return "redirect:/timeline";
        }
        // 获取信息
        model.addAttribute("userP", userService.getUser(username));
        // 帖子分页
        page = (page != null ? Math.abs(page) : 1);
        Range range = new Range(page);
        model.addAttribute("morePosts", userService.hasMorePosts(username, range));
        model.addAttribute("posts", userService.getPosts(username, range));
        model.addAttribute("hasPrev", page != range.prev(page));
        model.addAttribute("next", range.next(page));
        model.addAttribute("prev", range.prev(page));
        // 是否有重定向信息
        redirectAttributes.getFlashAttributes().forEach((k,v)->{
            model.addAttribute(k,v);
        });
        // 获取登录用户
        User user = handleUserSession(session);
        // 访问自己,还是访问他人
        if (!checkUser(username, user)) { // 不是当前用户
            model.addAttribute("followed", userService.isFollowed(username,
                    user != null ? user.getUsername() : null));
            return "profile";
        }
        // 是当前用户,返回个人主页
        return "home";
    }

    /**
     * 关注
     * @param username
     * @param session
     * @return
     */
    @GetMapping("/follow/{name}")
    public String follow(@PathVariable("name") String username,
                         HttpSession session,
                         Model model,
                         RedirectAttributes redirectAttributes) throws UnsupportedEncodingException {
        if (!userService.checkValid(username))
            return "redirect:/";
        User user = handleUserSession(session);
        if (user == null) {
            redirectAttributes.addFlashAttribute(ERROR_MSG, "你还未登录");
            return "redirect:/profile/" + URLEncoder.encode(username, "UTF-8");
        }
        userService.follow(username, user.getUsername());
        return "redirect:/profile/" + URLEncoder.encode(username, "UTF-8");
    }

    /**
     * 发帖
     * @param content
     * @param model
     * @param session
     * @return
     */
    @PostMapping("/post")
    public String publishPost(String content,
                              Model model,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) throws UnsupportedEncodingException {
        if (!StringUtils.hasText(content)) {
            redirectAttributes.addFlashAttribute(ERROR_MSG, "内容不能为空");
            return "redirect:/profile/" + URLEncoder.encode(handleUserSession(session).getUsername(), "UTF-8");
        }
        User user = handleUserSession(session);
        Post post = new Post();
        post.setUid(userService.findUid(user.getUsername()));
        post.setUsername(user.getUsername());
        post.setPublishTime(System.currentTimeMillis());
        post.setContent(content);
        userService.addPost(post);
        return "redirect:/profile/" + URLEncoder.encode(user.getUsername(), "UTF-8");
    }


    /**
     * 停止关注
     * @param username
     * @param session
     * @return
     */
    @GetMapping("/stopFollowing/{name}")
    public String stopFollowing(@PathVariable("name") String username,
                                HttpSession session,
                                Model model, RedirectAttributes redirectAttributes)
            throws UnsupportedEncodingException {
        if (!userService.checkValid(username))
            return "redirect:/";
        User user = handleUserSession(session);
        if (user == null) {
            redirectAttributes.addFlashAttribute(ERROR_MSG, "你还未登录");
            return "redirect:/profile/" + URLEncoder.encode(username, "UTF-8");
        }
        userService.stopFollowing(username, user.getUsername());
        return "redirect:/profile/" + URLEncoder.encode(username, "UTF-8");
    }

    /**
     * 时间线广场
     * @param page
     * @param model
     * @param session
     * @return
     */
    @GetMapping("/timeline")
    public String timeline(@RequestParam(required = false, defaultValue = "1") Integer page,
                           Model model,
                           HttpSession session) {
        // 分页
        page = page <= 0 ? 1 : page;
        Range range = new Range(page, 50);
        model.addAttribute("next", range.next(page));
        model.addAttribute("prev", range.prev(page));
        model.addAttribute("hasMore", range.getEnd() < range.getTotal() - 1);
        model.addAttribute("hasPrev", page != 1);
        model.addAttribute("posts", userService.findPosts(range));
        // 获取注册的用户
        model.addAttribute("users", userService.findUsers(10));
        return "timeline";
    }

    /**
     * 检查是否为登录用户
     * @param name
     * @param user
     * @return
     */
    private boolean checkUser(String name, User user) {
        if (user == null)
            return false;
        if (ObjectUtils.nullSafeEquals(name, user.getUsername()))
            return true;
        return false;
    }

    private User handleUserSession(HttpSession session) {
        return  (User) session.getAttribute("user");
    }
}
