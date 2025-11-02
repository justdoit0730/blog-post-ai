package org.justdoit.blog.controller.jpa;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.justdoit.blog.config.auth.SessionUser;
import org.justdoit.blog.dto.ai.write.AiWriteSaveDto;

import org.justdoit.blog.entity.ai.AiWrite;
import org.justdoit.blog.entity.ai.AiWriteRepository;
import org.justdoit.blog.jpa.AiWriteJpaService;

import org.justdoit.blog.service.s3.S3Service;
import org.justdoit.blog.template.S3Prefix;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/write")
public class WriteJpaController {
    private final AiWriteJpaService aiWriteJpaService;
    private final AiWriteRepository aiWriteRepository;
    private final HttpSession httpSession;
    private final S3Service s3Service;

    // AI 글 저장
    @PostMapping("/save")
    public ResponseEntity<String> writeSave(HttpSession session, @RequestBody AiWriteSaveDto aiWriteSaveDto) throws JsonProcessingException {
        SessionUser sessionUser = (SessionUser) session.getAttribute("user");
        String email = sessionUser.getEmail();
        List<String> imgNames = new ArrayList<>();
        s3Service.moveImagesFromWriteCacheToWrite(aiWriteSaveDto);

        for (String url : aiWriteSaveDto.getImgUrlS()) {
            String marker = String.format("images/%s/%s/", email, S3Prefix.WRITE_CACHE.getPrefix());
            int start = url.indexOf(marker);
            if (start != -1) {
                String afterEmailPath = url.substring(start + marker.length());
                String fileName = afterEmailPath.split("\\?")[0];
                imgNames.add(fileName);
            }
        }

        String imgNamesStr = "";
        if (imgNames.size() != 0) {
            imgNamesStr = String.join(", ", imgNames);
        }
        String result = aiWriteJpaService.writeSave(sessionUser, aiWriteSaveDto, imgNamesStr);

        if (!imgNamesStr.isEmpty()) s3Service.cleanS3CacheImage(sessionUser, S3Prefix.WRITE_CACHE.getPrefix());
        sessionUser.setWriteTitle("");
        sessionUser.setWriteContent("");
        sessionUser.setWriteImgUrls(new ArrayList<>());

        return ResponseEntity.ok(result);
    }

    // 글 삭제
    @PostMapping("/delete/{id}")
    public ResponseEntity<String> deleteWrite(@PathVariable Long id) {
        SessionUser sessionUser = getSessionUser(httpSession);
        Optional<AiWrite> writeOpt = aiWriteRepository.findById(id);
        if (writeOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("F");
        }

        AiWrite write = writeOpt.get();
        List<String> fileNames = Arrays.stream(write.getImgList().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        if (!fileNames.isEmpty()) {
            s3Service.deleteImages(sessionUser, fileNames, S3Prefix.WRITE.getPrefix());
        }
        aiWriteRepository.delete(write);

        return ResponseEntity.ok("T");
    }


    private SessionUser getSessionUser(HttpSession session) {
        return Optional
                .ofNullable((SessionUser) session.getAttribute("basicUser"))
                .orElse((SessionUser) session.getAttribute("user"));
    }
}