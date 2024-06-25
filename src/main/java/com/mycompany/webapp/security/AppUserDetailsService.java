package com.mycompany.webapp.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mycompany.webapp.dao.MemberDao;
import com.mycompany.webapp.dto.Member;

@Service
public class AppUserDetailsService implements UserDetailsService {
	@Autowired
	private MemberDao memberDao;	
	
	@Override
	//유저이름을 이용해서 db에서 유저의 정보를 가져옴
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Member member = memberDao.selectByMid(username); 
		
		//유저 이름으로 db에서 유저 정보를 획득하지 못했을 경우 예외 발생
		if(member == null) {
			throw new UsernameNotFoundException(username);
		}
		
		//유저 정보 있을 경우 권한 정보 얻어서 저장
		List<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority(member.getMrole()));
		
		AppUserDetails userDetails = new AppUserDetails(member, authorities);
		return userDetails;
	}
}


