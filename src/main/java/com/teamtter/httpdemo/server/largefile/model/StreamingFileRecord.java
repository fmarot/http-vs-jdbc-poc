package com.teamtter.httpdemo.server.largefile.model;

import java.io.InputStream;

import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StreamingFileRecord {

//	@Id
//	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "FILE_RECORD_ID_SEQ")
//	@SequenceGenerator(name = "FILE_RECORD_ID_SEQ", sequenceName = "FILE_RECORD_ID_SEQ")
//	private long	id;

//	@Id
	@NotNull
	private String	filename;

	@NotNull
	private long	dataLength;

//	@Lob
	@NotNull
	private InputStream	data;

	public StreamingFileRecord(String filename, long dataLength, InputStream data) {
		this.filename = filename;
		this.dataLength = dataLength;
		this.data = data;
	}

}
