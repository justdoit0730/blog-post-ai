package org.justdoit.blog.service.user;

import com.theokanning.openai.service.OpenAiService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.justdoit.blog.config.auth.SessionUser;
import org.justdoit.blog.entity.ai.write.AiWriteSettingRepository;
import org.justdoit.blog.entity.ai.write.AiWriteSetting;
import org.justdoit.blog.entity.ai.write.AiWriteTemplate;
import org.justdoit.blog.entity.ai.write.AiWriteTemplateRepository;
import org.justdoit.blog.entity.cafe.CafeIdTemplate;
import org.justdoit.blog.entity.cafe.CafeIdTemplateRepository;
import org.justdoit.blog.entity.cafe.CafePostingTemplate;
import org.justdoit.blog.entity.cafe.CafePostingTemplateRepository;
import org.justdoit.blog.entity.user.CafeUser;
import org.justdoit.blog.entity.user.CafeUserRepository;
import org.justdoit.blog.service.cafe.CafeTokenService;
import org.justdoit.blog.utils.CryptUtils;
import org.justdoit.blog.variable.GlobalVariables;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {
    private final CryptUtils cryptUtils;
    private final GlobalVariables globalVariables;
    private final HttpSession httpSession;
    private final CafeUserRepository userRepository;
    private final CafeTokenService cafeTokenService;

    private final AiWriteSettingRepository aiWriteSettingRepository;
    private final AiWriteTemplateRepository aiWriteTemplateRepository;
    private final CafeIdTemplateRepository cafeIdTemplateRepository;
    private final CafePostingTemplateRepository cafePostingTemplateRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String email = authentication.getName();
        CafeUser cafeUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // AI 글쓰기 옵션 기본 세팅
        AiWriteSetting aiWriteSetting;
        if (email.equals(globalVariables.MAIN_EMAIL)) {
            aiWriteSetting = aiWriteSettingRepository.findByCafeUser(cafeUser)
                    .orElseGet(() -> {
                        AiWriteSetting newSetting = AiWriteSetting.builder()
                                .cafeUser(cafeUser)
                                .maxToken(1000)
                                .temperature(0.5)
                                .textVolume("1")
                                .availableToken(globalVariables.AVAILABLE_TOKEN_POWER_USER)
                                .build();
                        return aiWriteSettingRepository.save(newSetting);
                    });
        } else {
            aiWriteSetting = aiWriteSettingRepository.findByCafeUser(cafeUser)
                    .orElseGet(() -> {
                        AiWriteSetting newSetting = AiWriteSetting.builder()
                                .cafeUser(cafeUser)
                                .maxToken(1000)
                                .temperature(0.5)
                                .textVolume("1")
                                .availableToken(globalVariables.AVAILABLE_TOKEN_USER)
                                .build();
                        return aiWriteSettingRepository.save(newSetting);
                    });
        }

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime updatedAt = aiWriteSetting.getUpdatedAt().atZone(ZoneId.systemDefault()).toLocalDateTime();

        // 오늘 00:00 이전이면 사용량 초기화
        if (updatedAt.isBefore(todayStart)) {
            int newToken = switch (cafeUser.getRole()) {
                case USER -> globalVariables.AVAILABLE_TOKEN_USER;
                case POWER_USER -> globalVariables.AVAILABLE_TOKEN_POWER_USER;
                default -> aiWriteSetting.getAvailableToken();
            };
            aiWriteSetting.setAvailableToken(newToken);
            aiWriteSettingRepository.save(aiWriteSetting);
        }

        AiWriteTemplate aiWriteTemplate = aiWriteTemplateRepository.findByCafeUser(cafeUser)
                .orElseGet(() -> {
                    AiWriteTemplate newSetting = AiWriteTemplate.builder()
                            .cafeUser(cafeUser)
                            .template("")
                            .build();
                    return aiWriteTemplateRepository.save(newSetting);
                });

        CafeIdTemplate cafeIdTemplate = cafeIdTemplateRepository.findByCafeUser(cafeUser)
                .orElseGet(() -> {
                    CafeIdTemplate newTemplate = CafeIdTemplate.builder()
                            .cafeUser(cafeUser)
                            .cafeIdTemplate("")
                            .build();
                    return cafeIdTemplateRepository.save(newTemplate);
                });

        CafePostingTemplate cafePostingTemplate = cafePostingTemplateRepository.findByCafeUser(cafeUser)
                .orElseGet(() -> {
                    CafePostingTemplate newTemplate = CafePostingTemplate.builder()
                            .cafeUser(cafeUser)
                            .cafePostingTemplate("")
                            .build();
                    return cafePostingTemplateRepository.save(newTemplate);
                });

        SessionUser sessionUser = new SessionUser(cafeUser, aiWriteSetting, aiWriteTemplate, cafeIdTemplate, cafePostingTemplate);

        if (sessionUser.isClientApiEnabled()) {
            sessionUser.setCafeClientId(cryptUtils.decrypt256(cafeUser.getCafeClientId()));
            sessionUser.setCafeClientSecret(cryptUtils.decrypt256(cafeUser.getCafeClientSecret()));
            sessionUser.setCafeRefreshToken(cryptUtils.decrypt256(cafeUser.getCafeRefreshToken()));
        }

        if (sessionUser.getSubEmail() != null && !sessionUser.getSubEmail().isEmpty()) {
            sessionUser.setSubEmailAuthSuccess(true);
        }
        if (sessionUser.isSubEmailUsed()) {
            sessionUser.setReceiveEmail(sessionUser.getSubEmail());
        } else {
            sessionUser.setReceiveEmail(sessionUser.getEmail());
        }
        sessionUser.setOpenAiService(new OpenAiService(globalVariables.AI_KEY, Duration.ofSeconds(60)));

//        cafeTokenService.refreshAccessToken(cafeUser, sessionUser);
        httpSession.setAttribute("user", sessionUser);

        response.sendRedirect("/");
    }
}

