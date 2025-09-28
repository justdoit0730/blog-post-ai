package org.justdoit.blog.variable;

import org.springframework.mail.javamail.JavaMailSenderImpl;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

public class GlobalVariables {
    public String server = "";
    public String S3_ACCESS_KEY = "";
    public String S3_ACCESS_PASSWORD = "";
    public String S3_BUCKET_NAME = "";
    public S3Client s3Client;
    public S3Presigner s3Presigner;

    public int WRITE_TEMPLATE_MAX_ROW = 10;
    public int AVAILABLE_TOKEN_USER = 10000;
    public int AVAILABLE_TOKEN_POWER_USER = 50000;

    public String EMAIL_KEY;
    public JavaMailSenderImpl M_SENDER = null;
    public String MAIN_EMAIL;
    public String SEND_EMAIL;
    public boolean EMAIL_DEBUG;

    public String AES_KEY;

    public String AI_KEY;
}
