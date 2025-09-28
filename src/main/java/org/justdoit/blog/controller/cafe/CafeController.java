package org.justdoit.blog.controller.cafe;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.justdoit.blog.config.auth.SessionUser;
import org.justdoit.blog.dto.PostingDto;
import org.justdoit.blog.entity.user.CafeUser;
import org.justdoit.blog.entity.user.CafeUserRepository;
import org.justdoit.blog.service.cafe.CafeTokenService;
import org.justdoit.blog.service.cafe.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
public class CafeController {
    private final PostService postService;
    private final CafeUserRepository userRepository;
    private final CafeTokenService cafeTokenService;

    private final HttpSession httpSession;

    @PostMapping("/posting")
    public ResponseEntity<String> registerUser(HttpSession session, @RequestBody PostingDto postingDto) throws IOException {
        String email = session.getAttribute("email").toString();
        CafeUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        if (user == null) {
            return ResponseEntity.ok("C-F000");
        }

        // 0. 인증 시도 실패 횟수가 5 이상이면 그냥 취소
        int validationCount = user.getCafeValidationFailCount();
        if (validationCount > 5) {
            return ResponseEntity.ok("C-F001");
        }

        // 1. accessToken 있는 지 부터 확인한다.
        SessionUser sessionUser = (SessionUser) httpSession.getAttribute("user");
        String accessToken;
        if (sessionUser != null &&
                sessionUser.getAccessToken() != null &&
                sessionUser.getAccessTokenExpiresAt() != null &&
                sessionUser.getAccessTokenExpiresAt().isAfter(LocalDateTime.now().plusMinutes(30))) {
            accessToken = sessionUser.getAccessToken();
        } else {
            accessToken = cafeTokenService.getAccessToken(user, sessionUser);
        }

        if (accessToken == null) {
            // 현재 로그인한 유저 email 에 대해 회원정보(cafe_user) 결과 중 refreshToken 이 유효하지 않은 경우 -> session 에 횟수 추가, Client 정보 업데이트하라고 해야한다.
            // 메일 보내졌음.
            // 그대로 return
            return ResponseEntity.ok("C-F001");
        }
        // 2. postingDto 의 내용을 AI 서버로 HTTP 통신으로 보낸다.
            // postingDto.subject, postingDto.template 주제, 말투도 함께 보낸다.


        // 3. 해당 통신으로 받은 결과를 Posting 하고 결과 리턴
            // title, content는 2번 AI에서 받아온다.
            // 포스팅 성공 시
                // int validationCount = user.getValidationCount();
                // userService.validationCountPlus(user, ++validationCount);
                // 이거 초기화
        String result = postService.postArticle(user, sessionUser, accessToken, postingDto, "title", "content");


        return ResponseEntity.ok(result);
    }
//
//    @PostMapping("/test")
//    public Map<String,Object> retrieve(@RequestBody Map<String,Object> payload, @RequestBody SignUpDto signUpDto) {
//        // AI 서버에서 받아온다. 제목과 내용을 -> 지금은 일단 테스트
//        System.err.println("POST 요청 수신: " + payload);
//        return Map.of("status","ok","received",payload);
//    }
}