package org.justdoit.blog.jpa;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.justdoit.blog.config.auth.SessionUser;
import org.justdoit.blog.dto.user.ClientDto;
import org.justdoit.blog.entity.manager.ManagerInfo;
import org.justdoit.blog.entity.manager.ManagerInfoRepository;
import org.justdoit.blog.utils.CryptUtils;
import org.justdoit.blog.variable.GlobalVariables;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ManagerJpaService {
    private final ManagerInfoRepository managerInfoRepository;
    private final GlobalVariables globalVariables;
    private final CryptUtils cryptUtils;

    @Transactional
    public String clientUpdate(SessionUser sessionUser, ClientDto clientDto) {
        String email = globalVariables.MAIN_EMAIL;
        ManagerInfo managerInfo;
        managerInfo = managerInfoRepository.findByMainEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        if (managerInfo == null) {
            return "D-C-F001"; // Manager Info Table 에 main email로 된 row를 못찾음.
        }

        managerInfo.setCafeClientId(cryptUtils.encrypt256(clientDto.getClientId()));
        managerInfo.setCafeClientSecret(cryptUtils.encrypt256(clientDto.getClientSecret()));
        managerInfo.setCafeRefreshToken(cryptUtils.encrypt256(sessionUser.getCafeRefreshToken()));
        managerInfo.setCafeRefreshTokenExpiresAt(sessionUser.getCafeRefreshTokenExpiresAt());
        return "T";
    }

    @Transactional
    public String clientClear() {
        String email = globalVariables.MAIN_EMAIL;
        ManagerInfo managerInfo;
        managerInfo = managerInfoRepository.findByMainEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        if (managerInfo == null) {
            return "D-C-F001"; // Manager Info Table 에 main email로 된 row를 못찾음.
        }

        managerInfo.setCafeClientId(null);
        managerInfo.setCafeClientSecret(null);
        managerInfo.setCafeRefreshToken(null);
        managerInfo.setCafeRefreshTokenExpiresAt(0);
        return "T";
    }

}

