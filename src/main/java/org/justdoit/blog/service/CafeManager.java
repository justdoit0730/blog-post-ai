package org.justdoit.blog.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.justdoit.blog.configuration.app.AppMetadata;
import org.justdoit.blog.configuration.manager.AwsS3Metadata;
import org.justdoit.blog.configuration.manager.EmailMetadata;
import org.justdoit.blog.configuration.manager.WriteMetadata;
import org.justdoit.blog.entity.manager.ManagerInfo;
import org.justdoit.blog.entity.manager.ManagerInfoRepository;
import org.justdoit.blog.service.cafe.CafeTokenService;
import org.justdoit.blog.variable.GlobalVariables;
import org.justdoit.blog.utils.CryptUtils;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.util.Properties;

@Slf4j
@Service
@RequiredArgsConstructor
public class CafeManager {
    private final ManagerInfoRepository managerInfoRepository;
    private final GlobalVariables globalVariables;

    private final AppMetadata appMetadata;
    private final AwsS3Metadata awsS3Metadata;
    private final EmailMetadata emailMetadata;
    private final WriteMetadata writeMetadata;

    private final CryptUtils cryptUtils;

    private final CafeTokenService cafeTokenService;

    @PostConstruct
    public void init() {
        loadManagerInfo();
    }

    private void loadManagerInfo() {
        try {
            globalVariables.server = appMetadata.getServer();
            ManagerInfo managerInfo = managerInfoRepository.findById("default").orElse(null);
            if (managerInfo != null) {
                globalVariables.EMAIL_KEY = managerInfo.getEmailKey();
                globalVariables.AES_KEY = managerInfo.getAesKey();

                globalVariables.MAIN_EMAIL = (managerInfo.getMainEmail() != null)
                        ? managerInfo.getMainEmail()
                        : (emailMetadata != null ? emailMetadata.getMainEmail() : "");

                globalVariables.SEND_EMAIL = (managerInfo.getSendEmail() != null)
                        ? managerInfo.getSendEmail()
                        : (emailMetadata != null ? emailMetadata.getSendEmail() : "");

                // 이메일 설정
                if (globalVariables.EMAIL_KEY != null) {
                    globalVariables.M_SENDER = new JavaMailSenderImpl();
                    globalVariables.M_SENDER.setHost("smtp.gmail.com");
                    globalVariables.M_SENDER.setPort(465);
                    globalVariables.M_SENDER.setUsername(globalVariables.MAIN_EMAIL);
                    globalVariables.M_SENDER.setPassword(cryptUtils.decrypt256(globalVariables.EMAIL_KEY));

                    Properties props = globalVariables.M_SENDER.getJavaMailProperties();
                    props.put("mail.transport.protocol", "smtp");
                    props.put("mail.smtp.auth", "true");
                    props.put("mail.smtp.ssl.enable", "true");
                    props.put("mail.smtp.starttls.enable", "true");
                    props.put("mail.debug", globalVariables.EMAIL_DEBUG);
                }

                // AWS S3 Access info
                globalVariables.S3_ACCESS_KEY = cryptUtils.decrypt256(managerInfo.getS3Key());
                globalVariables.S3_ACCESS_PASSWORD = cryptUtils.decrypt256(managerInfo.getS3Password());
                globalVariables.S3_BUCKET_NAME = awsS3Metadata.getBucketName();

                if (globalVariables.S3_ACCESS_KEY != null && globalVariables.S3_ACCESS_PASSWORD != null) {
                    AwsBasicCredentials awsCreds = AwsBasicCredentials.create(globalVariables.S3_ACCESS_KEY, globalVariables.S3_ACCESS_PASSWORD);
                    globalVariables.s3Client = S3Client.builder()
                            .region(awsS3Metadata.getRegion())
                            .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                            .build();

                    globalVariables.s3Presigner = S3Presigner.builder()
                            .region(awsS3Metadata.getRegion())
                            .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                            .build();
                }

                // AI KEY 설정
                globalVariables.AI_KEY = cryptUtils.decrypt256(managerInfo.getAiKey());

                // Write Template row 최대 생성 개수
                globalVariables.WRITE_TEMPLATE_MAX_ROW = writeMetadata.getMaxRows();

                // 일일 TOKEN 사용량 기본값 설정
                globalVariables.AVAILABLE_TOKEN_USER = writeMetadata.getAvailableTokenPerDay().get("user");
                globalVariables.AVAILABLE_TOKEN_POWER_USER = writeMetadata.getAvailableTokenPerDay().get("power-user");

            } else {
                globalVariables.EMAIL_KEY = null;
                globalVariables.AES_KEY = null;

                globalVariables.MAIN_EMAIL = (emailMetadata != null) ? emailMetadata.getMainEmail() : "";
                globalVariables.SEND_EMAIL = (emailMetadata != null) ? emailMetadata.getSendEmail() : "";
            }
            String source = managerInfo != null ? "Database" : "Application.yml";

            // 로그 출력
            log.info("===========================================");
            log.info("         [Manager info load Result]     ");
            log.info("Source of Info       : {}", source);
            log.info(String.format("%-20s : %s", "EMAIL Enabled", globalVariables.EMAIL_KEY != null));
            log.info(String.format("%-20s : %s", "MAIN_EMAIL", globalVariables.MAIN_EMAIL));
            log.info(String.format("%-20s : %s", "SEND_EMAIL", globalVariables.SEND_EMAIL));
            log.info(String.format("%-20s : %s", "EMAIL Debug", emailMetadata.isDebug()));
            log.info(String.format("%-20s : %s", "AES_KEY exists", globalVariables.AES_KEY != null));
            log.info(String.format("%-20s : %s", "AI_KEY exists", globalVariables.AI_KEY != null));
            log.info(String.format("%-20s : %s", "S3 Enabled", globalVariables.s3Client != null && globalVariables.s3Presigner != null));
//            log.info("OpenAiService exists      : {}", globalVariables.openAiService != null);
            log.info("===========================================");

        } catch (Exception e) {
            log.warn("Manager settings are not available in both the database and the YAML configuration.", e);
        }
    }






//    @Scheduled(fixedRate = 5 * 60 * 1000)
//    public void reloadConfig() {
//        reloadManagerConfig();
//    }
//    private void reloadManagerConfig() {
//        ManagerConfig managerConfig = managerConfigRepository.findById("default").orElse(null);
//
//        if (managerConfig != null) {
//            globalMutableVariables.AVAILABLE_TOKENS_PER_DAY = managerConfig.getAvailableTokensPerDay();
//        }
//
//        String source = managerConfig != null ? "Database" : "Hard coding";
//
//        log.info("===========================================");
//        log.info("         [Manager Config load Result]     ");
//        log.info("Source of Info  : {}", source);
//        log.info("AVAILABLE_TOKENS_PER_DAY      : {}", globalMutableVariables.AVAILABLE_TOKENS_PER_DAY);
//        log.info("MAX_WRITE_TEMPLATE_ROWS      : {}", globalMutableVariables.MAX_WRITE_TEMPLATE_ROWS);
//        log.info("===========================================");
//    }

}
