package org.justdoit.blog.controller.cafe;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.justdoit.blog.config.auth.SessionUser;
import org.justdoit.blog.configuration.app.AppMetadata;
import org.justdoit.blog.dto.user.ClientDto;
import org.justdoit.blog.jpa.UserJpaService;
import org.justdoit.blog.variable.GlobalVariables;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor

public class ClientApiController {
    private final AppMetadata appMetadata;
    private final UserJpaService userJpaService;

    // Client 인증 시도
    @PostMapping("/client/naverLoginPopup")
    @ResponseBody
    public String naverLoginPopup(@RequestParam String clientId, @RequestParam String clientSecret, HttpSession session) {
        String sessionKey = UUID.randomUUID().toString();
        session.setAttribute(sessionKey, Map.of("clientId", clientId, "clientSecret", clientSecret));

        String authUrl = "https://nid.naver.com/oauth2.0/authorize"
                + "?response_type=code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + appMetadata.getServer() + "/oauth/callback"
                + "&state=" + sessionKey
                + "&scope=cafe.write";
        return authUrl;
    }

    // Client 인증 Call back 처리
    @GetMapping("/oauth/callback")
    public String naverCallback(@RequestParam("code") String code, @RequestParam("state") String sessionKey, HttpSession session) throws Exception {
        SessionUser sessionUser = (SessionUser) session.getAttribute("user");

        Map<String, String> clientInfo = (Map<String, String>) session.getAttribute(sessionKey);

        if (clientInfo == null) {
            return "popup/cafeIdPopUpFail03";
        }

        String clientId = clientInfo.get("clientId");
        String clientSecret = clientInfo.get("clientSecret");
        boolean isPrivacyAgreed = Boolean.parseBoolean(clientInfo.get("isPrivacyAgreed"));
        session.removeAttribute(sessionKey);

        sessionUser.setCafeClientId(clientId);
        sessionUser.setCafeClientSecret(clientSecret);
        sessionUser.setClientPrivacyAgreed(isPrivacyAgreed);

        // 토큰 요청
        String tokenUrl = "https://nid.naver.com/oauth2.0/token"
                + "?grant_type=authorization_code"
                + "&client_id=" + clientId
                + "&client_secret=" + clientSecret
                + "&code=" + code
                + "&state=" + sessionKey;

        try {
            HttpURLConnection con = (HttpURLConnection) new URL(tokenUrl).openConnection();
            con.setRequestMethod("GET");

            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) response.append(line);
            br.close();

            String result = response.toString();
            String accessToken = result.split("\"access_token\":\"")[1].split("\"")[0];
            String refreshToken = result.split("\"refresh_token\":\"")[1].split("\"")[0];
            String expiresIn = result.split("\"expires_in\":\"")[1].split("\"")[0];
            long expiresAt = Instant.now().getEpochSecond() + Long.parseLong(expiresIn);

            sessionUser.setCafeRefreshToken(refreshToken);
            sessionUser.setCafeRefreshTokenExpiresAt(expiresAt);

            session.setAttribute("user", sessionUser);

        } catch (Exception e){
            return "popup/cafeIdPopUpFail02";
        }
        return "popup/cafeIdPopUpSuccess";
    }

    // Client 인증 정보 수정
    @PostMapping("/myPage/client/update")
    public ResponseEntity<String> clientUpdate(HttpSession session, @RequestBody ClientDto clientDto) {
        SessionUser sessionUser = (SessionUser) session.getAttribute("user");
        if (sessionUser.getEmail() == null) {
            return ResponseEntity.ok("D-C-F001");
        } else if (!clientDto.isPrivacyAgreed()) {
            sessionUser.setClientPrivacyAgreed(false);
            return ResponseEntity.ok("D-C-F003");
        }
        sessionUser.setClientPrivacyAgreed(true);
        String result = userJpaService.clientUpdate(sessionUser, clientDto);
        return ResponseEntity.ok(result);
    }

    // Client 인증 정보 초기화
    @PostMapping("/myPage/client/clear")
    public ResponseEntity<String> clientClear(HttpSession session) {
        SessionUser sessionUser = (SessionUser) session.getAttribute("user");
        if (sessionUser.getEmail() == null) {
            return ResponseEntity.ok("D-C-F001");
        }
        String result = userJpaService.clientClear(sessionUser);
        return ResponseEntity.ok(result);
    }
