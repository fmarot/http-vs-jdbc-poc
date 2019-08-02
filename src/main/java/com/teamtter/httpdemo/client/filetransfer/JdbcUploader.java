package com.teamtter.httpdemo.client.filetransfer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.h2.util.IOUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JdbcUploader {
	
	static {
		JdbcDownloader.initJdbc();	
	}

	private String connectionString;


	public JdbcUploader(String serverHost, String jdbcPort, String databaseName) {
		connectionString = "jdbc:h2:tcp://" + serverHost + ":" + jdbcPort + "/" + databaseName + ";LOCK_TIMEOUT=10000";
	}

	// jdbc:h2:tcp://127.0.0.1:9092/C:\Program Files\Olea SphereSP19\Data\DB;LOCK_TIMEOUT=10000

	public void downloadFile(String filename) throws RuntimeException {
		try {
			try (Connection conn = getDBConnection()) {
				String sql = "SELECT data FROM STREAMING_FILE_RECORD WHERE filename = ? ";
				PreparedStatement stmt = conn.prepareStatement(sql);
				stmt.setString(1, filename);
				ResultSet resultSet = stmt.executeQuery();
				if (resultSet.next()) {
					File downloadedFile = File.createTempFile(filename + "-", ".tmp");
					downloadedFile.deleteOnExit();
					try (BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(downloadedFile));
							InputStream is = resultSet.getBinaryStream(1)) {
						IOUtils.copy(is, fos);
						fos.flush();
						log.info("File {} correctly copied to {}", filename, downloadedFile);
					}
				} else {
					log.warn("File {} not found to download", filename);
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

	public void uploadFile(File toUploadFile) throws RuntimeException {
		try (Connection connection = getDBConnection()) {

			String insertQuery = "INSERT INTO STREAMING_FILE_RECORD(FILENAME, DATALENGTH, DATA)  VALUES (?, ?, ?) ";
			log.debug("Will INSERT/upload into table with jdbc file {}", toUploadFile);
			try (PreparedStatement pstmt = connection.prepareStatement(insertQuery);
					InputStream binaryStreamToUpload = new FileInputStream(toUploadFile)) {
				pstmt.setString(1, toUploadFile.getName());
				pstmt.setLong(2, toUploadFile.length());
				pstmt.setBinaryStream(3, binaryStreamToUpload);
				int nbRowsUpdated = pstmt.executeUpdate();
				if (nbRowsUpdated != 1) {
					throw new RuntimeException("Unable to upload file through JDBC !!! - " + nbRowsUpdated);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
