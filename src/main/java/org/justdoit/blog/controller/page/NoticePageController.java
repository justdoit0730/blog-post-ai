package org.justdoit.blog.controller.page;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.justdoit.blog.entity.cafe.CafePosting;
import org.justdoit.blog.entity.commnity.Notice;
import org.justdoit.blog.entity.commnity.NoticeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Controller
public class NoticePageController {
    private final NoticeRepository noticeRepository;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());

    @GetMapping("/community/notice/{id}")
    public String noticeInfoPage(@PathVariable Long id, HttpServletRequest request, Model model) {

        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 글이 없습니다. id=" + id));

        model.addAttribute("id", notice.getId());
        model.addAttribute("subject", notice.getSubject());
        model.addAttribute("title", notice.getTitle());
        model.addAttribute("content", notice.getContent());
        model.addAttribute("date", dateFormatter.format(notice.getCreatedAt()));

        return "community/notice-info";
    }

}

