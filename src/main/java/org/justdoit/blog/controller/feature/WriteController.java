package org.justdoit.blog.controller.feature;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.justdoit.blog.config.auth.SessionUser;
import org.justdoit.blog.dto.ai.write.AiWriteDto;
import org.justdoit.blog.dto.ai.write.AiWriteResponse;
import org.justdoit.blog.service.ai.AiService;
import org.justdoit.blog.service.s3.S3Service;
import org.justdoit.blog.utils.MultipartUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URLEncoder;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Controller
@RequestMapping("/feature")
public class WriteController {
    private final HttpSession httpSession;
    private final AiService aiService;
    private final S3Service s3Service;

    // AI 글쓰기
    @PostMapping("/write")
    public ResponseEntity<AiWriteResponse> write(HttpSession session, @RequestBody AiWriteDto aiWriteDto) {
        SessionUser sessionUser = (SessionUser) session.getAttribute("user");
        AiWriteResponse response;

        if (aiWriteDto.getImages().isEmpty()) {
            response = aiService.generateArticle(sessionUser, aiWriteDto);
        } else {
            aiWriteDto.setPreSignedUrls(s3Service.uploadImages(sessionUser, aiWriteDto.getImages()));
            response = aiService.generateArticleWithImages(sessionUser, aiWriteDto);
        }
        return ResponseEntity.ok(response);
    }

    private SessionUser getSessionUser(HttpSession session) {
        return Optional
                .ofNullable((SessionUser) session.getAttribute("basicUser"))
                .orElse((SessionUser) session.getAttribute("user"));
    }

//    test
// AI 글쓰기
    @PostMapping("/posting/test")
    public ResponseEntity<String> test(HttpSession httpSession) {
        SessionUser sessionUser = getSessionUser(httpSession);

        String token = sessionUser.getAccessToken();
        String header = "Bearer " + token;

        try {
            // 카페 API URL
            String clubid = "31550656";
            System.out.println("clubid");
            System.out.println(clubid);
            String menuid = "2";
            String apiURL = "https://openapi.naver.com/v1/cafe/" + clubid + "/menu/" + menuid + "/articles";

            // multipart 유틸 객체 생성
            MultipartUtil mu = new MultipartUtil(apiURL);

            // Authorization 헤더 추가
            mu.addHeaderField("Authorization", header);

            // 연결 준비
            mu.readyToConnect();

            // 제목
            String subject = URLEncoder.encode("본문 중간 이미지 삽입 테스트", "UTF-8");
            mu.addFormField("subject", subject);

            // 본문: 원하는 위치에 [사진1], [사진2] 배치
            String content = URLEncoder.encode(
                    "안녕하세요, 테스트 글입니다.<br>" +
                            "첫 번째 이미지입니다 ↓<br><img src='" + "https://jhhan-s3.s3.ap-northeast-2.amazonaws.com/images/a07308%40naver.com/1758273946963.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20250919T092547Z&X-Amz-SignedHeaders=host&X-Amz-Expires=600&X-Amz-Credential=AKIARMZJTSG6R7UYMS7X%2F20250919%2Fap-northeast-2%2Fs3%2Faws4_request&X-Amz-Signature=0024a75e7ca0623205b85f9247e6682f9bd299513c249f6a2ff66da88a88090a" + "' style='max-width:100%;'><br>" +
                            "두 번째 이미지입니다 ↓<br><img src='" + "https://jhhan-s3.s3.ap-northeast-2.amazonaws.com/images/a07308%40naver.com/1758273946839.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20250919T092546Z&X-Amz-SignedHeaders=host&X-Amz-Expires=600&X-Amz-Credential=AKIARMZJTSG6R7UYMS7X%2F20250919%2Fap-northeast-2%2Fs3%2Faws4_request&X-Amz-Signature=b4d756f36cd6442a7986b22f49c4627a4dad43f0dfa6b57e6a6b6fa121a8f52b" + "' style='max-width:100%;'><br>" +
                            "이후로는 텍스트만 이어집니다.<br>" +
                            "감사합니다.",
                    "UTF-8"
            );
            mu.addFormField("content", content);

            // 서버 응답
            List response = mu.finish();
            System.out.println("SERVER REPLIED:");

            for (Object line : response) {
                System.out.println(line);
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        return ResponseEntity.ok("");
    }
}
