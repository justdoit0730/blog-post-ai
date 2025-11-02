package org.justdoit.blog.template;

import lombok.Getter;

@Getter
public enum S3Prefix {
    WRITE("write","AI 글 생성 목록 이미지 디렉터리"),
    WRITE_CACHE("writeCache","AI 글 생성 화면 출력용 이미지 디렉터리"),

    POST("post","카페 게시글 용 이미지 디렉터리"),
    POST_CACHE("postCache","카페 게시글 예비 이미지 디렉터리"),

    AI_POST("aiPost","카페 AI 게시글 용 이미지 디렉터리"),
    AI_POST_CACHE("aiPostCache","카페 AI 게시글 예비 이미지 디렉터리"),
    AI_POST_GENERATION_CACHE("aiGenerationPostCache","카페 AI 게시글 최초 생성 이미지 디렉터리");

    private final String prefix;
    private final String summary;

    S3Prefix(String prefix, String summary) {
        this.prefix = prefix;
        this.summary = summary;
    }
}
