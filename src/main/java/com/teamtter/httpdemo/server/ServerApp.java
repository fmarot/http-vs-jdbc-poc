package com.teamtter.httpdemo.server;

import java.sql.SQLException;

import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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
		return Server.createTcpServer("-tcp", "-tcpAllowOthers", "-webAllowOthers", "-tcpPort", serverJdbcPort);
	}

	/** possible to download files directly located besides the jar in the 'files' folder */
	@Configuration
	@EnableWebMvc
	public class MvcConfig implements WebMvcConfigurer {
		@Override
		public void addResourceHandlers(ResourceHandlerRegistry registry) {
			registry.addResourceHandler("/files/**")
					.addResourceLocations("file:files/");
		}
	}
}