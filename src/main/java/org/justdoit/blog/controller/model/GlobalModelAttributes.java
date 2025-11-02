package org.justdoit.blog.controller.model;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.justdoit.blog.config.auth.SessionUser;
import org.justdoit.blog.template.Role;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributes {
    private final HttpSession httpSession;

    @ModelAttribute
    public void addGlobalAttributes(Model model, HttpServletRequest request) {
        SessionUser session = (SessionUser) httpSession.getAttribute("user");
        if (session != null) {
            model.addAttribute("email", session.getEmail());
            model.addAttribute("subEmail", session.getSubEmail());
            model.addAttribute("role", session.getRole());
            Role sessionRole = Role.fromKey(session.getRole());
            model.addAttribute("isManager", Role.MANAGER.equals(sessionRole));
            model.addAttribute("isUser", true);
            model.addAttribute("isGuest", false);
        } else {
            model.addAttribute("isManager", false);
            model.addAttribute("isUser", false);
            model.addAttribute("isGuest", true);
        }

        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            model.addAttribute("_csrf_token", csrfToken.getToken());
            model.addAttribute("_csrf_header", csrfToken.getHeaderName());
        }
    }
}
