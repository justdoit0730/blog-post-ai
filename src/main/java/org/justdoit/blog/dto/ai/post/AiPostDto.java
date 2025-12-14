package org.justdoit.blog.dto.ai.post;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class AiPostDto {
    private String subject;           // 글 제목
    private String prompt;            // 요청 프롬프트
    private MultipartFile[] images;
    private List<String> preSignedUrls;
}
