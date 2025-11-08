package org.justdoit.blog.controller.cafe.ai;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.justdoit.blog.config.auth.SessionUser;
import org.justdoit.blog.dto.post.PostAiDto;
import org.justdoit.blog.entity.cafe.ai.CafeAiPosting;
import org.justdoit.blog.entity.cafe.ai.CafeAiPostingRepository;
import org.justdoit.blog.entity.manager.ManagerInfo;
import org.justdoit.blog.entity.user.CafeUser;
import org.justdoit.blog.entity.user.CafeUserRepository;
import org.justdoit.blog.service.cafe.CafeTokenService;
import org.justdoit.blog.service.cafe.PostService;
import org.justdoit.blog.service.s3.S3Service;
import org.justdoit.blog.template.S3Prefix;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class AiCafeController {
    private final PostService postService;
    private final CafeUserRepository userRepository;
    private final CafeTokenService cafeTokenService;
    private final S3Service s3Service;

    private final CafeAiPostingRepository cafeAiPostingRepository;

    @PostMapping("/cafe/ai/uploadImages")
    @ResponseBody
    public List<String> uploadImages(HttpSession session, @RequestBody Map<String, List<String>> request) {
        SessionUser sessionUser = (SessionUser) session.getAttribute("user");
        List<String> base64Images = request.get("base64Images");
        List<String> imgLinkList = s3Service.uploadImages(sessionUser, base64Images, S3Prefix.AI_POST.getPrefix());

        System.out.println("uploadImages 실행 여부?");
        sessionUser.setPostAiImgList(imgLinkList.stream()
                .map(url -> {
                    int lastSlash = url.lastIndexOf('/');
                    return lastSlash != -1 ? url.substring(lastSlash + 1) : url;
                })
                .collect(Collectors.toList()));

        s3Service.cleanS3CacheImage(sessionUser, S3Prefix.AI_POST_GENERATION_CACHE.getPrefix());

        return imgLinkList;
    }

    @PostMapping("/cafe/ai/post")
    public ResponseEntity<String> cafePost(HttpSession session, @RequestBody PostAiDto postAiDto) throws IOException {
        SessionUser sessionUser = (SessionUser) session.getAttribute("user");
        String email = sessionUser.getEmail();
        CafeUser cafeUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        if (cafeUser == null) {
            return ResponseEntity.ok("C-F000");
        }

        // S3 directory 변경
        if (postAiDto.getImgUrls() != null) s3Service.moveImagesFromWriteCacheToWrite(postAiDto);
        s3Service.cleanS3CacheImage(sessionUser, S3Prefix.AI_POST_GENERATION_CACHE.getPrefix());

        // Naver cafe 게시
        String accessToken;
        boolean isPostSuccess;
        String result = "F";

        int validationCount = cafeUser.getCafeValidationFailCount();
        if (validationCount > 5) return ResponseEntity.ok("C-F001");
        accessToken = cafeTokenService.refreshAccessToken(cafeUser, sessionUser);

        if (accessToken == null) {
            return ResponseEntity.ok("C-F001");
        }

        isPostSuccess = postService.postHtmlArticle(sessionUser, accessToken, postAiDto);

        if (isPostSuccess) {
            CafeAiPosting cafeAiPosting = CafeAiPosting.builder()
                    .cafeUser(cafeUser)
                    .cafeName(postAiDto.getCafeName())
                    .cafeId(postAiDto.getCafeId())
                    .cafeBoardTag(postAiDto.getCafeBoardTag())
                    .cafeBoardId(postAiDto.getCafeBoardId())
                    .subject(postAiDto.getSubject())
                    .prompt(postAiDto.getPrompt())
                    .title(postAiDto.getTitle())
                    .contentHtml(postAiDto.getContentHtml())
                    .imgList(sessionUser.getPostAiImgList() != null ? String.join(",", sessionUser.getPostAiImgList()) : "")
                    .cafeBoardLink(sessionUser.getCafeBoardLink())
                    .build();
            cafeAiPostingRepository.save(cafeAiPosting);
            result = "T";
            sessionUser.setPostContentHtml("");
            sessionUser.setPostTitle("");
            sessionUser.setPostImgUrls(new ArrayList<>());
            sessionUser.setPostAiImgList(new ArrayList<>());
        }

        return ResponseEntity.ok(result);
    }

}