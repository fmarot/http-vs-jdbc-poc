package com.teamtter.httpdemo.server.largefile.model;

import java.sql.Blob;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class StreamingFileRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "FILE_RECORD_ID_SEQ")
	@SequenceGenerator(name = "FILE_RECORD_ID_SEQ", sequenceName = "FILE_RECORD_ID_SEQ")
	private long	id;

	@NotNull
	private String	filename;

	@NotNull
	private long	dataLength;

	@Lob
	@NotNull
	private Blob	data;

	public StreamingFileRecord(String filename, long dataLength, Blob data) {
		this.filename = filename;
		this.dataLength = dataLength;
		this.data = data;
	}

}
