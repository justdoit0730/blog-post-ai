package org.justdoit.blog.service.email;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.justdoit.blog.config.auth.SessionUser;
import org.justdoit.blog.dto.community.InquiryDto;
import org.justdoit.blog.variable.GlobalVariables;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSender {
    private final GlobalVariables globalVariables;

    public void refreshTokenFailSendEmail(SessionUser sessionUser) {
        String email = sessionUser.isSubEmailUsed() && !sessionUser.getReceiveEmail().isEmpty() ? sessionUser.getReceiveEmail() : sessionUser.getEmail();
        String title = "[AUTO CAFE] : Naver Client API Access Token 갱신 실패 안내";
        String htmlContent =
                "<div style='font-family: Arial, sans-serif; padding:20px; background-color:#f9f9f9;'>"
                        + "  <div style='max-width:600px; margin:auto; background:white; border-radius:10px; "
                        + "       box-shadow:0 2px 8px rgba(0,0,0,0.1); padding:30px;'>"
                        + "    <h2 style='color:#D32F2F; text-align:center;'>Naver Client API Access Token 갱신 실패</h2>"
                        + "    <p style='font-size:16px; color:#555; line-height:1.6;'>"
                        + "      안녕하세요. AUTO CAFE 입니다.<br><br>"
                        + "      등록된 클라이언트의 Access Token이 유효하지 않아 갱신에 실패했습니다.<br><br>"
                        + "      아래 내용을 확인하신 후, API 인증 정보를 다시 설정해 주세요.<br><br>"
                        + "      <strong style='color:#D32F2F;'>⚠️ 주의: 동일한 경고 메일이 최대 5회까지 발송된 후,</strong><br>"
                        + "      해당 클라이언트 정보로는 정상적인 작업을 수행할 수 없습니다.<br><br>"
                        + "      발송 대상 이메일 : <strong>" + email + "</strong><br>"
                        + "      발송 일시 : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        + "    </p>"
                        + "    <hr style='margin:30px 0; border:none; border-top:1px solid #eee;'>"
                        + "    <p style='font-size:12px; color:#999; text-align:center;'>"
                        + "      본 메일은 시스템에 의해 자동 발송되었습니다. 회신하지 마세요.<br>"
                        + "      문의가 필요할 경우 고객센터를 이용해 주세요."
                        + "    </p>"
                        + "    <div style='text-align:center; margin-top:20px;'>"
                        + "      <img src='https://jhhan-s3.s3.ap-northeast-2.amazonaws.com/manager/logo.png' "
                        + "           alt='AUTO CAFE 로고' style='width:120px; height:auto;'>"
                        + "    </div>"
                        + "  </div>"
                        + "</div>";

        try {
            MimeMessage message = globalVariables.M_SENDER.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject(title);
            helper.setText(htmlContent, true);

            globalVariables.M_SENDER.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean checkEmail(String toEmail, String code) {
        String title = "[AUTO CAFE] : 이메일 인증번호";
        String htmlContent =
                "<div style='font-family: Arial, sans-serif; padding:20px; background-color:#f9f9f9;'>"
                        + "  <div style='max-width:600px; margin:auto; background:white; border-radius:10px; "
                        + "       box-shadow:0 2px 8px rgba(0,0,0,0.1); padding:30px;'>"
                        + "    <h2 style='color:#333; text-align:center;'>AUTO CAFE 이메일 인증</h2>"
                        + "    <p style='font-size:16px; color:#555; line-height:1.6;'>"
                        + "      안녕하세요. AUTO CAFE 입니다.<br><br>"
                        + "      아래의 인증번호를 입력하여 이메일 인증을 완료해주세요.<br><br>"
                        + "      <strong style='font-size:22px; color:#007BFF; letter-spacing:2px;'>" + code + "</strong><br>"
                        + "      <span style='color:#888;'>⚠️ 5분 이내에 입력해주세요.</span><br><br>"
                        + "      이메일 : <strong>" + toEmail + "</strong><br>"
                        + "      발송 일시 : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        + "    </p>"
                        + "    <hr style='margin:30px 0; border:none; border-top:1px solid #eee;'>"
                        + "    <p style='font-size:12px; color:#999; text-align:center;'>"
                        + "      본 메일은 발신전용입니다. 문의는 고객센터를 이용해주세요."
                        + "    </p>"
                        + "    <div style='text-align:center; margin-top:20px;'>"
                        + "      <img src='https://jhhan-s3.s3.ap-northeast-2.amazonaws.com/manager/logo.png' alt='AUTO CAFE 로고' style='width:120px; height:auto;'>"
                        + "    </div>"
                        + "  </div>"
                        + "</div>";

        try {
            MimeMessage message = globalVariables.M_SENDER.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject(title);
            helper.setText(htmlContent, true);

            globalVariables.M_SENDER.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean checkSubEmail(SessionUser sessionUser, String code) {
        String toEmail = sessionUser.getEmail();
        String title = "[AUTO CAFE] : 추가 이메일 인증번호";

        String htmlContent =
                "<div style='font-family: Arial, sans-serif; padding:20px; background-color:#f9f9f9;'>"
                        + "  <div style='max-width:600px; margin:auto; background:white; border-radius:10px; "
                        + "       box-shadow:0 2px 8px rgba(0,0,0,0.1); padding:30px;'>"
                        + "    <h2 style='color:#333; text-align:center;'>AUTO CAFE 추가 이메일 인증</h2>"
                        + "    <p style='font-size:16px; color:#555; line-height:1.6;'>"
                        + "      안녕하세요. AUTO CAFE 입니다.<br><br>"
                        + "      아래의 인증번호를 입력하여 추가 이메일 인증을 완료해주세요.<br><br>"
                        + "      <strong style='font-size:22px; color:#007BFF; letter-spacing:2px;'>" + code + "</strong><br>"
                        + "      <span style='color:#888;'>⚠️ 5분 이내에 입력해주세요.</span><br><br>"
                        + "      이메일 : <strong>" + toEmail + "</strong><br>"
                        + "      발송 일시 : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        + "    </p>"
                        + "    <hr style='margin:30px 0; border:none; border-top:1px solid #eee;'>"
                        + "    <p style='font-size:12px; color:#999; text-align:center;'>"
                        + "      본 메일은 발신전용입니다. 문의는 고객센터를 이용해주세요."
                        + "    </p>"
                        + "    <div style='text-align:center; margin-top:20px;'>"
                        + "      <img src='https://jhhan-s3.s3.ap-northeast-2.amazonaws.com/manager/logo.png' "
                        + "           alt='AUTO CAFE 로고' style='width:120px; height:auto;'>"
                        + "    </div>"
                        + "  </div>"
                        + "</div>";

        try {
            MimeMessage message = globalVariables.M_SENDER.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject(title);
            helper.setText(htmlContent, true);

            globalVariables.M_SENDER.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            sessionUser.setSubEmailAuthSuccess(false);
            return false;
        }
        return true;
    }

    // 문의사항
    public String inquiryEmail(InquiryDto inquiryDto) {
        String title = "[AUTO CAFE] 문의 사항 : ";

        String htmlContent =
                "<div style='font-family: Arial, sans-serif; padding:20px; background-color:#f9f9f9;'>"
                        + "  <div style='max-width:600px; margin:auto; background:white; border-radius:10px; "
                        + "       box-shadow:0 2px 8px rgba(0,0,0,0.1); padding:30px;'>"
                        + "    <h2 style='color:#333; text-align:center;'>AUTO CAFE 문의 사항</h2>"
                        + "    <p style='font-size:16px; color:#555; line-height:1.6;'>"
                        + "      안녕하세요. AUTO CAFE 입니다.<br><br>"
                        + "      문의가 접수되었습니다.<br><br>"
                        + "      <strong>문의 제목 :</strong> " + inquiryDto.getInquiryTitle() + "<br>"
                        + "      <strong>문의 이메일 :</strong> " + inquiryDto.getInquiryEmail() + "<br>"
                        + "      <strong>문의 일자 :</strong> " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "<br><br>"
                        + "      <strong>문의 내용 :</strong><br>" + inquiryDto.getInquiryDetails() + "<br>"
                        + "    </p>"
                        + "    <hr style='margin:30px 0; border:none; border-top:1px solid #eee;'>"
                        + "    <p style='font-size:12px; color:#999; text-align:center;'>"
                        + "      본 메일은 발신전용입니다. 문의는 고객센터를 이용해주세요."
                        + "    </p>"
                        + "    <div style='text-align:center; margin-top:20px;'>"
                        + "      <img src='https://jhhan-s3.s3.ap-northeast-2.amazonaws.com/manager/logo.png' alt='AUTO CAFE 로고' style='width:120px; height:auto;'>"
                        + "    </div>"
                        + "  </div>"
                        + "</div>";

        try {
            MimeMessage message = globalVariables.M_SENDER.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(new String[] { globalVariables.SEND_EMAIL, globalVariables.MAIN_EMAIL});
            helper.setSubject(title + inquiryDto.getInquiryTitle());
            helper.setText(htmlContent, true);

            globalVariables.M_SENDER.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            return "문의 처리 간 문제가 발생했습니다. 다시 시도해주세요.";
        }

        return "문의가 정상적으로 접수 되었습니다.";
    }

}