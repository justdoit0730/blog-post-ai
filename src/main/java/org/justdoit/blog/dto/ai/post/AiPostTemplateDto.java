package org.justdoit.blog.dto.ai.post;

import lombok.Getter;
import lombok.Setter;
import org.justdoit.blog.dto.TemplateDtoInterface;

@Getter
@Setter
public class AiPostTemplateDto implements TemplateDtoInterface {
    private String tag;               // 태그
    private String subject;           // 글 제목
    private String prompt;            // 요청 프롬프트
}
