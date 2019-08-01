package com.teamtter.httpdemo.server;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import org.h2.jdbcx.JdbcConnectionPool;
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
	protected String			serverJdbcPort;
	
	 @Value("${databaseFileName}")
	 protected String databaseFileName;

	private JdbcConnectionPool	connectionPool;

	public static void main(String[] args) throws Exception {
		System.setProperty("h2.lobInDatabase", "false");
		System.setProperty("h2.maxCompactTime", "3000");
		String tmp = "OLEA-DATABASEIMPL-" + UUID.randomUUID().toString().substring(0, 8);
		System.setProperty("h2.prefixTempFile", tmp);
		
		ConfigurableApplicationContext appContext = SpringApplication.run(ServerApp.class, args);
//		Class.forName("org.h2.Driver");
//		// exposeDBOnTCP
//		Server tcpServer = Server.createTcpServer(
//				new String[] { "-tcpAllowOthers", "-tcpDaemon", "-ifExists", "-tcpPassword", "true", "-tcpPort", "" + 9092 })
//				.start();
//		Thread.sleep(5000);
	}

	/** Start internal H2 server so we can query the DB from IDE 
	 * @throws ClassNotFoundException */
	@Bean(/*initMethod = "start", */destroyMethod = "stop")
	public Server h2Server() throws Exception {
		Class.forName("org.h2.Driver");
		// exposeDBOnTCP
		Server tcpServer = Server.createTcpServer(
				new String[] { "-tcpAllowOthers", "-tcpDaemon", "-ifExists", "-tcpPassword", "true", "-tcpPort", "" + serverJdbcPort })
				.start();
		// will listen on all addresses
		//		Server createTcpServer = Server.createTcpServer("-tcp", "-tcpAllowOthers", "-webAllowOthers", "-tcpPort", serverJdbcPort);
		return tcpServer;
	}

	@Bean
	public JdbcConnectionPool initConnectionPool() throws Exception {
		Class.forName("org.h2.Driver");
		String dbUrl = "jdbc:h2:"+databaseFileName+";LOCK_TIMEOUT=10000;TRACE_LEVEL_FILE=4";
		String emptyPassword = "";
		connectionPool = JdbcConnectionPool.create(dbUrl, "sa", emptyPassword);
		connectionPool.setMaxConnections(10);
		return connectionPool;
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