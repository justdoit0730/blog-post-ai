package org.justdoit.blog.controller.cafe;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.justdoit.blog.config.auth.SessionUser;
import org.justdoit.blog.dto.post.PostBasicDto;
import org.justdoit.blog.entity.cafe.CafePosting;
import org.justdoit.blog.entity.cafe.CafePostingRepository;
import org.justdoit.blog.entity.manager.ManagerInfo;
import org.justdoit.blog.entity.manager.ManagerInfoRepository;
import org.justdoit.blog.entity.user.CafeUser;
import org.justdoit.blog.entity.user.CafeUserRepository;
import org.justdoit.blog.service.cafe.CafeTokenService;
import org.justdoit.blog.service.cafe.PostService;
import org.justdoit.blog.service.s3.S3Service;
import org.justdoit.blog.template.S3Prefix;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// @Controller 는 html 렌더링에 사용된다.
@RestController
@RequiredArgsConstructor
@RequestMapping("/cafe")
public class CafeController {
    private final PostService postService;
    private final CafeUserRepository userRepository;
    private final CafeTokenService cafeTokenService;

    private final ManagerInfoRepository managerInfoRepository;

    private final CafePostingRepository cafePostingRepository;

    private final S3Service s3Service;

    @PostMapping("/uploadCacheImages")
    @ResponseBody
    public List<String> cafePostUploadCacheImages(HttpSession session, @RequestBody Map<String, List<String>> request) {
        SessionUser sessionUser = (SessionUser) session.getAttribute("user");
        List<String> base64Images = request.get("base64Images");

        s3Service.cleanS3CacheImage(sessionUser, S3Prefix.POST_CACHE.getPrefix());
        return s3Service.uploadImages(sessionUser, base64Images, S3Prefix.POST_CACHE.getPrefix());
    }

    @PostMapping("/uploadImages")
    @ResponseBody
    public List<String> uploadImages(HttpSession session, @RequestBody Map<String, List<String>> request) {
        SessionUser sessionUser = (SessionUser) session.getAttribute("user");
        List<String> base64Images = request.get("base64Images");
        List<String> imgLinkList = s3Service.uploadImages(sessionUser, base64Images, S3Prefix.POST.getPrefix());
        sessionUser.setPostBasicImgList(imgLinkList.stream()
                .map(url -> {
                    int lastSlash = url.lastIndexOf('/');
                    return lastSlash != -1 ? url.substring(lastSlash + 1) : url;
                })
                .collect(Collectors.toList()));

        s3Service.cleanS3CacheImage(sessionUser, S3Prefix.POST_CACHE.getPrefix());
        return imgLinkList;
    }

    @PostMapping("/post")
    public ResponseEntity<String> cafePost(HttpSession session, @RequestBody PostBasicDto postBasicDto) throws IOException {
        SessionUser sessionUser = (SessionUser) session.getAttribute("user");
        String email = sessionUser.getEmail();
        CafeUser cafeUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        if (cafeUser == null) {
            return ResponseEntity.ok("C-F000");
        }

        String accessToken;
        boolean isPostSuccess;
        String result = "F";

        if (cafeUser.isClientApiEnabled()) {
            int validationCount = cafeUser.getCafeValidationFailCount();
            if (validationCount > 5) return ResponseEntity.ok("C-F001");

            accessToken = cafeTokenService.refreshAccessToken(cafeUser, sessionUser);
        } else {
            ManagerInfo managerInfo = managerInfoRepository.findById("default")
                    .orElseThrow(() -> new IllegalStateException("ManagerInfo not found"));
            int validationCount = managerInfo.getCafeValidationFailCount();
            if (validationCount > 5) return ResponseEntity.ok("C-F001");

            accessToken = cafeTokenService.managerRefreshAccessToken(managerInfo);
        }

        if (accessToken == null) {
            return ResponseEntity.ok("C-F001");
        }

        isPostSuccess = postService.postHtmlArticle(sessionUser, accessToken, postBasicDto);

        if (isPostSuccess) {
            CafePosting cafePosting = CafePosting.builder()
                    .cafeUser(cafeUser)
                    .cafeName(postBasicDto.getCafeName())
                    .cafeId(postBasicDto.getCafeId())
                    .cafeBoardTag(postBasicDto.getCafeBoardTag())
                    .cafeBoardId(postBasicDto.getCafeBoardId())
                    .title(postBasicDto.getTitle())
                    .contentHtml(postBasicDto.getContentHtml())
                    .imgList(sessionUser.getPostBasicImgList() != null ? String.join(",", sessionUser.getPostBasicImgList()) : "")
                    .cafeBoardLink(sessionUser.getCafeBoardLink())
                    .build();
            cafePostingRepository.save(cafePosting);
            result = "T";
        }

        return ResponseEntity.ok(result);
    }
}