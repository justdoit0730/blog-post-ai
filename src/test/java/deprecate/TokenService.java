//package org.justdoit.blog.service;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.justdoit.blog.config.NaverBlogConfig;
//import org.justdoit.blog.entity.cafe.RefreshToken;
//import org.justdoit.blog.metable.GlobalVariables;
//import org.justdoit.blog.repository.RefreshTokenRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import java.awt.*;
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.net.HttpURLConnection;
//import java.net.URI;
//import java.net.URL;
//import java.net.URLEncoder;
//import java.nio.charset.StandardCharsets;
//import java.time.Instant;
//import java.util.Scanner;
//
//
//@Service
//public class TokenService {
//    private static final RestTemplate restTemplate = new RestTemplate();
//    private static ObjectMapper objectMapper;
//    private static GlobalVariables globalMutableVariables;
//    private static RefreshTokenRepository refreshTokenRepository;
//    private static String CLIENT_ID;
//    private static String CLIENT_SECRET;
//    private static String REDIRECT_URI;
//
//    private static String accessToken;
//    private static long expiresAt = 0;
//
//    @Autowired
//    public TokenService(ObjectMapper objectMapper, NaverBlogConfig naverBlogConfig, RefreshTokenRepository refreshTokenRepository) {
//        this.objectMapper = objectMapper;
//        this.refreshTokenRepository = refreshTokenRepository;
//        CLIENT_ID = naverBlogConfig.getClientId();
//        CLIENT_SECRET = naverBlogConfig.getClientSecret();
//        REDIRECT_URI = naverBlogConfig.getRedirectUri();
//    }
//
//    public static String getAccessToken() throws IOException {
//        long now = Instant.now().getEpochSecond();
//
//        // 최초 기동(DB에 Refresh Token 없는 경우) 시 불러오고 저장
//        if (globalMutableVariables.REFRESH_TOKEN == null) {
//            loadOrInitRefreshToken();
//        }
//
//        if (globalMutableVariables.accessToken == null || now >= expiresAt) {
//            refreshAccessToken();
//        }
//
//        return globalMutableVariables.accessToken;
//    }
//
//    public static void loadOrInitRefreshToken() throws IOException {
//        String state = "state";
//        String authUrl = "https://nid.naver.com/oauth2.0/authorize"
//                + "?response_type=code"
//                + "&client_id=" + CLIENT_ID
//                + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8)
//                + "&state=" + state
//                + "&scope=cafe.write";
//
//        System.out.println("브라우저에서 로그인 후 authorization code를 입력하세요.");
//        if (Desktop.isDesktopSupported()) {
//            Desktop.getDesktop().browse(URI.create(authUrl));
//        } else {
//            System.out.println("로그인 URL: " + authUrl);
//        }
//
//        Scanner scanner = new Scanner(System.in);
//        System.out.print("authorization code 입력: ");
//        String code = scanner.nextLine();
//
//        String tokenUrl = "https://nid.naver.com/oauth2.0/token"
//                + "?grant_type=authorization_code"
//                + "&client_id=" + CLIENT_ID
//                + "&client_secret=" + CLIENT_SECRET
//                + "&code=" + code
//                + "&state=" + state;
//
//        HttpURLConnection con = (HttpURLConnection) new URL(tokenUrl).openConnection();
//        con.setRequestMethod("GET");
//
//        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
//        StringBuilder response = new StringBuilder();
//        String line;
//        while ((line = br.readLine()) != null) response.append(line);
//        br.close();
//
//        String result = response.toString();
//        globalMutableVariables.accessToken = result.split("\"access_token\":\"")[1].split("\"")[0];
//        String refreshToken = result.split("\"refresh_token\":\"")[1].split("\"")[0];
//        String expiresIn = result.split("\"expires_in\":\"")[1].split("\"")[0];
//        long expiresAt = Instant.now().getEpochSecond() + Long.parseLong(expiresIn);
//
//        refreshTokenRepository.save(new RefreshToken(refreshToken, expiresAt));
//
//
//        System.out.println("Access Token: " + globalMutableVariables.accessToken);
//        System.out.println("Refresh Token DB 저장 완료.");
//
//    }
//
//    private static void refreshAccessToken_og() throws IOException {
//        String apiUrl = "https://nid.naver.com/oauth2.0/token"
//                + "?grant_type=refresh_token"
//                + "&client_id=" + CLIENT_ID
//                + "&client_secret=" + CLIENT_SECRET
//                + "&refresh_token=" + "Y9C1tXtBEYXBiiM0VBQBOvo9Cc8QLEhbFm9OLuWhRu6XjGG0fq7Eis0oP0Vj5Lwx5SGisYVXiiw3AxGxIis0pR0vHXqBPpbmEx1RGm5TLgmziiCDPWgP2ismLMLkrmduismUcqJy";
////                + "&refresh_token=" + globalMutableVariables.refreshToken;
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
//        globalMutableVariables.accessToken = result.split("\"access_token\":\"")[1].split("\"")[0];
//        String expiresIn = result.split("\"expires_in\":\"")[1].split("\"")[0];
//        expiresAt = Instant.now().getEpochSecond() + Long.parseLong(expiresIn);
//
//        System.out.println("갱신된 Access Token: " + globalMutableVariables.accessToken);
//    }
//
//    public static void refreshAccessToken() throws IOException {
//        String apiUrl = "https://nid.naver.com/oauth2.0/token"
//                + "?grant_type=refresh_token"
//                + "&client_id=" + CLIENT_ID
//                + "&client_secret=" + CLIENT_SECRET
//                + "&refresh_token=" + globalMutableVariables.REFRESH_TOKEN;
//
//        // GET 요청
//        String response = restTemplate.getForObject(apiUrl, String.class);
//
//        // JSON 파싱
//        JsonNode jsonNode = objectMapper.readTree(response);
//        String accessToken = jsonNode.get("access_token").asText();
//        long expiresIn = jsonNode.get("expires_in").asLong();
//
//        // 전역 변수 갱신
//        globalMutableVariables.accessToken = accessToken;
//        expiresAt = Instant.now().getEpochSecond() + expiresIn;
//
//        System.out.println("갱신된 Access Token: " + accessToken);
//    }
//}
//
