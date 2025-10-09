package org.justdoit.blog.service.cafe;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.justdoit.blog.config.auth.SessionUser;
import org.justdoit.blog.entity.manager.ManagerInfo;
import org.justdoit.blog.entity.user.CafeUser;
import org.justdoit.blog.service.email.EmailSender;
import org.justdoit.blog.service.email.EmailTemplate;
import org.justdoit.blog.utils.CryptUtils;
import org.justdoit.blog.variable.GlobalVariables;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CafeTokenService {
    private final GlobalVariables globalVariables;
    private final CryptUtils cryptUtils;
    private final EmailSender emailSender;

    public String getAccessToken(CafeUser user, SessionUser sessionUser) throws IOException {
        if (sessionUser.getCafeRefreshToken() == null || sessionUser.getCafeRefreshToken().isEmpty()) {
            int validationCount = sessionUser.getCafeValidationFailCount();
            validationCountPlus(user, ++validationCount);
            return "C-F001"; // 현재 로그인한 유저 email 에 대해 회원정보(cafe_user) 결과 중 refreshToken 이 없는 경우 -> session 에 횟수 추가
        }
        return refreshAccessToken(user, sessionUser);
    }

    public String refreshAccessToken(CafeUser user, SessionUser sessionUser) throws IOException {
        String clientId = user.getCafeClientId();
        String clientSecret = user.getCafeClientSecret();
        String refreshToken = user.getCafeRefreshToken();

        if (clientId == null || clientId.isEmpty()) {
            return null;
        }

        String apiUrl = "https://nid.naver.com/oauth2.0/token"
                + "?grant_type=refresh_token"
                + "&client_id=" + cryptUtils.decrypt256(clientId)
                + "&client_secret=" + cryptUtils.decrypt256(clientSecret)
                + "&refresh_token=" + cryptUtils.decrypt256(refreshToken);

        HttpURLConnection con = (HttpURLConnection) new URL(apiUrl).openConnection();
        con.setRequestMethod("GET");

        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) response.append(line);
        br.close();

        String result = response.toString();
        try {
            String accessToken = result.split("\"access_token\":\"")[1].split("\"")[0];
            String expiresIn = result.split("\"expires_in\":\"")[1].split("\"")[0];
            long expiresAt = Instant.now().getEpochSecond() + Long.parseLong(expiresIn);

            sessionUser.setAccessToken(accessToken);
            sessionUser.setAccessTokenExpiresAt(LocalDateTime.now());
            sessionUser.setAccessTokenValidation(true);
            log.info("Successfully obtained Access Token. Valid: {}, expires at: {}", accessToken != null, expiresAt);

            return accessToken;
        } catch (Exception e) {
            int validationCount = user.getCafeValidationFailCount();
            validationCountPlus(user, ++validationCount);
            sessionUser.setAccessTokenValidation(false);
            log.warn("Failed to obtain Access Token using Refresh Token (retry count: {}).}", validationCount);
            EmailTemplate template = EmailTemplate.REFRESH_TOKEN_FAIL;
            emailSender.sendEmail(sessionUser, template.getSubject(), template.getContent());
            return null;
        }
    }

    public String refreshAccessToken(CafeUser user) throws IOException {
        return refreshAccessToken(user, null);
    }

    @Transactional
    public void validationCountPlus(CafeUser user, int validationCount) {
        user.setCafeValidationFailCount(validationCount);
    }

    @Transactional
    public void validationCountReset(CafeUser user) {
        user.setCafeValidationFailCount(0);
    }

    public String managerRefreshAccessToken(ManagerInfo managerInfo) throws IOException {
        String refreshToken = globalVariables.CAFE_REFRESH_TOKEN;
        String clientId = globalVariables.CAFE_CLIENT_ID;
        String clientSecret = globalVariables.CAFE_CLIENT_SECRET;

        if (clientId == null || clientId.isEmpty()) {
            return null;
        }

        String apiUrl = "https://nid.naver.com/oauth2.0/token"
                + "?grant_type=refresh_token"
                + "&client_id=" + clientId
                + "&client_secret=" + clientSecret
                + "&refresh_token=" + refreshToken;

        HttpURLConnection con = (HttpURLConnection) new URL(apiUrl).openConnection();
        con.setRequestMethod("GET");

        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) response.append(line);
        br.close();

        String result = response.toString();
        try {
            String accessToken = result.split("\"access_token\":\"")[1].split("\"")[0];
            String expiresIn = result.split("\"expires_in\":\"")[1].split("\"")[0];
            long expiresAt = Instant.now().getEpochSecond() + Long.parseLong(expiresIn);

            globalVariables.CAFE_ACCESS_TOKEN = accessToken;
            globalVariables.CAFE_REFRESH_TOKEN_VALIDATION = true;
            log.info("Successfully obtained Access Token. Valid: {}, expires at: {}", accessToken != null, expiresAt);

            return accessToken;
        } catch (Exception e) {
            int validationCount = managerInfo.getCafeValidationFailCount();
            validationCountPlus(managerInfo, ++validationCount);
            globalVariables.CAFE_REFRESH_TOKEN_VALIDATION = false;
            log.warn("Failed to obtain Access Token using Refresh Token (retry count: {}).}", validationCount);
            EmailTemplate template = EmailTemplate.REFRESH_TOKEN_FAIL;
            emailSender.sendToManagerEmail(globalVariables.SEND_EMAIL, template.getSubject(), template.getContent());
            return null;
        }
    }

    @Transactional
    public void managerValidationCountPlus(ManagerInfo managerInfo, int validationCount) {
        managerInfo.setCafeValidationFailCount(validationCount);
    }

    @Transactional
    public void validationCountReset(ManagerInfo managerInfo) {
        managerInfo.setCafeValidationFailCount(0);
    }

}
