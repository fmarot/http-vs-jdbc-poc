package com.teamtter.httpdemo.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
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
	private URL serverUrl;

	public FileDownloader(OkHttpClient httpClient, URL serverUrl) {
		this.httpClient = httpClient;
		this.serverUrl = serverUrl;
	}

	public void downloadFile(String toDownloadFilename) throws Exception {
		String url = serverUrl.toURI().resolve(Endpoints.download + Endpoints.DownloadMethods.file).toString()
				+ "?" + Endpoints.DownloadMethods.filename_var + "=" + toDownloadFilename;
		Request request = new Request.Builder()
				.url(url)
				.get().build();
		Call call = httpClient.newCall(request);
		Response response = call.execute();

		File downloadedFile = File.createTempFile(toDownloadFilename+"-", ".tmp");
		downloadedFile.deleteOnExit();
		try (InputStream downloadedFileStream = response.body().byteStream();
				FileOutputStream fos = new FileOutputStream(downloadedFile)) {
			long contentLength = Long.parseLong(response.header("Content-Length"));
			String filesize = LargeFileCreatorHelper.readableFileSize(contentLength);
			IOUtils.copy(downloadedFileStream, fos);
			log.info("File {} ({}) correctly copied to {}", toDownloadFilename, filesize, downloadedFile);
		}
	}

}
