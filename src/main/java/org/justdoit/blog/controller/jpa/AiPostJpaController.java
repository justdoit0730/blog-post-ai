package org.justdoit.blog.controller.jpa;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.justdoit.blog.config.auth.SessionUser;
import org.justdoit.blog.entity.cafe.ai.CafeAiPosting;
import org.justdoit.blog.entity.cafe.ai.CafeAiPostingRepository;
import org.justdoit.blog.service.s3.S3Service;
import org.justdoit.blog.template.S3Prefix;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/cafe")
public class AiPostJpaController {
    private final CafeAiPostingRepository cafeAiPostingRepository;

    private final HttpSession httpSession;
    private final S3Service s3Service;

    // 글 삭제
    @PostMapping("/ai/post/delete/{id}")
    public ResponseEntity<String> deletePost(@PathVariable Long id) {
        SessionUser sessionUser = getSessionUser(httpSession);
        Optional<CafeAiPosting> cafeAiPosting = cafeAiPostingRepository.findById(id);
        if (cafeAiPosting.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("F");
        }

        CafeAiPosting cafePosting = cafeAiPosting.get();
        List<String> fileNames = Arrays.stream(cafePosting.getImgList().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        if (!fileNames.isEmpty()) {
            s3Service.deleteImages(sessionUser, fileNames, S3Prefix.POST.getPrefix());
        }
        cafeAiPostingRepository.delete(cafePosting);

        return ResponseEntity.ok("T");
    }


    private SessionUser getSessionUser(HttpSession session) {
        return Optional
                .ofNullable((SessionUser) session.getAttribute("basicUser"))
                .orElse((SessionUser) session.getAttribute("user"));
    }
}