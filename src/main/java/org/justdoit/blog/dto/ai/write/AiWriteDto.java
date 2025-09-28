package org.justdoit.blog.dto.ai.write;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AiWriteDto {
    private String subject;           // 글 제목
    private String prompt;            // 요청 프롬프트
    private List<String> images = new ArrayList<>();      // Base64 이미지 문자열 배열
    private List<String> preSignedUrls = new ArrayList<>();
}
