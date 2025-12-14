package org.justdoit.blog.controller.manager;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.justdoit.blog.config.auth.SessionUser;
import org.justdoit.blog.dto.community.NoticeDto;
import org.justdoit.blog.entity.commnity.Notice;
import org.justdoit.blog.entity.commnity.NoticeRepository;
import org.justdoit.blog.jpa.NoticeJpaService;
import org.justdoit.blog.template.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RequiredArgsConstructor
@Controller
public class CafeManagerController {
    private final NoticeRepository noticeRepository;
    private final NoticeJpaService noticeJpaService;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());
    private final HttpSession httpSession;


    // 공지사항 작성, 삭제하는 페이지
    @GetMapping("/manager/notice")
    public String noticeManagerPage(Model model, @RequestParam(value = "page", defaultValue = "1") int page) {
        SessionUser sessionUser = getSessionUser(httpSession);
        if (!sessionUser.getRole().equals(Role.MANAGER.getKey())) {
            return "error/error-alert";
        }

        int pageSize = 10;
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), pageSize);

        Page<Notice> noticePage = noticeRepository.findAllByOrderByCreatedAtDesc(pageable);

        int startNo = (page - 1) * pageSize + 1;
        List<Map<String,Object>> writeList = new ArrayList<>();
        int index = 0;
        for(Notice notice : noticePage.getContent()) {
            Map<String,Object> map = new HashMap<>();
            map.put("no", startNo + index++);
            map.put("id", notice.getId());
            map.put("subject", notice.getSubject());
            map.put("title", notice.getTitle());
            map.put("content", notice.getContent());
            map.put("date", dateFormatter.format(notice.getCreatedAt()));
            writeList.add(map);
        }
        model.addAttribute("notice", writeList);
        model.addAttribute("count", noticePage.getContent().size());

        int totalPages = noticePage.getTotalPages();
        int startPage = Math.max(1, page - 2);
        int endPage = Math.min(totalPages, page + 2);

        if (endPage - startPage + 1 < 5) {
            if (startPage == 1) {
                endPage = Math.min(5, totalPages);
            } else if (endPage == totalPages) {
                startPage = Math.max(1, totalPages - 4);
            }
        }

        List<Map<String,Object>> pages = new ArrayList<>();
        for(int i=startPage; i<=endPage; i++) {
            Map<String,Object> p = new HashMap<>();
            p.put("num", i);
            p.put("isCurrent", i == page);
            pages.add(p);
        }
        model.addAttribute("pages", pages);
        model.addAttribute("prevPage", page > 1 ? page - 1 : 1);
        model.addAttribute("nextPage", page < totalPages ? page + 1 : totalPages);
        model.addAttribute("totalPages", totalPages);

        return "/community/manager-notice";
    }

    //    공지사항 게시글 저장 및 수정
    @PostMapping("/manager/notice/save")
    public ResponseEntity<String> noticeSave(@RequestBody NoticeDto noticeDto) {
        SessionUser sessionUser = getSessionUser(httpSession);
        if (!sessionUser.getRole().equals(Role.MANAGER.getKey())) {
            return ResponseEntity.ok("F");
        }

        String result = noticeJpaService.save(noticeDto);
        return ResponseEntity.ok(result);
    }

    //    공지사항 게시글 삭제
    @DeleteMapping("/manager/notice/delete/{id}")
    public ResponseEntity<String> noticeDelete(@PathVariable Long id) {
        SessionUser sessionUser = getSessionUser(httpSession);
        if (!sessionUser.getRole().equals(Role.MANAGER.getKey())) {
            return ResponseEntity.ok("F");
        }

        String result = noticeJpaService.delete(id);
        return ResponseEntity.ok(result);
    }

    private SessionUser getSessionUser(HttpSession session) {
        return Optional
                .ofNullable((SessionUser) session.getAttribute("basicUser"))
                .orElse((SessionUser) session.getAttribute("user"));
    }

    @GetMapping("/black")
    public String handleBlack() {
        return "error/black-alert";
    }
}
