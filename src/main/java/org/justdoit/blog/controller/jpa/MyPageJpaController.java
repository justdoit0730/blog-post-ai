package org.justdoit.blog.controller.jpa;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.justdoit.blog.config.auth.SessionUser;
import org.justdoit.blog.dto.user.DeleteUserDto;
import org.justdoit.blog.dto.user.SignUpDto;
import org.justdoit.blog.dto.user.UpdateUserDto;
import org.justdoit.blog.entity.user.CafeUser;
import org.justdoit.blog.entity.user.CafeUserRepository;
import org.justdoit.blog.jpa.UserJpaService;
import org.justdoit.blog.service.email.EmailSender;
import org.justdoit.blog.service.s3.S3Service;
import org.justdoit.blog.utils.RandomCodeUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Controller
@RequiredArgsConstructor
public class MyPageJpaController {
    private final UserJpaService userJpaService;
    private final EmailSender emailSender;
    private final RandomCodeUtil randomCodeUtil;
    private final CafeUserRepository cafeUserRepository;
    private final PasswordEncoder passwordEncoder;

    private final S3Service s3Service;

    // Email 중복 확인
    @PostMapping("/user/authCode/email")
    @ResponseBody
    public boolean emailCheck(@RequestParam String email, HttpSession session) {
        return userJpaService.checkEmailDup(email);
    }

    // Email 인증 번호 전송
    @PostMapping("/user/authCode/send")
    @ResponseBody
    public ResponseEntity<String> sendAuthCode(@RequestParam String email, HttpSession session) {
        final long now = System.currentTimeMillis();

        Integer count = (Integer) session.getAttribute("emailCount");
        Long resetTime = (Long) session.getAttribute("resetTime");

        if (resetTime != null && now < resetTime) {
            long remainingMs = resetTime - now;
            long minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMs);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(remainingMs) - minutes * 60;
            String human = (minutes > 0) ? (minutes + "분 " + seconds + "초") : (seconds + "초");
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("요청 제한 중입니다. " + human + " 후 다시 시도하세요.");
        }

        if (count == null || (resetTime != null && now >= resetTime)) {
            count = 5;
            session.setAttribute("emailCount", count);
            session.removeAttribute("resetTime");
        }

        if (count <= 0) {
            session.setAttribute("resetTime", now + 30 * 60 * 1000L);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("요청 한도 초과. 30분 후 다시 시도하세요.");
        }

        String code = randomCodeUtil.generateCode(20);
        long expireAt = System.currentTimeMillis() + (5 * 60 * 1000);

        session.setAttribute("authCode", code);
        session.setAttribute("authExpireAt", expireAt);

        boolean sendResult = emailSender.checkEmail(email, "인증번호: " + code);
        if (!sendResult) {
            return ResponseEntity.ok("이메일 주소를 정확하게 작성해주세요.");
        }

