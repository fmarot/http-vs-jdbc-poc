package com.teamtter.httpdemo.client.filetransfer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.h2.util.IOUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JdbcDownloader {
	private static final String DB_DRIVER = "org.h2.Driver";
	
	static {
		JdbcDownloader.initJdbc();	
	}
	
	static void initJdbc() {
		System.setProperty("h2.lobInDatabase", "false");
		System.setProperty("h2.maxCompactTime", "3000");
		String tmp = "OLEA-DATABASEIMPL-" + UUID.randomUUID().toString().substring(0, 8);
		System.setProperty("h2.prefixTempFile", tmp);
		try {
			Class.forName(DB_DRIVER);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	private String connectionString;

	public static void main(String[] args) throws Exception {
		new JdbcDownloader("127.0.0.1", "9092", "~/test").downloadFile("xxx");
	}

	public JdbcDownloader(String serverHost, String jdbcPort, String databaseName) {
		connectionString = "jdbc:h2:tcp://" + serverHost + ":" + jdbcPort + "/" + databaseName + ";LOCK_TIMEOUT=10000";
	}

	// jdbc:h2:tcp://127.0.0.1:9092/C:\Program Files\Olea SphereSP19\Data\DB;LOCK_TIMEOUT=10000
	
	public File downloadFile(String filename) throws RuntimeException {
		try {
			try (Connection conn = getDBConnection()) {
				String sql = "SELECT TOP 1 data FROM STREAMING_FILE_RECORD WHERE filename = ? ";
				PreparedStatement stmt = conn.prepareStatement(sql);
				stmt.setString(1, filename);
				ResultSet resultSet = stmt.executeQuery();
				if (resultSet.next()) {
					File downloadedFile = File.createTempFile(filename+"-", ".tmp");
					downloadedFile.deleteOnExit();
					try (BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(downloadedFile));
							InputStream is = resultSet.getBinaryStream(1)) {
						IOUtils.copy(is, fos);
						fos.flush();
						log.debug("File {} correctly copied to {}", filename, downloadedFile);
					}
					return downloadedFile;
				} else {
					throw new RuntimeException("File " + filename + " not found to download");
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Connection getDBConnection() throws SQLException {
		Connection dbConnection = DriverManager.getConnection(connectionString, "sa", "");
		dbConnection.setAutoCommit(true);
		return dbConnection;
	}

}
