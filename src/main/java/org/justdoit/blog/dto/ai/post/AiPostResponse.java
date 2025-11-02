package org.justdoit.blog.dto.ai.post;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AiPostResponse {
    private String title;
    private String content;
    private List<String> images;
}