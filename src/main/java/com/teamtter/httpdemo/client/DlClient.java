package com.teamtter.httpdemo.client;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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
		HashCode expectedHashcode = com.google.common.io.Files.asByteSource(largeFile).hash(Hashing.goodFastHash(32));
		String fileId = largeFile.getName();
		OkHttpClient httpClient = buildHttpClient();
		URI uploadUri = serverUrl.toURI().resolve(Endpoints.upload);

		HttpFileUploader fileUploader = new HttpFileUploader(httpClient, uploadUri);
		ServerInfos serverInfos = fileUploader.queryServerInfos();
		String jdbcPort = serverInfos.getServerJdbcPort();
		String dbFilename = serverInfos.getDatabaseFileName();
		
		InetAddress localHost = InetAddress.getLocalHost();
		log.info("Client: {} - {}", localHost, localHost.getHostName());
		log.info("      =>   server infos: {} - url: {}", serverInfos, serverUrl);
		log.info("");
		
		JdbcUploader jdbcUploader = new JdbcUploader(serverUrl.getHost(), jdbcPort, dbFilename);

		final HttpDownloader httpDownloader = new HttpDownloader(httpClient, serverUrl);

		executeFileUpload(1, "http_upload", largeFile, f -> fileUploader.uploadFile(f));

		executeFileUpload(1, "http_upload_multi", largeFile, f -> fileUploader.uploadFileAsMultipart(f));

		executeFileUpload(1, "jdbc_upload", largeFile, f -> jdbcUploader.uploadFile(f));
		
		log.info("");	// just add empty line in logs...

		executeFileDownload(NB_LOOP, "http_1", expectedHashcode, () -> httpDownloader.downloadFile(fileId, Endpoints.DownloadMethods.file));

		executeFileDownload(NB_LOOP, "http_spring", expectedHashcode, () -> httpDownloader.downloadFile(fileId, Endpoints.DownloadMethods.file_spring));

		// executeDownload(3, "publicFile", () -> new PublicFileDownloader(serverUrl).downloadFile("file.tmp"));

		executeFileDownload(NB_LOOP, "JDBC", expectedHashcode, () -> new JdbcDownloader(serverUrl.getHost(), jdbcPort, dbFilename).downloadFile(fileId));
	}


	private static void executeFileUpload(int nbExecutions, String executionId, File f, Consumer<File> code) {
		StopWatch watch = new StopWatch(executionId);
		for (int i = 0; i < nbExecutions; i++) {
			watch.start("" + i);
			code.accept(f);
			watch.stop();
		}
		// compute and display download speed
		long uploadedSize = f.length() * nbExecutions;
		long durationMillisec = watch.getTotalTimeMillis();
		String uploadSpeed = computeDownloadSpeed(uploadedSize, durationMillisec);
		
		log.info("{}  - \n\t\t\t\t\t\t\t speed: {}", watch, uploadSpeed);
	}

	/** executes the downloadCode a certain number of times, logging the time result with a watch having the "donwloadType" id */
	private static void executeFileDownload(int nbExecutions, String executionId, HashCode expectedHashcode, Supplier<File> code) {
		List<File> allDownloadedFiles = new ArrayList<>();
		StopWatch watch = new StopWatch(executionId);
		for (int i = 0; i < nbExecutions; i++) {
			watch.start("" + i);
			File file = code.get();
			allDownloadedFiles.add(file);
			watch.stop();
		}
		
		// compute and display download speed
		long downloadedSize = allDownloadedFiles.stream().mapToLong(f -> f.length()).sum();
		long durationMillisec = watch.getTotalTimeMillis();
		String downloadSpeed = computeDownloadSpeed(downloadedSize, durationMillisec);
		
		log.info("{}  - \n\t\t\t\t\t\t\t speed: {}", watch, downloadSpeed);
		
		// check the downloaded files are the expected ones with the hashcode
		allDownloadedFiles.forEach(f -> {
			if (! checkFileHash(f, expectedHashcode)) {
				log.error("WRONG FILE HASH FOR {}", f);
				throw new RuntimeException("WRONG FILE HASH" + f);
			}
		});
	}

	private static boolean checkFileHash(File downloadedFile, HashCode expected) {
		try {
			HashCode hc = com.google.common.io.Files.asByteSource(downloadedFile).hash(Hashing.goodFastHash(32));
			return hc.equals(expected);
		} catch (IOException e) {
			throw new RuntimeException("Unexpected error...", e);
		}
	}

	private static String computeDownloadSpeed(long downloadedSizeInBytes, long durationMillisec) {
		long speed = (((downloadedSizeInBytes / durationMillisec) * 1000)/ 1024) / 1024; 
		return speed + " MBps";
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
