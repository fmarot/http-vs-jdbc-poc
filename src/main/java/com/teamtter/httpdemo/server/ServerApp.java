package com.teamtter.httpdemo.server;

import java.sql.SQLException;

import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

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
    
    	// Create SEPERATE LOBs!!!
		System.setProperty("h2.lobInDatabase", "false");//$NON-NLS-1$ //$NON-NLS-2$
		// 3 seconds max (in stead of 200 ms by default) in order to execute the SHUTDOWN COMPACT as done by h2 database 
		// when it is closed
		System.setProperty("h2.maxCompactTime", "3000");//$NON-NLS-1$ //$NON-NLS-2$
    			
		// will listen on all addresses
        return Server.createTcpServer("-tcp", "-tcpAllowOthers", "-webAllowOthers", "-tcpPort", serverJdbcPort);
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