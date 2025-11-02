package org.justdoit.blog.controller.page;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.justdoit.blog.config.auth.SessionUser;
import org.springframework.stereotype.Controller;
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
    public String login(HttpSession httpSession) {
        SessionUser session = (SessionUser) httpSession.getAttribute("user");
        if (session != null) {
            return "redirect:/";
        }

        return "user/login";
    }

}
