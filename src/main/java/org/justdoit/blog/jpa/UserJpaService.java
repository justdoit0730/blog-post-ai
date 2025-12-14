package org.justdoit.blog.jpa;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.justdoit.blog.config.auth.SessionUser;
import org.justdoit.blog.dto.user.ClientDto;
import org.justdoit.blog.dto.user.SignUpDto;
import org.justdoit.blog.dto.user.UpdateUserDto;
import org.justdoit.blog.entity.user.CafeUser;
import org.justdoit.blog.entity.user.CafeUserRepository;
import org.justdoit.blog.template.Role;
import org.justdoit.blog.utils.CryptUtils;
import org.justdoit.blog.variable.GlobalVariables;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserJpaService implements UserDetailsService {
    private final CafeUserRepository cafeUserRepository;
    private final GlobalVariables globalVariables;
    private final CryptUtils cryptUtils;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        CafeUser cafeUser = cafeUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User.builder()
                .username(cafeUser.getEmail())
                .password(cafeUser.getPassword())
                .roles(cafeUser.getRoleKey())
                .build();
    }

    @Transactional
    public boolean checkEmailDup(String email) {
        if (cafeUserRepository.findByEmail(email).isPresent()) {
            return false;
        }
        return true;
    }

    @Transactional
    public String save(HttpSession session, SignUpDto signUpDto) {
        CafeUser cafeUser = CafeUser.builder()
                .email(session.getAttribute("email").toString())
                .password(passwordEncoder.encode(signUpDto.getPassword()))
                .isEmailPrivacyAgreed(true)
                .isClientPrivacyAgreed(false)
                .role(session.getAttribute("email").toString().equals(globalVariables.MAIN_EMAIL) ? Role.MANAGER : Role.USER)
                .build();
        cafeUserRepository.save(cafeUser);
        return "T";
    }

    @Transactional
    public String subEmailUpdate(SessionUser sessionUser, UpdateUserDto updateUserDto) {
        CafeUser cafeUser;
        String email = sessionUser.getEmail();
        cafeUser = cafeUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        if (cafeUser == null) {
            return "D-C-F001"; // email 인 유저가 client 정보를 업데이트하려는 데 회원정보(cafe_user)에 없는 사람이어서 업데이트 취소됨.
        }
        cafeUser.setSubEmail(updateUserDto.getSubEmail());
        cafeUser.setSubEmailUsed(updateUserDto.isSubEmailUsed());
        return "T";
    }

    @Transactional
    public String passwordUpdate(SessionUser sessionUser, UpdateUserDto updateUserDto) {
        CafeUser cafeUser;
        String email = sessionUser.getEmail();
        cafeUser = cafeUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        if (cafeUser == null) {
            return "D-C-F001"; // email 인 유저가 client 정보를 업데이트하려는 데 회원정보(cafe_user)에 없는 사람이어서 업데이트 취소됨.
        }
        cafeUser.setPassword(passwordEncoder.encode(updateUserDto.getPassword()));
        return "T";
    }

    @Transactional
    public String clientUpdate(SessionUser sessionUser, ClientDto clientDto) {
        String email = sessionUser.getEmail();
        CafeUser cafeUser;

        if (sessionUser.getCafeRefreshToken() == null) {
            return "D-C-F002"; // email 인 유저가 client 정보를 업데이트하려는 데 refreshToken이 없는 사람이어서 업데이트 취소됨. -> 재인증필요
        }

        cafeUser = cafeUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        if (cafeUser == null) {
            return "D-C-F001"; // email 인 유저가 client 정보를 업데이트하려는 데 회원정보(cafe_user)에 없는 사람이어서 업데이트 취소됨.
        }

        cafeUser.setClientApiEnabled(true);
        cafeUser.setCafeClientId(cryptUtils.encrypt256(clientDto.getClientId()));
        cafeUser.setCafeClientSecret(cryptUtils.encrypt256(clientDto.getClientSecret()));
        cafeUser.setCafeRefreshToken(cryptUtils.encrypt256(sessionUser.getCafeRefreshToken()));
        cafeUser.setCafeRefreshTokenExpiresAt(sessionUser.getCafeTokenExpiresAt());
        cafeUser.setClientPrivacyAgreed(true);

        sessionUser.setCafeClientId(clientDto.getClientId());
        sessionUser.setClientApiEnabled(true);
        sessionUser.setCafeClientSecret(cryptUtils.encrypt256(clientDto.getClientSecret()));
        sessionUser.setClientPrivacyAgreed(true);

        return "T";
    }

    @Transactional
    public String clientClear(SessionUser sessionUser) {
        String email = sessionUser.getEmail();
        CafeUser cafeUser;
        cafeUser = cafeUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        if (cafeUser == null) {
            return "D-C-F001"; // email 인 유저가 client 정보를 업데이트하려는 데 회원정보(cafe_user)에 없는 사람이어서 업데이트 취소됨.
        }

        cafeUser.setCafeClientId(null);
        cafeUser.setCafeClientSecret(null);
        cafeUser.setCafeRefreshToken(null);
        cafeUser.setCafeRefreshTokenExpiresAt(0);
        cafeUser.setClientPrivacyAgreed(false);
        cafeUser.setClientApiEnabled(false);
        return "T";
    }
}

