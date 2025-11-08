package org.justdoit.blog.service.cafe;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.justdoit.blog.config.auth.SessionUser;
import org.justdoit.blog.dto.post.PostAiDto;
import org.justdoit.blog.dto.post.PostBasicDto;
import org.justdoit.blog.entity.manager.ManagerInfo;
import org.justdoit.blog.entity.user.CafeUser;
import org.justdoit.blog.utils.MultipartUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {
    private final CafeTokenService cafeTokenService;

    private static final Logger postingLogger = LoggerFactory.getLogger("postingLogger");

    public String postArticle(CafeUser cafeUser, String accessToken, PostBasicDto postBasicDto, String title, String content) throws IOException {
        String apiURL = "https://openapi.naver.com/v1/cafe/" + postBasicDto.getCafeId() + "/menu/" + postBasicDto.getCafeBoardId() + "/articles";

        HttpURLConnection con = (HttpURLConnection) new URL(apiURL).openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Authorization", "Bearer " + accessToken);

        String subject = URLEncoder.encode(URLEncoder.encode(title, "UTF-8"), "MS949");
        String body = URLEncoder.encode(URLEncoder.encode(content, "UTF-8"), "MS949");
        String postParams = "subject=" + subject + "&content=" + body;

        con.setDoOutput(true);
        try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
            wr.writeBytes(postParams);
        }

        int responseCode = con.getResponseCode();

        if (responseCode != 200) {
            String refreshAccessToken = cafeTokenService.refreshAccessToken(cafeUser);
            if (refreshAccessToken == null) {
                int validationCount = cafeUser.getCafeValidationFailCount();
                cafeTokenService.validationCountPlus(cafeUser, ++validationCount);
                log.warn("Posting canceled after retry: access token invalid (retry count: {})", validationCount);
                return "P-F001";
            }

            con = (HttpURLConnection) new URL(apiURL).openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", "Bearer " + refreshAccessToken);
            con.setDoOutput(true);
            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                wr.writeBytes(postParams);
            }
            responseCode = con.getResponseCode();
        }

        BufferedReader br = (responseCode == 200)
                ? new BufferedReader(new InputStreamReader(con.getInputStream()))
                : new BufferedReader(new InputStreamReader(con.getErrorStream()));

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) response.append(line);
        br.close();

        cafeTokenService.validationCountReset(cafeUser);
        postingLogger.info("Post successful: menuId={}, title='{}', response={}", postBasicDto.getCafeId(), title, response);

        return "T";
    }

