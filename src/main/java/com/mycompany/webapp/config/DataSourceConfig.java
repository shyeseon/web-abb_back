package com.mycompany.webapp.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class DataSourceConfig {
   @Bean //클래스에 @Configuration @Bean 어노테이션이 있으면 자동으로 관리 객체를 실행 
   public DataSource dataSource() { //메소드 이름인 dataSource가 관리객체 이름
      HikariConfig config = new HikariConfig();      
      config.setDriverClassName("oracle.jdbc.OracleDriver");
      config.setJdbcUrl("jdbc:oracle:thin:@kosa164.iptime.org:1521:orcl");      
      //config.setDriverClassName("net.sf.log4jdbc.DriverSpy");
      //config.setJdbcUrl("jdbc:log4jdbc:oracle:thin:@localhost:1521:orcl");      
      config.setUsername("user_spring");
      config.setPassword("oracle");
      config.setMaximumPoolSize(3);
      HikariDataSource hikariDataSource = new HikariDataSource(config);
      return hikariDataSource; 
   }
   
   //src/main/resources/application.properties 에서도 설정 가능 -> @configuration과 @bean 어노테이션에 주석 달아줘야 그쪽에서 사용 가능
}