package org.justdoit.blog.jpa;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.justdoit.blog.dto.community.NoticeDto;
import org.justdoit.blog.entity.commnity.Notice;
import org.justdoit.blog.entity.commnity.NoticeRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeJpaService {
    private final NoticeRepository noticeRepository;

    @Transactional
    public String save(NoticeDto noticeDto) {
        Notice notice = Notice.builder()
                .subject(noticeDto.getNoticeSubject())
                .title(noticeDto.getNoticeTitle())
                .content(noticeDto.getNoticeContent())
                .build();
        noticeRepository.save(notice);
        return "T";
    }

    @Transactional
    public String delete(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("F"));

        noticeRepository.delete(notice);
        return "T";
    }

}

