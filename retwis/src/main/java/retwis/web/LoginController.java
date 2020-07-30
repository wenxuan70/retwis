package retwis.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import retwis.pojo.User;
import retwis.service.UserService;

import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;



@Slf4j
@Controller
@SessionAttributes(names = "user", types = User.class)
public class LoginController {

    private UserService userService;

    private static final String SING_UP_ERROR_MSG = "signUpErrorMsg";
    private static final String SING_IN_ERROR_MSG = "signInErrorMsg";
    private static final String SING_UP_SUCCESS_MSG = "signUpSuccessMsg";

    private static final String USERNAME_VALID = "\\w{3,16}";
    private static final String PASSWORD_VALID = "[A-Za-z0-9_.]{6,16}";

    @Autowired
    public LoginController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 登录
     * @param username
     * @param password
     * @param model
     * @return
     */
    @PostMapping("/signIn")
    public String signIn(@RequestParam(required = false) String username,
                         @RequestParam(required = false) String password,
                         Model model,
                         RedirectAttributes redirectAttributes)
            throws UnsupportedEncodingException {
        User user = (User) model.getAttribute("user");
        if (user != null) {
            // 已登录
            return "redirect:/profile/" +
                    URLEncoder.encode(user.getUsername(), "UTF-8");
        }
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            redirectAttributes.addFlashAttribute(SING_IN_ERROR_MSG,"用户名或密码不能为空");
            return "redirect:/signIn.html";
        }
        if (!username.matches(USERNAME_VALID) || !password.matches(PASSWORD_VALID)) {
            redirectAttributes.addFlashAttribute(SING_IN_ERROR_MSG, "用户名或密码格式错误");
            return "redirect:/signIn.html";
        }
        // 验证用户名和密码
        if (userService.auth(username, password)) {
            // 添加用户到session
            model.addAttribute("user", new User(
                    userService.findUid(username),
                    username,
                    password));
            // 跳转到个人信息页
            return "redirect:/profile/" +
                    URLEncoder.encode(username, "UTF-8");
        } else if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
            redirectAttributes.addFlashAttribute(SING_IN_ERROR_MSG,"用户名或密码错误");
        }
        // 验证失败,返回登录页
        return "redirect:/signIn.html";
    }

    @PostMapping("/signUp")
    public String singUp(String username, String password, String password2,
                         RedirectAttributes redirectAttributes) {
        if (!StringUtils.hasText(username)
                || !StringUtils.hasText(password)
                || !StringUtils.hasText(password2)) {
            redirectAttributes.addFlashAttribute(SING_UP_ERROR_MSG, "用户名或密码不能为空");
            return "redirect:/signIn.html";
        }
        if (!ObjectUtils.nullSafeEquals(password, password2)) {
            redirectAttributes.addFlashAttribute(SING_UP_ERROR_MSG, "两次输入的密码不一致");
            return "redirect:/signIn.html";
        }
        if (username.length() < 3 || username.length() > 16 || !username.matches(USERNAME_VALID)) {
            redirectAttributes.addFlashAttribute(SING_UP_ERROR_MSG, "用户名长度必须3-16之间,且只能由数字,字母和下划线");
            return "redirect:/signIn.html";
        }
        if (password.length() < 6 || password.length() > 16 || !password.matches(PASSWORD_VALID)) {
            redirectAttributes.addFlashAttribute(SING_UP_ERROR_MSG, "密码长度必须在6-16之间,且只能由数字,字母,_和.组成");
            return "redirect:/signIn.html";
        }
        if (userService.checkValid(username)) {
            redirectAttributes.addFlashAttribute(SING_UP_ERROR_MSG, "用户名重复");
            return "redirect:/signIn.html";
        }
        // 注册
        userService.regUser(username, password);
        redirectAttributes.addFlashAttribute(SING_UP_SUCCESS_MSG, "注册成功!");
        // 跳转到登录页
        return "redirect:/signIn.html";
    }

    /**
     * 退出登录
     * @return
     */
    @GetMapping("/logout")
    public String logout(SessionStatus status,
                         HttpSession session,
                         Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            model.addAttribute(SING_IN_ERROR_MSG, "你还未登录");
            return "signIn";
        }
        // 删除session
        status.setComplete();
        return "redirect:/";
    }

}
