package org.justdoit.blog.controller.write;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.justdoit.blog.config.auth.SessionUser;
import org.justdoit.blog.dto.ai.post.AiPostDto;
import org.justdoit.blog.dto.ai.post.AiPostResponse;
import org.justdoit.blog.dto.ai.write.AiWriteDto;
import org.justdoit.blog.dto.ai.write.AiWriteResponse;
import org.justdoit.blog.service.ai.AiService;
import org.justdoit.blog.service.s3.S3Service;
import org.justdoit.blog.template.S3Prefix;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/feature")
public class WriteController {

    private final AiService aiService;
    private final S3Service s3Service;

    // AI 글 생성
    @PostMapping("/write")
    public ResponseEntity<AiWriteResponse> write(HttpSession session, @RequestBody AiWriteDto aiWriteDto) {
        SessionUser sessionUser = (SessionUser) session.getAttribute("user");
        AiWriteResponse response;

        if (aiWriteDto.getImages().isEmpty()) {
            response = aiService.generateArticle(sessionUser, aiWriteDto);
        } else {
            aiWriteDto.setPreSignedUrls(s3Service.uploadImages(sessionUser, aiWriteDto.getImages(), S3Prefix.WRITE_CACHE.getPrefix()));
            response = aiService.generateArticleWithImages(sessionUser, aiWriteDto);
        }
        return ResponseEntity.ok(response);
    }

    // AI 게시글 생성
    @PostMapping("/post")
    public ResponseEntity<AiPostResponse> post(HttpSession session, @RequestBody AiPostDto aiPostDto) {
        SessionUser sessionUser = (SessionUser) session.getAttribute("user");
        AiPostResponse response;

        if (aiPostDto.getImages().isEmpty()) {
            response = aiService.generatePostArticle(sessionUser, aiPostDto);
        } else {
            s3Service.cleanS3CacheImage(sessionUser, S3Prefix.AI_POST_GENERATION_CACHE.getPrefix());
            aiPostDto.setPreSignedUrls(s3Service.uploadImages(sessionUser, aiPostDto.getImages(), S3Prefix.AI_POST_GENERATION_CACHE.getPrefix()));
            response = aiService.generatePostArticleWithImages(sessionUser, aiPostDto);
        }
        return ResponseEntity.ok(response);
    }
}
