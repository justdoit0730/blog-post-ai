package org.justdoit.blog.controller.jpa;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.justdoit.blog.config.auth.SessionUser;

import org.justdoit.blog.entity.cafe.CafePosting;
import org.justdoit.blog.entity.cafe.CafePostingRepository;
import org.justdoit.blog.service.s3.S3Service;
import org.justdoit.blog.template.S3Prefix;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/cafe")
public class PostJpaController {
    private final CafePostingRepository cafePostingRepository;

    private final HttpSession httpSession;
    private final S3Service s3Service;

    // 글 삭제
    @PostMapping("/post/delete/{id}")
    public ResponseEntity<String> deletePost(@PathVariable Long id) {
        SessionUser sessionUser = getSessionUser(httpSession);
        Optional<CafePosting> cafePostOpt = cafePostingRepository.findById(id);
        if (cafePostOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("F");
        }

        CafePosting cafePosting = cafePostOpt.get();
        List<String> fileNames = Arrays.stream(cafePosting.getImgList().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        if (!fileNames.isEmpty()) {
            s3Service.deleteImages(sessionUser, fileNames, S3Prefix.POST.getPrefix());
        }
        cafePostingRepository.delete(cafePosting);

        return ResponseEntity.ok("T");
    }


    private SessionUser getSessionUser(HttpSession session) {
        return Optional
                .ofNullable((SessionUser) session.getAttribute("basicUser"))
                .orElse((SessionUser) session.getAttribute("user"));
    }
}
    //    test
//    @PostMapping("/posting/test")
//    public ResponseEntity<String> test(HttpSession httpSession) throws IOException {
//        SessionUser sessionUser = getSessionUser(httpSession);
//        ManagerInfo managerInfo = managerInfoRepository.findById("default")
//                .orElseThrow(() -> new IllegalStateException("ManagerInfo not found"));
//
//
//        // 1. accessToken 있는 지 부터 확인한다.
////        String accessToken = globalVariables.CAFE_ACCESS_TOKEN;
//        String accessToken = cafeTokenService.managerRefreshAccessToken(managerInfo);
//
//        String token = accessToken;
//        System.out.println(token);
//        String header = "Bearer " + token;
//
//        try {
//            // 카페 API URL
//            String clubid = "31550656";
//            System.out.println("clubid");
//            System.out.println(clubid);
//            String menuid = "2";
//            String apiURL = "https://openapi.naver.com/v1/cafe/" + clubid + "/menu/" + menuid + "/articles";
//
//            // multipart 유틸 객체 생성
//            MultipartUtil mu = new MultipartUtil(apiURL);
//
//            // Authorization 헤더 추가
//            mu.addHeaderField("Authorization", header);
//
//            // 연결 준비
//            mu.readyToConnect();
//
//            // 제목
//            String subject = URLEncoder.encode("본문 중간 이미지 삽입 테스트", "UTF-8");
//            mu.addFormField("subject", subject);
//
//            // 본문: 원하는 위치에 [사진1], [사진2] 배치
////            String content = URLEncoder.encode(
////                    "test<br><br><br><br>1324",
////                    "UTF-8"
////            );
//            // ❌ URLEncoder.encode 제거
//            String content =
//                    "test<img src=\"https://jhhan-s3.s3.ap-northeast-2.amazonaws.com/images/a07308@naver.com/cache/1760409643837.jpg\"><br><br><br><br>1324";
//
//            mu.addFormField("content", content);
//
//            // 서버 응답
//            List response = mu.finish();
//            System.out.println("SERVER REPLIED:");
//
//            for (Object line : response) {
//                System.out.println(line);
//            }
//
//        } catch (Exception e) {
//            System.out.println("Error: " + e.getMessage());
//            e.printStackTrace();
//        }
//        return ResponseEntity.ok("");
//    }
//    @PostMapping("/posting/test")
//    public ResponseEntity<String> test(HttpSession httpSession) {
//        SessionUser sessionUser = getSessionUser(httpSession);
//        String email = sessionUser.getEmail();
//        CafeUser cafeUser = userRepository.findByEmail(email)
//                .orElseThrow(() -> new IllegalStateException("User not found"));
//
//        String token = sessionUser.getAccessToken() == null ? globalVariables.CAFE_ACCESS_TOKEN : sessionUser.getAccessToken();
//        System.out.println(token);
//        String header = "Bearer " + token;
//
//        try {
//            // 카페 API URL
//            String clubid = "31550656";
//            System.out.println("clubid");
//            System.out.println(clubid);
//            String menuid = "2";
//            String apiURL = "https://openapi.naver.com/v1/cafe/" + clubid + "/menu/" + menuid + "/articles";
//
//            // multipart 유틸 객체 생성
//            MultipartUtil mu = new MultipartUtil(apiURL);
//
//            // Authorization 헤더 추가
//            mu.addHeaderField("Authorization", header);
//
//            // 연결 준비
//            mu.readyToConnect();
//
//            // 제목
//            String subject = URLEncoder.encode("본문 중간 이미지 삽입 테스트", "UTF-8");
//            mu.addFormField("subject", subject);
//
//            // 본문: 원하는 위치에 [사진1], [사진2] 배치
//            String content = URLEncoder.encode(
//
//                    "ㅇ<br>ㅇ<br>ㅇ<br><img src=\"https://jhhan-s3.s3.ap-northeast-2.amazonaws.com/images/a07308@naver.com/cache/1760345870337.jpg\" alt=\"스크린샷 2025-07-10 140306-Photoroom.png\" contenteditable=\"false\"><br><br>ㅇ<br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br>ㅇ<br><br><img src=\"https://jhhan-s3.s3.ap-northeast-2.amazonaws.com/images/a07308@naver.com/cache/1760345870446.jpg\" alt=\"스크린샷 2025-07-10 140306-Photoroom.png\" contenteditable=\"false\"><br><br>ㅇ<br>ㅇ<br>ㅇ<br>ㅇ<br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br>ㅇ<br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br>",
//                    "UTF-8"
//            );
//            mu.addFormField("content", content);
//
//            // 서버 응답
//            List response = mu.finish();
//            System.out.println("SERVER REPLIED:");
//
//            for (Object line : response) {
//                System.out.println(line);
//            }
//
//        } catch (Exception e) {
//            System.out.println("Error: " + e.getMessage());
//            e.printStackTrace();
//        }
//        return ResponseEntity.ok("");
//    }