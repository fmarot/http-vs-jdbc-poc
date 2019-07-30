package com.teamtter.httpdemo.client;

import java.io.File;
import java.net.URI;
import java.net.URL;

import org.springframework.util.StopWatch;
import org.springframework.util.unit.DataSize;

import com.teamtter.httpdemo.client.filetransfer.FileDownloader;
import com.teamtter.httpdemo.client.filetransfer.FileDownloaderJdbc;
import com.teamtter.httpdemo.client.filetransfer.FileUploader;
import com.teamtter.httpdemo.client.filetransfer.LargeFileCreatorHelper;
import com.teamtter.httpdemo.client.filetransfer.PublicFileDownloader;
import com.teamtter.httpdemo.common.Endpoints;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;

@Slf4j
public class DlClient {

	public static void main(String[] args) throws Exception {
		
		DataSize defaultLargeFileSize = DataSize.parse("500MB");
		DataSize dataSize = args.length == 0 ? defaultLargeFileSize : DataSize.parse(args[0]);   
		long nbBytes = dataSize.toBytes();

		URL defaultUrl = new URL("http://127.0.0.1:8080");
		URL serverUrl = args.length > 1 ? new URL(args[1]) : defaultUrl;

		File largeFile = LargeFileCreatorHelper.createAutoDestructibleLargeFileInTmp(nbBytes);
		String fileId = largeFile.getName();
		OkHttpClient httpClient = buildHttpClient();
		URI uploadUri = serverUrl.toURI().resolve(Endpoints.upload + Endpoints.UploadMethods.file);
		String databaseFileName = new FileUploader(httpClient, uploadUri).uploadFile(largeFile);


		executeDownload(2, "http_1", () -> new FileDownloader(httpClient, serverUrl).downloadFile(fileId, Endpoints.DownloadMethods.file));
		
		executeDownload(2, "http_spring", () -> new FileDownloader(httpClient, serverUrl).downloadFile(fileId, Endpoints.DownloadMethods.file_spring));
		
		executeDownload(2, "http_spring", () -> new FileDownloader(httpClient, serverUrl).downloadFile(fileId, Endpoints.DownloadMethods.file_spring));
		
		executeDownload(2, "http_1", () -> new FileDownloader(httpClient, serverUrl).downloadFile(fileId, Endpoints.DownloadMethods.file));

		executeDownload(2, "publicFile", () -> new PublicFileDownloader(serverUrl).downloadFile("file.tmp"));
		executeDownload(2, "publicFile", () -> new PublicFileDownloader(serverUrl).downloadFile("file.tmp"));
		
		executeDownload(2, "JDBC", () -> new FileDownloaderJdbc(serverUrl.getHost(), "9092", databaseFileName).downloadFile(fileId));
	}

	/** executes the downloadCode a certain number of times, logging the time result with a watch having the "donwloadType" id */
	private static void executeDownload(int nbExecutions, String downloadType, Runnable downloadCode) {
		StopWatch watch = new StopWatch(downloadType);
		for (int i=0; i<nbExecutions; i++) {
			watch.start(""+i);
			downloadCode.run();
			watch.stop();
		}
		log.info("\n{}: {}\n", downloadType, watch);
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
