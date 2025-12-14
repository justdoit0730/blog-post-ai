package org.justdoit.blog.controller.community;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.justdoit.blog.dto.community.InquiryDto;
import org.justdoit.blog.service.email.EmailSender;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
public class InquiryController {
    private final EmailSender emailSender;

    @PostMapping("/etc/community/inquery")
    public ResponseEntity<String> inquiryEmailSend(HttpSession session, @RequestBody InquiryDto inquiryDto) throws IOException {
        final long now = System.currentTimeMillis();

        Integer count = (Integer) session.getAttribute("inqueryEmailCount");
        Long resetTime = (Long) session.getAttribute("inqueryResetTime");

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
            session.setAttribute("inqueryEmailCount", count);
            session.removeAttribute("inqueryResetTime");
        }

        if (count <= 0) {
            session.setAttribute("resetTime", now + 30 * 60 * 1000L);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("요청 한도 초과. 30분 후 다시 시도하세요.");
        }

        return ResponseEntity.ok(emailSender.inquiryEmail(inquiryDto));
    }
}
