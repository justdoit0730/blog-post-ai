package org.justdoit.blog.config.auth;

import com.theokanning.openai.service.OpenAiService;
import lombok.Getter;
import lombok.Setter;
import org.justdoit.blog.entity.ai.AiWriteSetting;
import org.justdoit.blog.entity.ai.AiWriteTemplate;
import org.justdoit.blog.entity.cafe.CafeIdTemplate;
import org.justdoit.blog.entity.cafe.CafePostingTemplate;
import org.justdoit.blog.entity.user.CafeUser;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SessionUser implements Serializable {
    // 최초 로그인 시
    private String email;
    private String subEmail;
    private boolean isSubEmailUsed;
    private String receiveEmail;

    private boolean isEmailPrivacyAgreed;
    private String role;

//    naver api
    private boolean clientApiEnabled;
    private boolean isClientPrivacyAgreed;
    private String cafeClientId = "";
    private String cafeClientSecret = "";

    private int cafeValidationFailCount;

    private String cafeRefreshToken;
    private long cafeRefreshTokenExpiresAt;

    // openAI
    private OpenAiService openAiService;
    private int maxToken;
    private int availableToken;
    private double temperature;
    private String textVolume;

    private String aiWriteTemplate;
    private String cafeIdTemplate;
    private String cafePostingTemplate;

    private String writeTitle = "";
    private String writeContent = "";
    private List<String> writeImgUrls = new ArrayList<>();

    private String postTitle = "";
    private String postContentHtml = "";
    private List<String> postBasicImgList;
    private String cafeBoardLink;


    private String postAiTitle = "";
    private String postAiContentHtml = "";
    private String cafeAiBoardLink;
    private List<String> postAiImgList;

    private List<String> postImgUrls = new ArrayList<>();


    private int subEmailSendCount;
    private Long subEmailSendResetTime;
    private String subEmailAuthCode;
    private long subEmailAuthExpireAt;
    private boolean subEmailAuthSuccess;

    public SessionUser(CafeUser cafeUser, AiWriteSetting aiWriteSetting, AiWriteTemplate aiWriteTemplate, CafeIdTemplate cafeIdTemplate, CafePostingTemplate cafePostingTemplate) {
        this.email = cafeUser.getEmail();
        this.subEmail = cafeUser.getSubEmail();
        this.isSubEmailUsed = cafeUser.isSubEmailUsed();
        this.cafeValidationFailCount = cafeUser.getCafeValidationFailCount();
        this.cafeRefreshTokenExpiresAt = cafeUser.getCafeRefreshTokenExpiresAt();
        this.isEmailPrivacyAgreed = cafeUser.isEmailPrivacyAgreed();
        this.isClientPrivacyAgreed = cafeUser.isClientPrivacyAgreed();
        this.clientApiEnabled = cafeUser.isClientApiEnabled();
        this.role = String.valueOf(cafeUser.getRole());

        this.maxToken = aiWriteSetting.getMaxToken();
        this.availableToken = aiWriteSetting.getAvailableToken();
        this.temperature = aiWriteSetting.getTemperature();
        this.textVolume = aiWriteSetting.getTextVolume();

        this.aiWriteTemplate = aiWriteTemplate.getTemplate();

        this.cafeIdTemplate = cafeIdTemplate.getCafeIdTemplate();

        this.cafePostingTemplate = cafePostingTemplate.getCafePostingTemplate();
    }

}
