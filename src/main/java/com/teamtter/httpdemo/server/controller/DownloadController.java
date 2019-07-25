package com.teamtter.httpdemo.server.controller;

import java.sql.SQLException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.teamtter.httpdemo.common.Endpoints;
import com.teamtter.httpdemo.server.largefile.model.StreamingFileRecord;
import com.teamtter.httpdemo.server.largefile.service.LargeFileService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller	(Endpoints.download)	// not @RestController !
public class DownloadController {

	private final LargeFileService lfService;

	public DownloadController(LargeFileService lfService) {
		this.lfService = lfService;
	}

	@GetMapping(value = Endpoints.DownloadMethods.file,
			//consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE,
			produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public InputStreamResource downloadFile(
			@RequestParam(Endpoints.DownloadMethods.id) long id,
			HttpServletResponse response) throws SQLException {
		
		
		StreamingFileRecord record = lfService.loadRecordById(id);
		
		response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + record.getFilename());	// allows the browser to propose downloading a separate file instead of displaying it inline
		response.setHeader(HttpHeaders.CONTENT_LENGTH, "" + record.getDataLength());
		// response.setHeader(HttpHeaders.CONTENT_TYPE, "application/octet-stream");	this line does not work. Alternatively the solution is to rely on @GetMapping(produces)

		InputStreamResource resource = new InputStreamResource(record.getData().getBinaryStream());

		
		return resource;
	}

}
