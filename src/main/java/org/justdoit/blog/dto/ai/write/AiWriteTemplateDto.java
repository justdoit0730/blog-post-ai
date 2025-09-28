package org.justdoit.blog.dto.ai.write;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AiWriteTemplateDto {
    private String tag;               // 태그
    private String subject;           // 글 제목
    private String prompt;            // 요청 프롬프트
}
