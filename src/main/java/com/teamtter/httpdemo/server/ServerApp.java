package com.teamtter.httpdemo.server;

import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class ServerApp {

	public static void main(String[] args) {
		ConfigurableApplicationContext appContext = SpringApplication.run(ServerApp.class, args);
	}

	@ControllerAdvice
	public class DogsServiceErrorAdvice {
		@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
		@ExceptionHandler({ Exception.class })
		public void handle(HttpServletRequest request, Exception thrown) {
			log.error("Exception thrown by controller", thrown);
		}
	}

}