//    public String managerPostArticle(ManagerInfo managerInfo, String accessToken, PostBasicDto postBasicDto, String title, String content) throws IOException {
//        String apiURL = "https://openapi.naver.com/v1/cafe/" + postBasicDto.getCafeId() + "/menu/" + postBasicDto.getCafeBoardId() + "/articles";
//
//        HttpURLConnection con = (HttpURLConnection) new URL(apiURL).openConnection();
//        con.setRequestMethod("POST");
//        con.setRequestProperty("Authorization", "Bearer " + accessToken);
//
//        String subject = URLEncoder.encode(URLEncoder.encode(title, "UTF-8"), "MS949");
//        String body = URLEncoder.encode(URLEncoder.encode(content, "UTF-8"), "MS949");
//        String postParams = "subject=" + subject + "&content=" + body;
//
//        con.setDoOutput(true);
//        try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
//            wr.writeBytes(postParams);
//        }
//
//        int responseCode = con.getResponseCode();
//
//        if (responseCode != 200) {
//            String refreshAccessToken = cafeTokenService.managerRefreshAccessToken(managerInfo);
//            if (refreshAccessToken == null) {
//                int validationCount = managerInfo.getCafeValidationFailCount();
//                cafeTokenService.managerValidationCountPlus(managerInfo, ++validationCount);
//                log.warn("Posting canceled after retry: access token invalid (retry count: {})", validationCount);
//                return "P-F001";
//            }
//
//            con = (HttpURLConnection) new URL(apiURL).openConnection();
//            con.setRequestMethod("POST");
//            con.setRequestProperty("Authorization", "Bearer " + refreshAccessToken);
//            con.setDoOutput(true);
//            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
//                wr.writeBytes(postParams);
//            }
//            responseCode = con.getResponseCode();
//        }
//
//        BufferedReader br = (responseCode == 200)
//                ? new BufferedReader(new InputStreamReader(con.getInputStream()))
//                : new BufferedReader(new InputStreamReader(con.getErrorStream()));
//
//        StringBuilder response = new StringBuilder();
//        String line;
//        while ((line = br.readLine()) != null) response.append(line);
//        br.close();
//
//        cafeTokenService.validationCountReset(managerInfo);
//        postingLogger.info("Post successful: menuId={}, title='{}', response={}", postBasicDto.getCafeId(), title, response);
//
//        return "T";
//    }

    public boolean postHtmlArticle(SessionUser sessionUser, String accessToken, PostBasicDto postBasicDto) {
        String header = "Bearer " + accessToken;

        try {
            // 카페 API URL
            String cafeId = postBasicDto.getCafeId();
            String menuId = postBasicDto.getCafeBoardId();
            String apiURL = "https://openapi.naver.com/v1/cafe/" + cafeId + "/menu/" + menuId + "/articles";

            // multipart 유틸 객체 생성
            MultipartUtil mu = new MultipartUtil(apiURL);

            // Authorization 헤더 추가
            mu.addHeaderField("Authorization", header);

            // 연결 준비
            mu.readyToConnect();

            // 제목
            String subject = URLEncoder.encode(postBasicDto.getTitle(), "UTF-8");
            mu.addFormField("subject", subject);

            // 내용
            String content = encodeKoreanInHtml(postBasicDto.getContentHtml());
            mu.addFormField("content", content);

            // 서버 응답
            List response = mu.finish();
            ObjectMapper mapper = new ObjectMapper();

            for (Object line : response) {
                String jsonLine = line.toString();

                try {
                    JsonNode root = mapper.readTree(jsonLine);
                    JsonNode statusNode = root.path("message").path("status");
                    JsonNode msgNode = root.path("message").path("result").path("msg");

                    boolean isSuccess = "200".equals(statusNode.asText()) && "Success".equals(msgNode.asText());

                    if (isSuccess) {
                        JsonNode articleUrlNode = root.path("message").path("result").path("articleUrl");
                        if (!articleUrlNode.isMissingNode()) {
                            String articleUrl = articleUrlNode.asText();
                            sessionUser.setCafeBoardLink(articleUrl);
                        }
                        return true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return false;
        } catch (Exception e) {

            e.printStackTrace();
            return false;
        }
    }

    public boolean postHtmlArticle(SessionUser sessionUser, String accessToken, PostAiDto postAiDto) {
        String header = "Bearer " + accessToken;

        try {
            // 카페 API URL
            String cafeId = postAiDto.getCafeId();
            String menuId = postAiDto.getCafeBoardId();
            String apiURL = "https://openapi.naver.com/v1/cafe/" + cafeId + "/menu/" + menuId + "/articles";

            // multipart 유틸 객체 생성
            MultipartUtil mu = new MultipartUtil(apiURL);

            // Authorization 헤더 추가
            mu.addHeaderField("Authorization", header);

            // 연결 준비
            mu.readyToConnect();

            // 제목
            String subject = URLEncoder.encode(postAiDto.getTitle(), "UTF-8");
            mu.addFormField("subject", subject);

            // 내용
            String content = encodeKoreanInHtml(postAiDto.getContentHtml());
            mu.addFormField("content", content);

            // 서버 응답
            List response = mu.finish();
            ObjectMapper mapper = new ObjectMapper();

            for (Object line : response) {
                String jsonLine = line.toString();

                try {
                    JsonNode root = mapper.readTree(jsonLine);
                    JsonNode statusNode = root.path("message").path("status");
                    JsonNode msgNode = root.path("message").path("result").path("msg");

                    boolean isSuccess = "200".equals(statusNode.asText()) && "Success".equals(msgNode.asText());

                    if (isSuccess) {
                        JsonNode articleUrlNode = root.path("message").path("result").path("articleUrl");
                        if (!articleUrlNode.isMissingNode()) {
                            String articleUrl = articleUrlNode.asText();
                            sessionUser.setCafeBoardLink(articleUrl);
                        }
                        return true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return false;
        } catch (Exception e) {

            e.printStackTrace();
            return false;
        }
    }

    public static String encodeKoreanInHtml(String html) throws Exception {
        // 한글(가-힣)만 찾아서 URLEncoder 처리
        Pattern pattern = Pattern.compile("[가-힣]+");
        Matcher matcher = pattern.matcher(html);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String encoded = URLEncoder.encode(matcher.group(), "UTF-8");
            matcher.appendReplacement(sb, encoded);
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

}
