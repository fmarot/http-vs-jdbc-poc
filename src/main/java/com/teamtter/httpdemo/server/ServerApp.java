package com.teamtter.httpdemo.server;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;

import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class ServerApp {

	@Value("${serverJdbcPort}")
	protected String serverJdbcPort;

	public static void main(String[] args) {
		ConfigurableApplicationContext appContext = SpringApplication.run(ServerApp.class, args);
	}
	
	/** Start internal H2 server so we can query the DB from IDE */
    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server h2Server() throws SQLException {
    	// will listen on all addresses
        return Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", serverJdbcPort);
    }
/*
	@ControllerAdvice
	public class MyServiceErrorAdvice {
		@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
		@ExceptionHandler({ Exception.class })
		public void handle(HttpServletRequest request, Exception thrown) {
			log.error("Exception thrown by controller", thrown);
		}
	}
*/
}