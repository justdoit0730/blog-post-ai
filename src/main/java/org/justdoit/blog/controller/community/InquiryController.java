package org.justdoit.blog.controller.community;

import lombok.RequiredArgsConstructor;
import org.justdoit.blog.dto.community.InquiryDto;
import org.justdoit.blog.service.email.EmailSender;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class InquiryController {
    private final EmailSender emailSender;

    @PostMapping("/etc/community/inquery")
    public ResponseEntity<Boolean> inquiryEmailSend(@RequestBody InquiryDto inquiryDto) throws IOException {
        // sendAuthCode 를 참고해서 30분 내 보낼 수 있는 횟수가 있어야 한다.

        return ResponseEntity.ok(emailSender.inquiryEmail(inquiryDto));
    }
}
