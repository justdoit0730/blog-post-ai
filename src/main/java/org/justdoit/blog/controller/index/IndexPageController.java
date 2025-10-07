package org.justdoit.blog.controller.index;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.justdoit.blog.config.auth.SessionUser;
import org.justdoit.blog.entity.user.Role;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

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
