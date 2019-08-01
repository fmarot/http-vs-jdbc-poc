package com.teamtter.httpdemo.common;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ServerInfos {

	private String databaseFileName;
	private String serverJdbcPort;

	public ServerInfos(String databaseFileName, String serverJdbcPort) {
		this.databaseFileName = databaseFileName;
		this.serverJdbcPort = serverJdbcPort;
	}

}
