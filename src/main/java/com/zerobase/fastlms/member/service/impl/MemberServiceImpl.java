package com.zerobase.fastlms.member.service.impl;

import com.zerobase.fastlms.member.repository.MemberRepository;
import com.zerobase.fastlms.component.MailComponents;
import com.zerobase.fastlms.member.exception.MemberNotEmailAuthException;
import com.zerobase.fastlms.member.model.ResetPasswordInput;
import com.zerobase.fastlms.member.service.MemberService;
import com.zerobase.fastlms.member.entity.Member;
import com.zerobase.fastlms.member.model.MemberInput;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final MailComponents mailComponents;

    @Override
    public boolean register(MemberInput parameter) {
        Optional<Member> optionalMember = memberRepository.findById(parameter.getUserId());
        if (optionalMember.isPresent()) {
            return false;
        }

        String uuid = UUID.randomUUID().toString();
        String encPassword = BCrypt.hashpw(parameter.getPassword(), BCrypt.gensalt());

        Member member = Member.builder()
                .userId(parameter.getUserId())
                .userName(parameter.getUserName())
                .password(encPassword)
                .phone(parameter.getPhone())
                .regDt(LocalDateTime.now())
                .emailAuthYn(false)
                .emailAuthKey(uuid).build();
        memberRepository.save(member);

        String email = parameter.getUserId();
        String subject = "fastlms ????????? ????????? ??????????????????. ";
        String text = "<p>fastlms ????????? ????????? ??????????????????.</p><p>?????? ????????? ?????????????????? ????????? ???????????????.</p>"
                + "<div><a target='_blank' href='http://localhost:8080/member/email-auth?id=" + uuid + "'> ?????? ?????? </a></div>";
        mailComponents.sendMail(email, subject, text);

        return true;
    }

    @Override
    public boolean emailAuth(String uuid) {
        Optional<Member> optionalMember = memberRepository.findByEmailAuthKey(uuid);
        if (!optionalMember.isPresent()) {
            return false;
        }

        Member member = optionalMember.get();

        if (member.isEmailAuthYn()) {
            return false;
        }

        member.setEmailAuthYn(true);
        member.setEmailAuthDt(LocalDateTime.now());
        memberRepository.save(member);

        return true;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Member> optionalMember = memberRepository.findById(username);
        if (!optionalMember.isPresent()) {
            throw new UsernameNotFoundException("?????? ????????? ???????????? ????????????.");
        }

        Member member = optionalMember.get();

        if (!member.isEmailAuthYn()) {
            throw new MemberNotEmailAuthException("????????? ????????? ????????? ???????????? ????????????.");
        }

        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        if (member.isAdminYn()) {
            grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        return new User(member.getUserName(), member.getPassword(), grantedAuthorities);
    }

    @Override
    public boolean sendResetPassword(ResetPasswordInput parameter) {
        Optional<Member> optionalMember = memberRepository.findByUserIdAndUserName(parameter.getUserId(), parameter.getUserName());
        if (!optionalMember.isPresent()) {
            throw new UsernameNotFoundException("?????? ????????? ???????????? ????????????.");
        }

        Member member = optionalMember.get();

        String uuid = UUID.randomUUID().toString();

        member.setResetPasswordKey(uuid);
        member.setResetPasswordLimitDt(LocalDateTime.now().plusDays(1));
        memberRepository.save(member);

        String email = parameter.getUserId();
        String subject = "[fastlms] ???????????? ????????? ???????????????.";
        String text = "<p>fastlms ???????????? ????????? ???????????????.</p><p>?????? ????????? ??????????????? ??????????????? ????????? ????????????.</p>"
                + "<div><a target='_blank' href='http://localhost:8080/member/reset/password?id=" + uuid + "'>???????????? ????????? ??????</a></div>";
        mailComponents.sendMail(email, subject, text);

        return true;
    }

    @Override
    public boolean resetPassword(String uuid, String password) {
        Optional<Member> optionalMember = memberRepository.findByResetPasswordKey(uuid);
        if (!optionalMember.isPresent()) {
            throw new UsernameNotFoundException("?????? ????????? ???????????? ????????????.");
        }

        Member member = optionalMember.get();

        if (member.getResetPasswordLimitDt() == null) {
            throw new RuntimeException("????????? ????????? ????????????.");
        }

        if (member.getResetPasswordLimitDt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("????????? ????????? ????????????.");
        }

        String encPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        member.setPassword(encPassword);
        member.setResetPasswordKey(null);
        member.setResetPasswordLimitDt(null);
        memberRepository.save(member);

        return true;
    }

    @Override
    public boolean checkResetPassword(String uuid) {
        Optional<Member> optionalMember = memberRepository.findByResetPasswordKey(uuid);
        if (!optionalMember.isPresent()) {
            return false;
        }

        Member member = optionalMember.get();

        // ????????? ????????? ???????????? ??????
        if (member.getResetPasswordLimitDt() == null) {
            throw new RuntimeException("????????? ????????? ????????????.");
        }

        if (member.getResetPasswordLimitDt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("????????? ????????? ????????????.");
        }

        return true;
    }

    @Override
    public List<Member> list() {
        return memberRepository.findAll();
    }
}
