package retwis.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import retwis.pojo.User;

import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Controller
public class HomeController {

    @GetMapping("/")
    public String root(HttpSession session) throws UnsupportedEncodingException {
        if (session.getAttribute("user") != null) {
            User user = (User) session.getAttribute("user");
            // 已登录
            return "redirect:/profile/" + URLEncoder.encode(user.getUsername(), "UTF-8");
        }
        return "signIn";
    }

    @GetMapping("/signIn.html")
    public String singInView(Model model, RedirectAttributes redirectAttributes) {
        redirectAttributes.getFlashAttributes().forEach(
                (k,v) -> model.addAttribute(k,v)
        );
        return "signIn";
    }
}
