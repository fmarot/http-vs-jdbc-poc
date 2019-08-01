package com.teamtter.httpdemo.server.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.teamtter.httpdemo.common.Endpoints;
import com.teamtter.httpdemo.common.ServerInfos;
import com.teamtter.httpdemo.server.largefile.model.StreamingFileRecord;
import com.teamtter.httpdemo.server.largefile.service.LargeFileService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController			// not @RestController !
@RequestMapping(Endpoints.upload)
public class UploadController {

	private final LargeFileService lfService;
	
	@Value("${serverJdbcPort}")
	protected String			serverJdbcPort;
	
	 @Value("${databaseFileName}")
	 protected String databaseFileName;

	public UploadController(LargeFileService lfService) {
		this.lfService = lfService;
	}
	
	@GetMapping(path = Endpoints.UploadMethods.serverInfos)
	public ServerInfos serverInfos(HttpServletRequest request) {
		return new ServerInfos(databaseFileName, serverJdbcPort);
	}

	@PostMapping(path = Endpoints.UploadMethods.file,
			consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE,
			produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public String upload(
			@RequestParam(Endpoints.UploadMethods.filename_var) String filename,
			@RequestParam(Endpoints.UploadMethods.filesize_var) long filesize,
			HttpServletRequest request) throws Exception {
		StreamingFileRecord newRecord = lfService.saveFile(filename, filesize, request.getInputStream());
		return databaseFileName;
	}
	
}
