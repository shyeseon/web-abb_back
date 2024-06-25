package com.mycompany.webapp.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtProvider {
	//서명 및 암호화를 위한 SecretKey
//	@Value("${jwt.security.key}")
//	private String jwtSecurityKey;
	private SecretKey secretKey;
	//AccessToken의 유효 기간(단위: 밀리세컨)
	private long accessTokenDuration = 24*60*60*1000; // 하루동안
	
	//생성자
	// resources/application.properties 파일의 안에 정의한 시크릿키
	// jwt.security.key = com.mycompany.jsonwebtoken.kosacourse
	public JwtProvider(@Value("${jwt.security.key}") String jwtSecurityKey) {
		try {
			//application.property에서 문자열 키를 읽고, SecretKey를 생성
			secretKey = Keys.hmacShaKeyFor(jwtSecurityKey.getBytes("UTF-8"));
		} catch (Exception e) {
			log.info(e.toString());
		} 
	}
	
	// AccessToken 생성 (로그인 성공하면)
	// 이메일도 넣으려면 (String email) 추가 -> builder.claim("email")
	public String createAccessToken(String userId, String authority) {
		String token = null;
		try {
			JwtBuilder builder = Jwts.builder();
			//header 설정
			//자동으로 설정
			
			//payload 설정
			builder.subject(userId); // 페이로드 주요정보 -> id
			builder.claim("authority", authority); // 페이로드 내용 -> id 이외 정보
			builder.expiration(new Date(new Date().getTime() + accessTokenDuration));
			// 지금 시간으로부터 하루동안 사용 가능
			// {"sub":"user",
			// "authority":"ROLE_USER"}
			
			//signature 설정
			builder.signWith(secretKey);
			token = builder.compact();
		} catch(Exception e) {
			log.info(e.toString());
			// 예외가 생길 경우 에러메세지
		}
		return token;
		// 예외 없이 try 단에서 토큰 생성 성공했을 경우 생성한 token 리턴
	}
	
	// 토큰을 가져올 때 유효성 검사 (사용자가 로그인을 다시 할 때)
	public Jws<Claims> validateToken(String token) {
		Jws<Claims> jws = null;
		// jws의 디폴트 값을 null로 줬기 때문에 유효성검사 통과하면 페이로드 정보를 가진 jwt를 리턴 아니면 null값 그대로
	    try {
	    	//JWT 파서 빌더 생성
	        JwtParserBuilder builder = Jwts.parser();
	        //JWT 파서 빌더에 비밀키 설정
	        builder.verifyWith(secretKey);
	        //JWT 파서 생성
	        JwtParser parser = builder.build();
	        //AccessToken으로부터 payload 얻기
	        jws = parser.parseSignedClaims(token);
	    } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
	        log.info("잘못된 JWT 서명입니다.");
	    } catch (ExpiredJwtException e) {
	    	log.info("만료된 JWT 토큰입니다."); // accessTokenDuration 시간이 지나서 토큰 만료
	    } catch (UnsupportedJwtException e) {
	    	log.info("지원되지 않는 JWT 토큰입니다.");
	    } catch (IllegalArgumentException e) {
	    	log.info("JWT 토큰이 잘못되었습니다."); // jwt 구조가 이상함
	    }
	    return jws;
		// 유효성 검사 통과하면 페이로드 정보를 가진 jwt를 리턴
		// payload
		/*
		 * { "sub": "user", "authority": "ROLE_USER", "exp": 1717220009 }
		 */
	}
	
	// 리턴된 jws의 페이로드 정보 중 id 얻기
	public String getUserId(Jws<Claims> jws) {
		//Payload 얻기
	    Claims claims = jws.getPayload();
	    //사용자 아이디 얻기
	    String userId = claims.getSubject();
	    return userId;
	}
    
	// 리턴된 jws의 페이로드 정보 중 권한 얻기
	public String getAuthority(Jws<Claims> jws) {
		//Payload 얻기
	    Claims claims = jws.getPayload();
		//사용자 권한 얻기
		String autority = claims.get("authority").toString();
	    return autority;
	}	
	
	// 리턴된 jws의 페이로드 정보 중 email 얻기
	//public String getAuthority(Jws<Claims> jws) {
		// Payload 얻기
		//Claims claims = jws.getPayload();
		// 사용자 권한 얻기
		//String email = claims.get("email").toString();
		//return email;
	//}
	
	
	// jwt 테스트용
	public static final void main(String[] args) {
		JwtProvider jwtProvider = new JwtProvider("com.mycompany.jsonwebtoken.kosacourse");
		
		String accessToken = jwtProvider.createAccessToken("user", "ROLE_USER");
		log.info("AccessToken: " + accessToken);
		
		Jws<Claims> jws = jwtProvider.validateToken(accessToken);
		log.info("validate: " + ((jws!=null)? true : false));
		
		if(jws != null) {
			String userId = jwtProvider.getUserId(jws);
			log.info("userId: " + userId);
			
			String autority = jwtProvider.getAuthority(jws);
			log.info("autority: " + autority);
		}
	}
}






