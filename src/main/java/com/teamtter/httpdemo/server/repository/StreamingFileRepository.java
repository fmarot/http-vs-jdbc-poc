package com.teamtter.httpdemo.server.repository;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

import org.h2.jdbcx.JdbcConnectionPool;
import org.springframework.stereotype.Service;

import com.teamtter.httpdemo.server.largefile.model.StreamingFileRecord;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StreamingFileRepository {

	private JdbcConnectionPool connectionPool;

	StreamingFileRepository(JdbcConnectionPool connectionPool) throws SQLException {
		this.connectionPool = connectionPool;
		
		try (Connection conn = getConnection();
				Statement stmt = conn.createStatement()) {
			String createTable = "CREATE TABLE STREAMING_FILE_RECORD (FILENAME VARCHAR(1024),  DATALENGTH BIGINT, DATA BLOB)";
			stmt.execute(createTable);
		} catch (Exception e) {
			log.warn("Database already exists ??? {}", e.getMessage());
		}
		
	}
	
	public Optional<StreamingFileRecord> findByFilename(String filename) throws SQLException {
		try (Connection conn = getConnection();
			Statement stmt = conn.createStatement()) {
			try (PreparedStatement ps = conn.prepareStatement("SELECT TOP 1 DATALENGTH, DATA FROM STREAMING_FILE_RECORD WHERE FILENAME=?")) {
				ps.setString(1, filename);
				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					long length = rs.getLong(1);
					InputStream binaryStream = rs.getBinaryStream(2);
					return Optional.of(new StreamingFileRecord(filename, length, binaryStream));
				} else {
					return Optional.empty();
				}
			}	
		}
	}

	public void save(StreamingFileRecord streamingFileRecord) throws Exception {
		try (Connection conn = getConnection();
				Statement stmt = conn.createStatement()) {
			try (PreparedStatement ps = conn.prepareStatement("INSERT INTO STREAMING_FILE_RECORD(FILENAME, DATALENGTH, DATA) VALUES(?,?,?)")) {
				String filename = streamingFileRecord.getFilename();
				ps.setString(1, filename);
				long dataLength = streamingFileRecord.getDataLength();
				ps.setLong(2, dataLength);
				InputStream data = streamingFileRecord.getData();
				ps.setBinaryStream(3, data);
				int executeUpdate = ps.executeUpdate();
			} catch(Exception e) {
				log.error("", e);
			}
		}
	}
	
	synchronized Connection getConnection() throws SQLException {
		int nbActiveConnections = connectionPool.getActiveConnections();
		int nbMaxConnections = connectionPool.getMaxConnections();
		log.trace("Nb of active connections {} / {}", nbActiveConnections, nbMaxConnections);
		if (nbActiveConnections >= nbMaxConnections) {
			log.error("Max number of connections reached, app will perform slowly :(");
		} else if (nbActiveConnections == nbMaxConnections - 1) {
			log.warn("Max number of connections nearly reached, app may perform slowly in a near future");
		}
		Connection connection = null;
		try {
			connection = connectionPool.getConnection();
			connection.setAutoCommit(true);
		} catch (Throwable t) {
			log.error("", t);
		}
		return connection;
	}


}
