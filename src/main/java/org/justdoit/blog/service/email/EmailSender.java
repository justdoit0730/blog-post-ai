package org.justdoit.blog.service.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.justdoit.blog.config.auth.SessionUser;
import org.justdoit.blog.dto.community.InquiryDto;
import org.justdoit.blog.variable.GlobalVariables;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSender {
    private final GlobalVariables globalVariables;

    public void sendEmail(SessionUser sessionUser, String subject, String content) {
        String email = sessionUser.getReceiveEmail().isEmpty() ? sessionUser.getEmail() : sessionUser.getReceiveEmail();
        String title = "[AutoCafeWriter] : ";
        String basicContent = "Test 입니다. ";
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(title + subject);
        message.setText(basicContent + content);

        try {
            globalVariables.M_SENDER.send(message);
        } catch (Exception e) {

        }
        System.out.println("이메일 전송 완료");
    }

    public boolean checkEmail(String toEmail, String subject, String content) {
        String title = "[AutoCafeWriter] : ";
        String basicContent = "Test 입니다. ";
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(title + subject);
        message.setText(basicContent + content);

        try {
            globalVariables.M_SENDER.send(message);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean checkSubEmail(SessionUser sessionUser, String toEmail, String subject, String content) {
        String title = "[AutoCafeWriter] : ";
        String basicContent = "Test 입니다. ";
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(title + subject);
        message.setText(basicContent + content);

        try {
            globalVariables.M_SENDER.send(message);
        } catch (Exception e) {
            sessionUser.setSubEmailAuthSuccess(false);
            return false;
        }
        return true;
    }

    // 문의사항

    public boolean inquiryEmail(InquiryDto inquiryDto) {
        String title = "[AutoCafeWriter] 문의 사항 : ";
        String basicContent = "문의 Email : " + inquiryDto.getInquiryEmail();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(globalVariables.SEND_EMAIL);
        message.setSubject(title + inquiryDto.getInquiryTitle());
        message.setText(basicContent + inquiryDto.getInquiryDetails());

        try {
            globalVariables.M_SENDER.send(message);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

//    public boolean inquiryEmail(String inquiryEmail, String inquiryTitle, String inquiryDetails) {
//        String title = "[AutoCafeWriter] 문의 사항 : ";
//        String basicContent = "문의 Email : " + inquiryEmail;
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(globalVariables.SEND_EMAIL);
//        message.setSubject(title + inquiryTitle);
//        message.setText(basicContent + inquiryDetails);
//
//        try {
//            globalVariables.M_SENDER.send(message);
//        } catch (Exception e) {
//            return false;
//        }
//        return true;
//    }

}

//    private TokenMetadata tokenMetadata;
//    private final JavaMailSender mailSender;
//
//    public EmailSender() {
//        JavaMailSenderImpl sender = new JavaMailSenderImpl();
//        sender.setHost("smtp.gmail.com");
//        sender.setPort(465);
//        sender.setUsername("eumjs69@gmail.com");        // 보내는 사람
//        sender.setPassword("zyob weba hrau rbzb");
//
//        Properties props = sender.getJavaMailProperties();
//        props.put("mail.transport.protocol", "smtp");
//        props.put("mail.smtp.auth", "true");
//        props.put("mail.smtp.ssl.enable", "true");
//        props.put("mail.smtp.starttls.enable", "true");
//        props.put("mail.debug", "true");
//
//        this.mailSender = sender;
//    }
//
//    /**
//     * 단순 메일 전송
//     * @param toEmail 수신자 이메일
//     * @param subject 제목
//     * @param content 내용
//     */
//    public void sendEmail(String toEmail, String subject, String content) {
//        String title = "[AutoCafeWriter] : ";
//        String basicContent = "Test 입니다. ";
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(toEmail);
//        message.setSubject(title + subject);
//        message.setText(basicContent + content);
//
//        mailSender.send(message);
//        System.out.println("이메일 전송 완료");
//    }
//
// 테스트용 main
//
//    public static void main(String[] args) {
//        EmailSender sender = new EmailSender();
//        // 조건 : 몇번 이상 메일 이미 보냈음. 마지막 회수가 되면 마지막 메일이라고 알려주고 이제 그만 보내야함.
//        sender.sendEmail("a07308@naver.com", "테스트 제목", "테스트 내용입니다.");
//    }