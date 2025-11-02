package org.justdoit.blog.dto.post;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostBasicDto {
    private String cafeName;
    private String cafeId;

    private String cafeBoardTag;
    private String cafeBoardId;

    private String title;
    private String contentHtml;
}
