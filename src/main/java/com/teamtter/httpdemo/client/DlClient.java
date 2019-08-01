package com.teamtter.httpdemo.client;

import java.io.File;
import java.net.URI;
import java.net.URL;

import org.springframework.util.StopWatch;
import org.springframework.util.unit.DataSize;

import com.teamtter.httpdemo.client.filetransfer.HttpFileUploader;
import com.teamtter.httpdemo.client.filetransfer.HttpDownloader;
import com.teamtter.httpdemo.client.filetransfer.JdbcDownloader;
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
		String fileId = largeFile.getName();
		OkHttpClient httpClient = buildHttpClient();
		URI uploadUri = serverUrl.toURI().resolve(Endpoints.upload);

		HttpFileUploader fileUploader = new HttpFileUploader(httpClient, uploadUri);
		ServerInfos serverInfos = fileUploader.queryServerInfos();
		String jdbcPort = serverInfos.getServerJdbcPort();
		String dbFilename = serverInfos.getDatabaseFileName();
		log.info("Server infos: {}", serverInfos);

		fileUploader.uploadFile(largeFile);

		final HttpDownloader httpDownloader = new HttpDownloader(httpClient, serverUrl);
		executeCode(NB_LOOP, "http_1", () -> httpDownloader.downloadFile(fileId, Endpoints.DownloadMethods.file));

		executeCode(NB_LOOP, "http_1", () -> httpDownloader.downloadFile(fileId, Endpoints.DownloadMethods.file));

		executeCode(NB_LOOP, "http_spring", () -> httpDownloader.downloadFile(fileId, Endpoints.DownloadMethods.file_spring));

		//		executeDownload(3, "publicFile", () -> new PublicFileDownloader(serverUrl).downloadFile("file.tmp"));

		executeCode(NB_LOOP, "JDBC", () -> new JdbcDownloader(serverUrl.getHost(), jdbcPort, dbFilename).downloadFile(fileId));
	}

	/** executes the downloadCode a certain number of times, logging the time result with a watch having the "donwloadType" id */
	private static void executeCode(int nbExecutions, String executionId, Runnable code) {
		StopWatch watch = new StopWatch(executionId);
		for (int i = 0; i < nbExecutions; i++) {
			watch.start("" + i);
			code.run();
			watch.stop();
		}
		log.info("\n{}: {}\n", executionId, watch);
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
