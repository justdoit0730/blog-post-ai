package org.justdoit.blog.dto.ai.write;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AiWriteSaveDto {
    private String title;
    private String subject;
    private String prompt;
    private List<String> imgUrlS = new ArrayList<>();
    private String fullContent;
}
