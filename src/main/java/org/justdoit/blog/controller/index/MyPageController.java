package org.justdoit.blog.controller.index;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.justdoit.blog.config.auth.SessionUser;
import org.justdoit.blog.entity.user.Role;
import org.justdoit.blog.variable.GlobalVariables;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@RequiredArgsConstructor
@Controller
@RequestMapping("/myPage")
public class MyPageController {
    private final HttpSession httpSession;
    private final GlobalVariables globalVariables;

    // 회원 정보
    @GetMapping("/profile")
    public String profilePage(HttpServletRequest request, Model model) {
        SessionUser session = getSessionUser(httpSession);
        model.addAttribute("email", session.getEmail());
        model.addAttribute("subEmail", session.getSubEmail());
        model.addAttribute("role", session.getRole());
        model.addAttribute("isUser", true);
        model.addAttribute("isGuest", false);

        model.addAttribute("clientValid", (session.getCafeClientId() == null || !session.isClientPrivacyAgreed()) ? "N" : (session.getAccessToken() == null ? "F" : "T"));
        model.addAttribute("isClientPrivacyAgreed", session.isClientPrivacyAgreed());

        boolean subEmailExists = session.getSubEmail() != null && !session.getSubEmail().isEmpty();

        model.addAttribute("subEmailExists", subEmailExists);
        model.addAttribute("subEmail", subEmailExists ? session.getSubEmail() : "");
        model.addAttribute("isSubEmailUsed", session.isSubEmailUsed());

        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

        if (csrfToken != null) {
            model.addAttribute("_csrf_token", csrfToken.getToken());
            model.addAttribute("_csrf_header", csrfToken.getHeaderName());
        }

        return "myPage/profile";
    }

    // AI 설정
    @GetMapping("/ai/setting")
    public String aiSetting(HttpServletRequest request, Model model) {

        SessionUser session = getSessionUser(httpSession);

        model.addAttribute("email", session.getEmail());
        model.addAttribute("role", session.getRole());
        model.addAttribute("isUser", true);
        model.addAttribute("isGuest", false);
        model.addAttribute("clientValid", (session.getCafeClientId() == null || !session.isClientPrivacyAgreed()) ? "N" : (session.getAccessToken() == null ? "F" : "T"));
        model.addAttribute("isClientPrivacyAgreed", session.isClientPrivacyAgreed());

        Role role = Role.fromKey(session.getRole());
        int availableToken = session.getAvailableToken();

        int maxToken = 0;

        if (role != null) {
            switch (role) {
                case USER -> maxToken = globalVariables.AVAILABLE_TOKEN_USER;
                case POWER_USER -> maxToken = globalVariables.AVAILABLE_TOKEN_POWER_USER;
                default -> maxToken = 0;
            }
        }

        int availableTokenRate = 0;
        if (maxToken > 0) {
            availableTokenRate = (int) ((long) availableToken * 100 / maxToken);
        }

        model.addAttribute("maxToken", session.getMaxToken());
        model.addAttribute("availableTokenRate", availableTokenRate);
        model.addAttribute("temperature", session.getTemperature() * 100);

        int textVolume = Integer.parseInt(session.getTextVolume());

        String textVolumeText;

        switch (textVolume) {
            case 1:
                textVolumeText = "짧은 글(약 350~500자)";
                break;
            case 2:
                textVolumeText = "중간 글(약 500 ~ 800자)";
                break;
            case 3:
                textVolumeText = "긴 글(약 800자 이상)";
                break;
            default:
                textVolumeText = "선택해주세요";
                break;
        }

        model.addAttribute("textVolume", textVolumeText);

        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

        if (csrfToken != null) {
            model.addAttribute("_csrf_token", csrfToken.getToken());
            model.addAttribute("_csrf_header", csrfToken.getHeaderName());
        }

        return "myPage/aiSetting";
    }

    // AI Template 설정 페이지
    @GetMapping("/ai/template/setting")
    public String aiTemplateSetting(HttpServletRequest request, Model model) {
        SessionUser session = getSessionUser(httpSession);
        model.addAttribute("email", session.getEmail());
        model.addAttribute("role", session.getRole());
        model.addAttribute("isUser", true);
        model.addAttribute("isGuest", false);

        model.addAttribute("aiTemplate", session.getAiWriteTemplate());

        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

        if (csrfToken != null) {
            model.addAttribute("_csrf_token", csrfToken.getToken());
            model.addAttribute("_csrf_header", csrfToken.getHeaderName());
        }

        return "myPage/writeTemplateSetting";
    }

    // Naver API setting
    @GetMapping("/postingSetting")
    public String naverPostingSetting(HttpServletRequest request, Model model) {
        SessionUser session = getSessionUser(httpSession);
        model.addAttribute("email", session.getEmail());
        model.addAttribute("role", session.getRole());
        model.addAttribute("isUser", true);
        model.addAttribute("isGuest", false);

        model.addAttribute("clientValid", (session.getCafeClientId() == null || !session.isClientPrivacyAgreed()) ? "N" : (session.getAccessToken() == null ? "F" : "T"));
        model.addAttribute("isClientPrivacyAgreed", session.isClientPrivacyAgreed());

        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

        if (csrfToken != null) {
            model.addAttribute("_csrf_token", csrfToken.getToken());
            model.addAttribute("_csrf_header", csrfToken.getHeaderName());
        }

        return "myPage/postingSetting";
    }

    // Naver cafe Template page
    @GetMapping("/postingTemplate")
    public String naverPostingTemplate(HttpServletRequest request, Model model) {
        SessionUser session = getSessionUser(httpSession);
        if (session != null) {
            model.addAttribute("email", session.getEmail());
            model.addAttribute("role", session.getRole());
            model.addAttribute("isUser", true);
            model.addAttribute("isGuest", false);
        } else {
            model.addAttribute("role", "GUEST");
            model.addAttribute("isUser", false);
            model.addAttribute("isGuest", true);
        }
        model.addAttribute("cafeIdTemplate", session.getCafeIdTemplate());
        model.addAttribute("cafePostingTemplate", session.getCafePostingTemplate());

        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            model.addAttribute("_csrf_token", csrfToken.getToken());
            model.addAttribute("_csrf_header", csrfToken.getHeaderName());
        }

        return "myPage/postingTemplate";
    }

    @GetMapping("/scheduleSetting")
    public String naverScheduleSetting(HttpServletRequest request, Model model) {
        SessionUser session = getSessionUser(httpSession);
        if (session != null) {
            model.addAttribute("email", session.getEmail());
            model.addAttribute("role", session.getRole());
            model.addAttribute("isUser", true);
            model.addAttribute("isGuest", false);
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

        return "myPage/scheduleSetting";
    }

    private SessionUser getSessionUser(HttpSession session) {
        return Optional
                .ofNullable((SessionUser) session.getAttribute("basicUser"))
                .orElse((SessionUser) session.getAttribute("user"));
    }
}
