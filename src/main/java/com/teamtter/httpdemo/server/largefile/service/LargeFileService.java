package com.teamtter.httpdemo.server.largefile.service;

import java.io.InputStream;
import java.sql.Blob;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.engine.jdbc.LobCreator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.teamtter.httpdemo.server.largefile.model.StreamingFileRecord;
import com.teamtter.httpdemo.server.repository.StreamingFileRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LargeFileService {

	@PersistenceContext		// TODO inject in ctor
	private EntityManager entityManager;

	private StreamingFileRepository streamingFileRepository;

	public LargeFileService(StreamingFileRepository streamingFileRepository) {
		this.streamingFileRepository = streamingFileRepository;
	}

	@Transactional
	public StreamingFileRecord saveFile(String originalFilename, long filesize, InputStream inputStream) {
		Session session = entityManager.unwrap(Session.class);
		LobCreator lobCreator = Hibernate.getLobCreator(session);
		Blob blob = lobCreator.createBlob(inputStream, filesize);
		StreamingFileRecord streamingFileRecord = new StreamingFileRecord(originalFilename, filesize, blob);
		streamingFileRecord = streamingFileRepository.save(streamingFileRecord);
		log.info("Persisted {} with id: {}", originalFilename, streamingFileRecord.getId());
		return streamingFileRecord;
	}

	@Transactional
	public StreamingFileRecord loadRecordById(long id) {
		log.info("Loading file id: {}", id);
		return streamingFileRepository.findById(id).get();
	}

	@Transactional
	public StreamingFileRecord loadRecordByFilename(String filename) {
		log.info("Loading filename: {}", filename);
		return streamingFileRepository.findByFilename(filename).get();
	}

}
