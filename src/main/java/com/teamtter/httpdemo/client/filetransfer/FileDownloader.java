package com.teamtter.httpdemo.client.filetransfer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import org.h2.util.IOUtils;

import com.teamtter.httpdemo.common.Endpoints;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Slf4j
public class FileDownloader {

	private OkHttpClient httpClient;
	private final String baseUrl;

	public FileDownloader(OkHttpClient httpClient, URL serverUrl) {
		this.httpClient = httpClient;
		try {
			baseUrl = serverUrl.toURI().resolve(Endpoints.download).toString();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		} 
	}

	public void downloadFile(String toDownloadFilename, String endpoint) throws RuntimeException {
		try {
			String url = baseUrl + endpoint + "?" + Endpoints.UploadMethods.filename_var + "=" + toDownloadFilename;
			Request request = new Request.Builder()
					.url(url)
					.get().build();
			Call call = httpClient.newCall(request);
			Response response = call.execute();
	
			File downloadedFile = File.createTempFile(toDownloadFilename+"-", ".tmp");
			downloadedFile.deleteOnExit();
			try (InputStream downloadedFileStream = response.body().byteStream();
					FileOutputStream fos = new FileOutputStream(downloadedFile)) {
				try {
					long contentLength = Long.parseLong(response.header("Content-Length"));
					String filesize = LargeFileCreatorHelper.readableFileSize(contentLength);
				} catch (Exception e) {
					log.error("could not compute content length...");
				}
				IOUtils.copy(downloadedFileStream, fos);
				log.info("File {} correctly copied to {}", toDownloadFilename/*, filesize*/, downloadedFile);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
