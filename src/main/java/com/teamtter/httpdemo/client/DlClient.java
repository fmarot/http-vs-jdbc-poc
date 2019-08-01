package com.teamtter.httpdemo.client;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;

import org.springframework.util.StopWatch;
import org.springframework.util.unit.DataSize;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.teamtter.httpdemo.client.filetransfer.HttpDownloader;
import com.teamtter.httpdemo.client.filetransfer.HttpFileUploader;
import com.teamtter.httpdemo.client.filetransfer.JdbcDownloader;
import com.teamtter.httpdemo.client.filetransfer.JdbcUploader;
import com.teamtter.httpdemo.client.filetransfer.LargeFileCreatorHelper;
import com.teamtter.httpdemo.common.Endpoints;
import com.teamtter.httpdemo.common.ServerInfos;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;

@Slf4j
public class DlClient {

	private static final int NB_LOOP = 3;

	public static void main(String[] args) throws Exception {

		DataSize defaultLargeFileSize = DataSize.parse("500MB");
		DataSize dataSize = args.length == 0 ? defaultLargeFileSize : DataSize.parse(args[0]);
		long nbBytes = dataSize.toBytes();

		URL defaultUrl = new URL("http://127.0.0.1:8080");
		URL serverUrl = args.length > 1 ? new URL(args[1]) : defaultUrl;

		File largeFile = LargeFileCreatorHelper.createAutoDestructibleLargeFileInTmp(nbBytes);
		HashCode hc = com.google.common.io.Files.asByteSource(largeFile).hash(Hashing.goodFastHash(32));
		String fileId = largeFile.getName();
		OkHttpClient httpClient = buildHttpClient();
		URI uploadUri = serverUrl.toURI().resolve(Endpoints.upload);

		HttpFileUploader fileUploader = new HttpFileUploader(httpClient, uploadUri);
		ServerInfos serverInfos = fileUploader.queryServerInfos();
		String jdbcPort = serverInfos.getServerJdbcPort();
		String dbFilename = serverInfos.getDatabaseFileName();
		log.info("Server infos: {}", serverInfos);

		JdbcUploader jdbcUploader = new JdbcUploader(serverUrl.getHost(), jdbcPort, dbFilename);

		final HttpDownloader httpDownloader = new HttpDownloader(httpClient, serverUrl);

		executeCode(1, "http_upload", () -> fileUploader.uploadFile(largeFile));

		executeCode(1, "http_upload_multi", () -> fileUploader.uploadFileAsMultipart(largeFile));

		executeCode(1, "jdbc_upload", () -> jdbcUploader.uploadFile(largeFile));

		executeCode(NB_LOOP, "http_1", () -> checkFileHash(httpDownloader.downloadFile(fileId, Endpoints.DownloadMethods.file), hc));

		executeCode(NB_LOOP, "http_spring", () -> checkFileHash(httpDownloader.downloadFile(fileId, Endpoints.DownloadMethods.file_spring), hc));

		// executeDownload(3, "publicFile", () -> new PublicFileDownloader(serverUrl).downloadFile("file.tmp"));

		executeCode(NB_LOOP, "JDBC", () -> checkFileHash(new JdbcDownloader(serverUrl.getHost(), jdbcPort, dbFilename).downloadFile(fileId), hc));
	}

	private static void checkFileHash(File downloadedFile, HashCode expected) {
		try {
			HashCode hc = com.google.common.io.Files.asByteSource(downloadedFile).hash(Hashing.goodFastHash(32));
			if (!hc.equals(expected)) {
				throw new RuntimeException("Wrong file hash for: " + downloadedFile);
			}
		} catch (IOException e) {
			throw new RuntimeException("Unexpected error...", e);
		}
	}

	/** executes the downloadCode a certain number of times, logging the time result with a watch having the "donwloadType" id */
	private static void executeCode(int nbExecutions, String executionId, Runnable code) {
		StopWatch watch = new StopWatch(executionId);
		for (int i = 0; i < nbExecutions; i++) {
			watch.start("" + i);
			code.run();
			watch.stop();
		}
		log.info("\n\n{}\n", watch);
	}

	private static OkHttpClient buildHttpClient() {
		HttpLoggingInterceptor.Logger logger = new HttpLoggingInterceptor.Logger() {
			@Override
			public void log(String message) {
				log.debug(message);
			}
		};
		HttpLoggingInterceptor logging = new HttpLoggingInterceptor(logger);
		logging.setLevel(Level.HEADERS);

		OkHttpClient client = new OkHttpClient().newBuilder()
				.addInterceptor(logging)
				.build();

		return client;
	}

}
