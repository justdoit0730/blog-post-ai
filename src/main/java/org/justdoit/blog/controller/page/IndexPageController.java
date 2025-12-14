package org.justdoit.blog.controller.page;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.justdoit.blog.config.auth.SessionUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@RequiredArgsConstructor
@Controller
public class IndexPageController {
    private final HttpSession httpSession;

    @GetMapping("/")
    public String index() {

        return "user/index";
    }

    @GetMapping("/user/login")
    public String login(HttpServletRequest request, HttpSession httpSession, Model model) {
        String rememberedEmail = "";
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("REMEMBERED_EMAIL".equals(cookie.getName())) {
                    rememberedEmail = cookie.getValue();
                    break;
                }
            }
        }
        model.addAttribute("rememberedEmail", rememberedEmail);
        model.addAttribute("rememberedEmailCheck", !rememberedEmail.isEmpty());

        SessionUser session = (SessionUser) httpSession.getAttribute("user");
        if (session != null) {
            return "redirect:/";
        }

        return "user/login";
    }

}
