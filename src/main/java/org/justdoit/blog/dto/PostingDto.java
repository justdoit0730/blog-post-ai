package org.justdoit.blog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostingDto {
    // 카페 아이디
    private String cafeId;

    // 카페 이름
    private String cafeName;

    // 카테고리 아이디
    private String cafeMenuId;

    // 주제
    private String subject;

    // 말투
    private String template;

}
