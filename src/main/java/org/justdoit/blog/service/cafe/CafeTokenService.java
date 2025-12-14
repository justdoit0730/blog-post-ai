package org.justdoit.blog.service.cafe;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.justdoit.blog.config.auth.SessionUser;
import org.justdoit.blog.entity.user.CafeUser;
import org.justdoit.blog.service.email.EmailSender;
import org.justdoit.blog.utils.CryptUtils;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class CafeTokenService {
    private final CryptUtils cryptUtils;
    private final EmailSender emailSender;

    public String refreshAccessToken(CafeUser cafeUser, SessionUser sessionUser) throws IOException {
        if (sessionUser.getCafeTokenExpiresAt() > Instant.now().getEpochSecond()) {
            return sessionUser.getCafeAccessToken();
        }

        String clientId = cafeUser.getCafeClientId();
        String clientSecret = cafeUser.getCafeClientSecret();
        String refreshToken = cafeUser.getCafeRefreshToken();

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

            cafeUser.setClientApiEnabled(true);
            log.info("Successfully obtained Access Token. Valid: {}, expires at: {}", accessToken != null, expiresAt);

            sessionUser.setCafeAccessToken(accessToken);
            sessionUser.setCafeTokenExpiresAt(expiresAt);

            return accessToken;
        } catch (Exception e) {
            int validationCount = cafeUser.getCafeValidationFailCount();
            validationCountPlus(cafeUser, ++validationCount);

            cafeUser.setClientApiEnabled(false);
            log.warn("Failed to obtain Access Token using Refresh Token (retry count: {}).}", validationCount);
            emailSender.refreshTokenFailSendEmail(sessionUser);
            return null;
        }
    }

    public String refreshAccessToken(CafeUser cafeUser) throws IOException {
        return refreshAccessToken(cafeUser, null);
    }

    @Transactional
    public void validationCountPlus(CafeUser user, int validationCount) {
        user.setCafeValidationFailCount(validationCount);
    }

    @Transactional
    public void validationCountReset(CafeUser user) {
        user.setCafeValidationFailCount(0);
    }

//    public String managerRefreshAccessToken(ManagerInfo managerInfo) throws IOException {
//        String refreshToken = globalVariables.CAFE_REFRESH_TOKEN;
//        String clientId = globalVariables.CAFE_CLIENT_ID;
//        String clientSecret = globalVariables.CAFE_CLIENT_SECRET;
//
//        if (clientId == null || clientId.isEmpty()) {
//            return null;
//        }
//
//        String apiUrl = "https://nid.naver.com/oauth2.0/token"
//                + "?grant_type=refresh_token"
//                + "&client_id=" + clientId
//                + "&client_secret=" + clientSecret
//                + "&refresh_token=" + refreshToken;
//
//        HttpURLConnection con = (HttpURLConnection) new URL(apiUrl).openConnection();
//        con.setRequestMethod("GET");
//
//        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
//        StringBuilder response = new StringBuilder();
//        String line;
//        while ((line = br.readLine()) != null) response.append(line);
//        br.close();
//
//        String result = response.toString();
//        try {
//            String accessToken = result.split("\"access_token\":\"")[1].split("\"")[0];
//            String expiresIn = result.split("\"expires_in\":\"")[1].split("\"")[0];
//            long expiresAt = Instant.now().getEpochSecond() + Long.parseLong(expiresIn);
//
//            globalVariables.CAFE_ACCESS_TOKEN = accessToken;
//            globalVariables.CAFE_REFRESH_TOKEN_VALIDATION = true;
//            log.info("Successfully obtained Access Token. Valid: {}, expires at: {}", accessToken != null, expiresAt);
//
//            return accessToken;
//        } catch (Exception e) {
//            int validationCount = managerInfo.getCafeValidationFailCount();
//            managerValidationCountPlus(managerInfo, ++validationCount);
//            globalVariables.CAFE_REFRESH_TOKEN_VALIDATION = false;
//            log.warn("Failed to obtain Access Token using Refresh Token (retry count: {}).}", validationCount);
//            EmailTemplate template = EmailTemplate.REFRESH_TOKEN_FAIL;
//            emailSender.sendToManagerEmail(globalVariables.SEND_EMAIL, template.getSubject(), template.getContent());
//            return null;
//        }
//    }
//
//    @Transactional
//    public void managerValidationCountPlus(ManagerInfo managerInfo, int validationCount) {
//        managerInfo.setCafeValidationFailCount(validationCount);
//    }
//
//    @Transactional
//    public void validationCountReset(ManagerInfo managerInfo) {
//        managerInfo.setCafeValidationFailCount(0);
//    }

}
