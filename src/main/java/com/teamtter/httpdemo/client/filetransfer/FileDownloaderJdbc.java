package com.teamtter.httpdemo.client.filetransfer;

import java.io.File;
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
public class FileDownloaderJdbc {
	private static final String DB_DRIVER = "org.h2.Driver";
	static {
		try {
			Class.forName(DB_DRIVER);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	private String connectionString;

	public static void main(String[] args) throws Exception {
		new FileDownloaderJdbc("127.0.0.1", "9092").downloadFile("xxx");
	}

	public FileDownloaderJdbc(String serverHost, String jdbcPort) {
		connectionString = "jdbc:h2:tcp://" + serverHost + ":" + jdbcPort + "/~/test";
	}

	public void downloadFile(String filename) throws Exception {
		try (Connection conn = getDBConnection()) {
			String sql = "SELECT data FROM Streaming_File_Record WHERE filename = ? ";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, filename);
			ResultSet resultSet = stmt.executeQuery();
			if (resultSet.next()) {
				File downloadedFile = File.createTempFile(filename+"-", ".tmp");
				downloadedFile.deleteOnExit();
				try (FileOutputStream fos = new FileOutputStream(downloadedFile);
						InputStream is = resultSet.getBinaryStream(1)) {
					IOUtils.copy(is, fos);
					log.info("File {} correctly copied to {}", filename, downloadedFile);
				}
			} else {
				log.warn("File {} not found to download", filename);
			}
		}
	}

	private Connection getDBConnection() throws SQLException {
		Connection dbConnection = DriverManager.getConnection(connectionString, "sa", "");
		dbConnection.setAutoCommit(true);
		return dbConnection;
	}
	/*
	 * public void uploadFile() { try (Connection connection = getDBConnection()) {
	 * 
	 * File myLargeFile = null; String filename = null; myLargeFile = new
	 * File("./src/main/resources/MAX_INT_file");
	 * createLargeFileIfNotExists(myLargeFile); filename = myLargeFile.getName();
	 * 
	 * try (PreparedStatement pstmt = connection.prepareStatement(
	 * "INSERT INTO FILES(OID, DATA, FILENAME) " + " VALUES (" + (index++) +
	 * ", ?, '" + filename + "')"); InputStream is = new BufferedInputStream(new
	 * FileInputStream(myLargeFile))) { log.info("Will insert file {}", filename);
	 * pstmt.setBinaryStream(1, is);
	 * log.info(" setBinaryStream() done, will executeUpdate()", filename);
	 * pstmt.executeUpdate(); // H2 is stuck here for large files (or exit with
	 * connection reset, depending on // the content of the file)
	 * log.info(" executeUpdate OK for {} ", filename); is.close(); } } }
	 */

}
