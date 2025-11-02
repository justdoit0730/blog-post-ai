package org.justdoit.blog.template;

public enum EmailTemplate {
    REFRESH_TOKEN_FAIL("Failed to Refresh Access Token ", "The token verified from the client information is invalid.\n" +
            "Please review and update the client information.\n" +
            "After a maximum of 5 warning emails, the client information will no longer be allowed to perform normal operations."),
    TEST2("", "테스트 내용입니다.2"),
    TEST3("테스트 제목3", "테스트 내용입니다.3");

    private final String subject;
    private final String content;

    EmailTemplate(String subject, String content) {
        this.subject = subject;
        this.content = content;
    }

    public String getSubject() {
        return subject;
    }

    public String getContent() {
        return content;
    }
}
