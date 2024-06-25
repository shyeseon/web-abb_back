package com.mycompany.webapp.security;

import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import com.mycompany.webapp.dto.Member;

// security 객체 변수 선언
public class AppUserDetails extends User  {
	private Member member;
	
	// security 객체(사용자)의 정보를 하나하나 쓸 수 있도록 씨큐리티 객체에 저장
	public AppUserDetails(Member member, List<GrantedAuthority> authorities) {	
		super(member.getMid(), 
			  member.getMpassword(), 
			  member.isMenabled(), 
			  true, true, true, 
			  authorities);
		this.member = member;
	}

	public Member getMember() {
		return member;
	}
}
