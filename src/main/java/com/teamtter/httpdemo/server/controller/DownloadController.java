package com.teamtter.httpdemo.server.controller;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.teamtter.httpdemo.common.Endpoints;
import com.teamtter.httpdemo.server.largefile.model.StreamingFileRecord;
import com.teamtter.httpdemo.server.largefile.service.LargeFileService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller					// not @RestController !
@RequestMapping(Endpoints.download)
public class DownloadController {

	private final LargeFileService lfService;

	public DownloadController(LargeFileService lfService) {
		this.lfService = lfService;
	}

	@GetMapping(path = Endpoints.DownloadMethods.file,
			produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public void downloadFile(
			@RequestParam(Endpoints.DownloadMethods.filename_var) String filename,
			HttpServletResponse response) throws SQLException, IOException {
		
		StreamingFileRecord record = lfService.loadRecordByFilename(filename);

		response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + record.getFilename());	// allows the browser to propose downloading a separate file instead of displaying it inline
		response.setHeader(HttpHeaders.CONTENT_LENGTH, "" + record.getDataLength());
		// response.setHeader(HttpHeaders.CONTENT_TYPE, "application/octet-stream");	this line does not work. Alternatively the solution is to rely on @GetMapping(produces)

		IOUtils.copyLarge(record.getData().getBinaryStream(), response.getOutputStream());
		log.info("controller downloadFile returned the stream...");
	}

}
