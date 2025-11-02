package org.justdoit.blog.dto.post;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PostAiDto {
    private String cafeName;
    private String cafeId;

    private String cafeBoardTag;
    private String cafeBoardId;

    private String subject;
    private String prompt;
    private String title;
    private String imgUrls;
    private String contentHtml;
}