        count = count - 1;
        session.setAttribute("emailCount", count);
        if (count == 0) {
            session.setAttribute("resetTime", now + 30 * 60 * 1000L);
        }
        session.setAttribute("email", email);
        return ResponseEntity.ok("");
    }

    // Email 인증 번호 확인
    @PostMapping("/user/authCode/verify")
    @ResponseBody
    public String verifyAuthCode(@RequestParam String code, HttpSession session) {
        String savedCode = (String) session.getAttribute("authCode");
        Long expireAt = (Long) session.getAttribute("authExpireAt");

        if (savedCode == null || expireAt == null) {
            return "인증문자가 없습니다. 인증문자를 요청해주세요.";
        }

        if (System.currentTimeMillis() > expireAt) {
            return "인증문자가 만료되었습니다.";
        }

        if (!savedCode.equals(code.trim())) {
            return "인증문자가 일치하지 않습니다.";
        }
        session.setAttribute("emailAuthSuccess", true);
        session.removeAttribute("authCode");
        session.removeAttribute("authExpireAt");
        return "인증 성공 하였습니다.";
    }

    // 회원 가입
    @PostMapping("/user/signUp")
    public ResponseEntity<String> registerUser(HttpSession session, @RequestBody SignUpDto signUpDto) {
        if (!signUpDto.isPrivacyAgreed()) {
            return ResponseEntity.ok("isPrivacyAgreedError");
        } else if (!(boolean) session.getAttribute("emailAuthSuccess")) {
            return ResponseEntity.ok("emailAuthError");
        }

        String result = userJpaService.save(session, signUpDto);
        session.removeAttribute("emailAuthSuccess");
        return ResponseEntity.ok(result);
    }

    // Sub Email 인증 번호 전송
    @PostMapping("/subEmail/authCode/send")
    @ResponseBody
    public ResponseEntity<String> subEmailSendAuthCode(@RequestParam String subEmail, HttpSession session) {
        SessionUser sessionUser = getSessionUser(session);

        if (subEmail.equals(sessionUser.getEmail())) {
            return ResponseEntity.ok("수신용 이메일의 주소는 기존 이메일과 달라야합니다.");
        }

        final long now = System.currentTimeMillis();
        Integer count = sessionUser.getSubEmailSendCount();

        String code = randomCodeUtil.generateCode(20);
        long expireAt = System.currentTimeMillis() + (5 * 60 * 1000);

        sessionUser.setSubEmailAuthCode(code);
        sessionUser.setSubEmailAuthExpireAt(expireAt);

        boolean sendResult = emailSender.checkSubEmail(sessionUser, code);
        if (!sendResult) {
            return ResponseEntity.ok("이메일 주소를 정확하게 작성해주세요.");
        }
        count = count - 1;
        sessionUser.setSubEmailSendCount(count);
        if (count == 0) {
            sessionUser.setSubEmailSendResetTime(now + 30 * 60 * 1000L);
        }

        return ResponseEntity.ok("");
    }

    // Sub Email 인증 번호 확인
    @PostMapping("/subEmail/authCode/verify")
    @ResponseBody
    public String subEmailVerifyAuthCode(@RequestParam String code, HttpSession session) {
        SessionUser sessionUser = getSessionUser(session);

        String savedCode = sessionUser.getSubEmailAuthCode();
        Long expireAt = sessionUser.getSubEmailAuthExpireAt();

        if (savedCode == null || expireAt == null) {
            return "인증문자가 없습니다. 인증문자를 요청해주세요.";
        }

        if (System.currentTimeMillis() > expireAt) {
            return "인증문자가 만료되었습니다.";
        }

        if (!savedCode.equals(code.trim())) {
            return "인증문자가 일치하지 않습니다.";
        }
        sessionUser.setSubEmailAuthSuccess(true);
        sessionUser.setSubEmailAuthCode("");
        sessionUser.setSubEmailAuthExpireAt(0L);
        return "인증 성공 하였습니다.";
    }

    // 수신용 이메일 초기화 시 저장된 권한 삭제
    @PostMapping("/subEmail/email/auth/clear")
    public void subEmailAuthClear(HttpSession session) {
        SessionUser sessionUser = getSessionUser(session);
        sessionUser.setSubEmailAuthSuccess(false);
    }

    // 수신용 이메일 수정
    @PostMapping("/subEmail/update")
    public ResponseEntity<String> subEmailUpdate(HttpSession session, @RequestBody UpdateUserDto updateUserDto) {
        SessionUser sessionUser = getSessionUser(session);
        if (updateUserDto.getSubEmail().equals(sessionUser.getEmail())) {
            return ResponseEntity.ok("emailError");
        }

        if (updateUserDto.getSubEmail() == null || updateUserDto.getSubEmail().isEmpty()) {
            updateUserDto.setSubEmailUsed(false);
        } else if (!sessionUser.isSubEmailAuthSuccess()) {
            return ResponseEntity.ok("AuthError");
        }

        String result = userJpaService.subEmailUpdate(sessionUser, updateUserDto);
        if (result.equals("T")) {
            sessionUser.setSubEmail(updateUserDto.getSubEmail());
            sessionUser.setSubEmailUsed(updateUserDto.isSubEmailUsed());
            if (updateUserDto.isSubEmailUsed()) {
                sessionUser.setReceiveEmail(sessionUser.getSubEmail());
            } else {
                sessionUser.setReceiveEmail(sessionUser.getEmail());
            }
        }

        return ResponseEntity.ok(result);
    }

    // 비밀번호 수정
    @PostMapping("/password/update")
    public ResponseEntity<String> passwordUpdate(HttpSession session, @RequestBody UpdateUserDto updateUserDto) {
        SessionUser sessionUser = getSessionUser(session);
        String result = userJpaService.passwordUpdate(sessionUser, updateUserDto);
        return ResponseEntity.ok(result);
    }

    // 회원 탈퇴
    @PostMapping("/myPage/user/delete")
    public ResponseEntity<String> userDelete(HttpSession session, @RequestBody DeleteUserDto deleteUserDto) {
        SessionUser sessionUser = getSessionUser(session);

        String email = sessionUser.getEmail();
        CafeUser cafeUser = cafeUserRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(deleteUserDto.getPassword(), cafeUser.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("비밀번호가 일치하지 않습니다.");
        }

        try {
            cafeUserRepository.delete(cafeUser);
            session.invalidate();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("회원 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
        s3Service.deleteEmailDirectory(sessionUser);
        return ResponseEntity.ok("T");
    }

    private SessionUser getSessionUser(HttpSession session) {
        return Optional
                .ofNullable((SessionUser) session.getAttribute("basicUser"))
                .orElse((SessionUser) session.getAttribute("user"));
    }
}

