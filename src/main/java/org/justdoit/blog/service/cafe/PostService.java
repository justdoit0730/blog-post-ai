package org.justdoit.blog.service.cafe;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.justdoit.blog.config.auth.SessionUser;
import org.justdoit.blog.dto.PostingDto;
import org.justdoit.blog.entity.manager.ManagerInfo;
import org.justdoit.blog.entity.user.CafeUser;
import org.justdoit.blog.jpa.UserJpaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {
    private final CafeTokenService cafeTokenService;

    private static final Logger postingLogger = LoggerFactory.getLogger("postingLogger");

//    public String postArticle(CafeUser user, SessionUser sessionUser, String accessToken, PostingDto postingDto, String title, String content) throws IOException {
//        String apiURL = "https://openapi.naver.com/v1/cafe/" + postingDto.getCafeId() + "/menu/" + postingDto.getCafeMenuId() + "/articles";
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
//            String refreshAccessToken = "cafeTokenService.refreshAccessToken(user, sessionUser)";
//            if (refreshAccessToken == null) {
//                int validationCount = sessionUser.getCafeValidationFailCount();
//                cafeTokenService.validationCountPlus(user, ++validationCount);
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
//        cafeTokenService.validationCountReset(user);
//        postingLogger.info("Post successful: menuId={}, title='{}', response={}", postingDto.getCafeId(), title, response);
//
//        return "T";
//    }

    public String postArticle(ManagerInfo managerInfo, String accessToken, PostingDto postingDto, String title, String content) throws IOException {
        String apiURL = "https://openapi.naver.com/v1/cafe/" + postingDto.getCafeId() + "/menu/" + postingDto.getCafeMenuId() + "/articles";

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
            String refreshAccessToken = cafeTokenService.managerRefreshAccessToken(managerInfo);
            if (refreshAccessToken == null) {
                int validationCount = managerInfo.getCafeValidationFailCount();
                cafeTokenService.managerValidationCountPlus(managerInfo, ++validationCount);
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

        cafeTokenService.validationCountReset(managerInfo);
        postingLogger.info("Post successful: menuId={}, title='{}', response={}", postingDto.getCafeId(), title, response);

        return "T";
    }
}