//    // Naver API Client 인증 시도
//    @PostMapping("/client/naverLoginPopup")
//    @ResponseBody
//    public String naverLoginPopup(@RequestParam String clientId, @RequestParam String clientSecret, HttpSession session) {
//        String sessionKey = UUID.randomUUID().toString();
//        session.setAttribute(sessionKey, Map.of("clientId", clientId, "clientSecret", clientSecret));
//
//        String authUrl = "https://nid.naver.com/oauth2.0/authorize"
//                + "?response_type=code"
//                + "&client_id=" + clientId
//                + "&redirect_uri=" + appMetadata.getServer() + "/oauth/callback"
//                + "&state=" + sessionKey
//                + "&scope=cafe.write";
//        return authUrl;
//    }
//
//    // Client 인증 Call back 처리
//    @GetMapping("/oauth/callback")
//    public String naverCallback(@RequestParam("code") String code, @RequestParam("state") String sessionKey, HttpSession session) throws Exception {
//        SessionUser sessionUser = (SessionUser) session.getAttribute("user");
//
//        Map<String, String> clientInfo = (Map<String, String>) session.getAttribute(sessionKey);
//
//        if (clientInfo == null) {
//            return "popup/cafeIdPopUpFail03";
//        }
//
//        String clientId = clientInfo.get("clientId");
//        String clientSecret = clientInfo.get("clientSecret");
//        boolean isPrivacyAgreed = Boolean.parseBoolean(clientInfo.get("isPrivacyAgreed"));
//        session.removeAttribute(sessionKey);
//
////        sessionUser.setCafeClientId(clientId);
////        sessionUser.setCafeClientSecret(clientSecret);
////        sessionUser.setClientPrivacyAgreed(isPrivacyAgreed);
//
//        // 토큰 요청
//        String tokenUrl = "https://nid.naver.com/oauth2.0/token"
//                + "?grant_type=authorization_code"
//                + "&client_id=" + clientId
//                + "&client_secret=" + clientSecret
//                + "&code=" + code
//                + "&state=" + sessionKey;
//
//        try {
//            HttpURLConnection con = (HttpURLConnection) new URL(tokenUrl).openConnection();
//            con.setRequestMethod("GET");
//
//            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
//            StringBuilder response = new StringBuilder();
//            String line;
//            while ((line = br.readLine()) != null) response.append(line);
//            br.close();
//
//            String result = response.toString();
//            String accessToken = result.split("\"access_token\":\"")[1].split("\"")[0];
//            String refreshToken = result.split("\"refresh_token\":\"")[1].split("\"")[0];
//            String expiresIn = result.split("\"expires_in\":\"")[1].split("\"")[0];
//            long expiresAt = Instant.now().getEpochSecond() + Long.parseLong(expiresIn);
//
//            // 수정 : 세션에 저장이 아니라 디비 및 전역 변수에 저장 후 전역 변수를 불러온다.
//            globalVariables.CAFE_ACCESS_TOKEN = accessToken;
//            globalVariables.CAFE_REFRESH_TOKEN = refreshToken;
//            globalVariables.CAFE_REFRESH_TOKEN_EXPIRES_AT = LocalDateTime.now();
//            globalVariables.CAFE_REFRESH_TOKEN_VALIDATION = true;
//
//            session.setAttribute("user", sessionUser);
//
//        } catch (Exception e){
//            return "popup/cafeIdPopUpFail02";
//        }
//        return "popup/cafeIdPopUpSuccess";
//    }
//
//    // Client 인증 정보 수정
//    @PostMapping("/myPage/client/update")
//    public ResponseEntity<String> clientUpdate(HttpSession session, @RequestBody ClientDto clientDto) {
//        SessionUser sessionUser = (SessionUser) session.getAttribute("user");
//        if (!globalVariables.CAFE_REFRESH_TOKEN_VALIDATION) {
//            return ResponseEntity.ok("D-C-F002");
//        } else if (!clientDto.isPrivacyAgreed()) {
////            sessionUser.setClientPrivacyAgreed(false);
//            return ResponseEntity.ok("D-C-F003");
//        }
////        sessionUser.setClientPrivacyAgreed(true);
//        String result = managerJpaService.clientUpdate(sessionUser, clientDto);
//        return ResponseEntity.ok(result);
//    }
//
//    // Client 인증 정보 초기화
//    @PostMapping("/myPage/client/clear")
//    public ResponseEntity<String> clientClear(HttpSession session) {
//        SessionUser sessionUser = (SessionUser) session.getAttribute("user");
//        if (sessionUser.getEmail() == null) {
//            return ResponseEntity.ok("D-C-F001");
//        }
//        String result = managerJpaService.clientClear();
//        return ResponseEntity.ok(result);
//    }

}
