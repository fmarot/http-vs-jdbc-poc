package com.teamtter.httpdemo.server.largefile.service;

import java.io.InputStream;
import java.sql.SQLException;

import org.springframework.stereotype.Service;

import com.teamtter.httpdemo.server.largefile.model.StreamingFileRecord;
import com.teamtter.httpdemo.server.repository.StreamingFileRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LargeFileService {

//	@PersistenceContext		// TODO inject in ctor
//	private EntityManager entityManager;

	private StreamingFileRepository streamingFileRepository;

	public LargeFileService(StreamingFileRepository streamingFileRepository) {
		this.streamingFileRepository = streamingFileRepository;
	}

//	@Transactional
	public StreamingFileRecord saveFile(String originalFilename, long filesize, InputStream inputStream) throws Exception {
		StreamingFileRecord streamingFileRecord = new StreamingFileRecord(originalFilename, filesize, inputStream);
		streamingFileRepository.save(streamingFileRecord);
		log.info("Persisted {}", originalFilename);
		return streamingFileRecord;
	}

//	@Transactional
//	public StreamingFileRecord loadRecordById(long id) {
//		log.info("Loading file id: {}", id);
//		return streamingFileRepository.findById(id).get();
//	}

//	@Transactional
	public StreamingFileRecord loadRecordByFilename(String filename) throws SQLException {
		log.info("Loading filename: {}", filename);
		return streamingFileRepository.findByFilename(filename).get();
	}

}
