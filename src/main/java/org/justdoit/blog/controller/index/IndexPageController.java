package org.justdoit.blog.controller.index;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.justdoit.blog.config.auth.SessionUser;
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
    public String index(HttpServletRequest request, Model model) {
        SessionUser session = getSessionUser(httpSession);
        if (session != null) {
            model.addAttribute("userEmail", session.getEmail());
            model.addAttribute("userRole", session.getRole());
            model.addAttribute("isUser", true);
            model.addAttribute("isGuest", false);
        } else {
            model.addAttribute("userRole", "GUEST");
            model.addAttribute("isUser", false);
            model.addAttribute("isGuest", true);
        }

        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

        if (csrfToken != null) {
            model.addAttribute("_csrf_token", csrfToken.getToken());
            model.addAttribute("_csrf_header", csrfToken.getHeaderName());
        }

        return "user/index";
    }

    @GetMapping("/user/login")
    public String login(HttpServletRequest request, Model model) {
        SessionUser session = getSessionUser(httpSession);
        if (session != null) {
            return "redirect:/";
        } else {
            model.addAttribute("role", "GUEST");
            model.addAttribute("isUser", false);
            model.addAttribute("isGuest", true);
        }

        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

        if (csrfToken != null) {
            model.addAttribute("_csrf_token", csrfToken.getToken());
            model.addAttribute("_csrf_header", csrfToken.getHeaderName());
        }
        return "user/login";
    }

    private SessionUser getSessionUser(HttpSession session) {
        return Optional
                .ofNullable((SessionUser) session.getAttribute("basicUser"))
                .orElse((SessionUser) session.getAttribute("user"));
    }
}
