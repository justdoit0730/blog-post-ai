package org.justdoit.blog.controller.manager;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.justdoit.blog.config.auth.SessionUser;
import org.justdoit.blog.entity.user.Role;
import org.justdoit.blog.variable.GlobalVariables;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@RequiredArgsConstructor
@Controller
@RequestMapping("/manager")
public class ManagerPageController {
    private final HttpSession httpSession;
    private final GlobalVariables globalVariables;

    // Naver API setting
    @GetMapping("/postingSetting")
    public String naverPostingSetting(HttpServletRequest request, Model model) {
        SessionUser session = getSessionUser(httpSession);

        // 수정 : session.getAccessToken() 이거 전역변수에서 가져온다
        model.addAttribute("clientValid", globalVariables.CAFE_CLIENT_ID == null ? "N" : (globalVariables.CAFE_ACCESS_TOKEN == null ? "F" : "T"));
        model.addAttribute("", "");
        model.addAttribute("", "");
        return "manager/postingSetting";
    }

    private SessionUser getSessionUser(HttpSession session) {
        return Optional
                .ofNullable((SessionUser) session.getAttribute("basicUser"))
                .orElse((SessionUser) session.getAttribute("user"));
    }
}
