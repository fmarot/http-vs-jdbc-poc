package com.teamtter.httpdemo.server.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.teamtter.httpdemo.server.largefile.model.StreamingFileRecord;

@Repository
public interface StreamingFileRepository extends CrudRepository<StreamingFileRecord, Long> {

	Optional<StreamingFileRecord> findByFilename(String filename);
}
