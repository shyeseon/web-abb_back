package com.mycompany.webapp.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration //설정파일들에 공통적으로 들어가는 어노테이션 (관리객체로 만들어주기)
@EnableMethodSecurity(securedEnabled = true) //@Secured 어노테이션을 사용하여 메소드 단위 보안 설정을 할 수 있도록 활성화
//메소드에 @Secured("ROLE_MANAGER") 설정 줄 수 있음
public class WebSecurityConfig {
	@Autowired
	private JwtAuthenticationFilter jwtAuthenticationFilter;
	
	//인증 필터 체인을 관리 객체로 등록
	@Bean //관리 객체 설정을 위한 어노테이션 -> 메소드 호출시 리턴된 객체가 자동으로 관리 객체로 생성 됨
	public SecurityFilterChain filterChanin(HttpSecurity http) throws Exception {
		//Rest API에서 로그인 폼을 제공하지 않으므로 폼을 통한 로그인 인증을 하지 않도록 설정
		//로그인 폼은 front-end 에서 제공해야 함
		//폼을 사용하지 않기 때문에 config 매개변수가 들어왔을 때 실행하는 config 함수를 disable 사용하지 않도록 함
		http.formLogin(config -> config.disable());
		//만약 spring 처럼 서버에서 로그인폼을 받는 방식으로 security 객체를 사용한다면 이런식으로 설정 가능함
		//http.formLogin(config -> config
								//.loginPage()
								//.loginProcessinUrl()
								//.usernameParameter()
								//.user());
		
		//HttpSession을 사용하지 않도록 설정
		//rest API를 사용할 경우 서버에서 HttpSession을 사용하지 않기 때문에 설정도 사용 안하는 방식으로 해주기
		http.sessionManagement(config -> config.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		
		//사이트간 요청 위조 방지 비활성화(GET 방식 이외의 방식을 요청할 경우 _csrf 토큰 요구하기 때문)
		http.csrf(config -> config.disable());
		
		//CORS 설정(다른(크로스) 도메인에서 받은 인증 정보(AccessToken)로 요청할 경우 허가(디폴트는 불허)하는 설정)
		//아래쪽에서 메소드를 따로 빼서 디테일하게 정의해줌
		http.cors(config -> {});
		
		//access 토큰은 이미 발행된 상태, 사용자가 토큰을 가져왔을 때 유효한지 판단해주는 단계
		//JWT로 인증이되도록 필터를 등록
		http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		//아이디와 패스워드를 이용해 db에 연결 및 일치 여부를 인증을 하는 필터가 있는데(로그인을 한건지, 권한이 있는건지 등)
		//그 필터가 동작하기 전에 인증처리를 미리 하도록하는 설정
		//why? 뒷단으로는 아이디와 패스워드가 들어오는 것이 아니라 토큰만 들어오기 때문에 필터 발동 전에 처리를 해야함
		
		return http.build();
	}
	
	//권한 계층을 관리 객체로 등록
	@Bean
	public RoleHierarchy roleHierarchy() {
		RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
		String strHierarchy = "";
		strHierarchy += "ADMIN > MANAGER ";
		strHierarchy += "MANAGER > USER ";
		hierarchy.setHierarchy(strHierarchy);
		return hierarchy;
	}

	//@PreAuthorize 어노테이션의 표현식을 해석하는 객체 등록
	@Bean
    public MethodSecurityExpressionHandler createExpressionHandler() {
      DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setRoleHierarchy(roleHierarchy());
        return handler;
    }	
	
	//다른(크로스) 도메인 제한 설정
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		//요청 사이트 제한
		configuration.addAllowedOrigin("*"); // 모든 도메인 허용 //신세계 현대 sk 다 상관없음 //Origin -> 처음 인증 받은 곳
		//ex) configuration.addAllowedOrigin("sinsegae.com"); -> 링크 뒤쪽에 sinsegae.com이라는 신세계 쪽 도메인은 다 허용 현대는 안됨
		
		//요청 방식 제한
		configuration.addAllowedMethod("*"); // 모든 방식 가능
		//ex) configuration.addAllowedMethod("GET"); -> get 방식 가능
		//ex) configuration.addAllowedMethod("POST"); -> post 방식 가능
		//PUT, PATCH, DELETE 방식 등도 추가 가능
		
		//요청 헤더 제한
		configuration.addAllowedHeader("*"); //모든 헤더 가능
		//configuration.addAllowedHeader("헤더이름");
		//ex) configuration.addAllowedHeader("user_agent");
		
		//모든 URL에 대해 위 설정을 내용 적용
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}







