package com.teamtter.httpdemo.server.controller;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.teamtter.httpdemo.common.Endpoints;
import com.teamtter.httpdemo.server.largefile.model.StreamingFileRecord;
import com.teamtter.httpdemo.server.largefile.service.LargeFileService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller			// not @RestController !
@RequestMapping(Endpoints.upload)
public class UploadController {

	private final LargeFileService lfService;

	public UploadController(LargeFileService lfService) {
		this.lfService = lfService;
	}
	
	@PostMapping(path = Endpoints.UploadMethods.file,
			consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE,
			produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<Long> upload(
			@RequestParam(Endpoints.UploadMethods.filename_var) String filename,
			@RequestParam(Endpoints.UploadMethods.filesize_var) long filesize,
			HttpServletRequest request) throws IOException, SQLException, URISyntaxException {
		StreamingFileRecord newRecord = lfService.saveFile(filename, filesize, request.getInputStream());
		return ResponseEntity.created(new URI("http://localhost:8080/blobs/" + newRecord.getId())).build();
	}
	/*
		@RequestMapping(value = "/blobs/{id}", method = RequestMethod.GET)
		public void load(@PathVariable("id") long id, HttpServletResponse response) throws SQLException, IOException {
			StreamingFileRecord record = lfService.loadRecordById(id);
			response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + record.getName());
			IOUtils.copy(record.getData().getBinaryStream(), response.getOutputStream());
			log.info("Sent file id: {}", record.getId());
		}
		*/
	// demonstrates using directly the stream of the request (seems faster than MultipartFile)
	//	@PostMapping(value = "storeCompressedDicomFiles")
	//	public void storeCompressedDicomFiles(HttpServletRequest request, @RequestParam("filename") String filename) throws Exception {
	//		log.info("Receiving file " + filename);
	//		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
	//        if (!isMultipart) {
	//        	log.error("error no multipart upload...");
	//        	throw new Exception("error no multipart upload...");
	//        }
	//
	//        File tmpFile = File.createTempFile("toto", "");
	//       
	//        try (OutputStream out = new FileOutputStream(tmpFile)) {
	//	        IOUtils.copy(request.getInputStream(), out);
	//	        log.info("content copied to {}", tmpFile);
	//        } catch (Exception e) {
	//        	log.error("error writing file", e);
	//        }
	//	}

	//	@PostMapping(value = storeCompressedDicomFiles, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	//	public void storeCompressedDicomFiles(@RequestPart("file") MultipartFile multipartFile) throws IOException {
	//		String originalFilename = multipartFile.getOriginalFilename();
	//		long filesize = multipartFile.getSize();
	//		log.info("Received file {} length {}", originalFilename, filesize);
	//	    File targetFile = File.createTempFile("originalFilename", "tmp");
	//	    FileUtils.copyInputStreamToFile(multipartFile.getInputStream(), targetFile);
	//	    log.info("content copied to {}", targetFile);
	//	}

	// client side:
	/*
	 * public void uploadStreamDicomFile(InputStream stream, String urlSuffix) throws IOException {
		
		RequestBody requestBodyStream = StreamHelper.create(mediaTypeOctet, stream);
		RequestBody requestBody = new MultipartBody.Builder().addPart(requestBodyStream).build();
	//				.setType(MultipartBody.FORM)
	//				.addFormDataPart("file", "myFileToto", requestBodyStream)
	//				.build();
	
		String parameters = "?filename=fileToto";
		Request request = new Request.Builder()
				.url(sdsBaseUrl + urlSuffix + parameters )
				.post(requestBody)
				.build();
		Call call = httpClient.newCall(request);
		log.info("Sending file...");
		Response response = call.execute();
		log.info("File sent.");
	}*/

}
