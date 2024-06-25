package com.mycompany.webapp.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycompany.webapp.dto.Member;
import com.mycompany.webapp.security.AppUserDetails;
import com.mycompany.webapp.security.AppUserDetailsService;
import com.mycompany.webapp.security.JwtProvider;
import com.mycompany.webapp.service.MemberService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/member")
public class MemberController {
	@Autowired
	private JwtProvider jwtProvider;
	
	@Autowired
	private AppUserDetailsService userDetailsService;
	
	@Autowired
	private MemberService memberService;
	
	@PostMapping("/login")
	public Map<String, String> userLogin(String mid, String mpassword) {
		//사용자 상세 정보 얻기
		AppUserDetails userDetails = (AppUserDetails)userDetailsService.loadUserByUsername(mid);
		//비밀번호 체크
		PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
		boolean checkResult = passwordEncoder.matches(mpassword, userDetails.getMember().getMpassword());
		//스프링 시큐리티 인증 처리
		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);
		//응답 생성
		//비밀번호 체크했을 때 로그인 성공했을 경우
		Map<String, String> map = new HashMap<>();
		if(checkResult) {
			//AccessToken을 생성
			String accessToken = jwtProvider.createAccessToken(mid, userDetails.getMember().getMrole());
			//JSON 응답 구성
			map.put("result", "success");
			map.put("mid", mid);
			map.put("accessToken", accessToken);
		//비밀번호 체크했을 때 실패할 경우
		} else {
			map.put("result", "fail");
		}
		return map;
	}
	
	@PostMapping("/join")
	public Member join(@RequestBody Member member) {
		//비밀번호 암호화
		PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
		member.setMpassword(passwordEncoder.encode(member.getMpassword()));
		//아이디 활성화 설정
		member.setMenabled(true);
		//권한 설정
		member.setMrole("ROLE_USER");
		//회원 가입 처리
		memberService.join(member);
		//비밀번호 제거(비밀번호는 제이슨 객체에 담아서 보내는 것은 보안 문제때문에 안됨)
		member.setMpassword(null);
		return member;
	}
}

