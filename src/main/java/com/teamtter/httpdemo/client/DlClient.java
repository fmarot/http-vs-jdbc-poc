package com.teamtter.httpdemo.client;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Callable;

import org.springframework.util.StringUtils;

import com.teamtter.httpdemo.common.Endpoints;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "dlclient", mixinStandardHelpOptions = true, description = "bla bla ...")
@Slf4j
public class DlClient implements Callable<Integer> {

	@Parameters(index = "0", description = "The serverIp", defaultValue = "http://127.0.0.1:8080")
	private URL			serverUrl;

	@Option(names = { "-u", "--upload" }, description = "file to upload", defaultValue="LICENSE")
	private File			toUploadFile;

	@Option(names = { "-d", "--download" }, description = "file to download in http", defaultValue="LICENSE")
	private String			toDownloadFile;

	@Option(names = { "-djdbc", "--downloadjdbc" }, description = "file to download in jdbc", defaultValue="LICENSE")
	private String			toDownloadFileJdbc;

	public static void main(String[] args) throws IOException {
		int exitCode = new CommandLine(new DlClient()).execute(args);
		System.exit(exitCode);
	}

	@Override
	public Integer call() throws Exception {
		OkHttpClient httpClient = buildHttpClient();
		if (toUploadFile != null) {
			new FileUploader(httpClient, serverUrl.toURI().resolve(Endpoints.upload +Endpoints.UploadMethods.file ))
				.uploadFile(toUploadFile);
		}
		if (!StringUtils.isEmpty(toDownloadFile)) {
			new FileDownloader(httpClient, serverUrl).downloadFile(toDownloadFile);
		}
		if (!StringUtils.isEmpty(toDownloadFileJdbc)) {
			new FileDownloaderJdbc(serverUrl.getHost(), "9092").downloadFile(toDownloadFileJdbc);
		}
		return 0;
	}

	private static OkHttpClient buildHttpClient() {
		HttpLoggingInterceptor.Logger logger = new HttpLoggingInterceptor.Logger() {
			@Override
			public void log(String message) {
				log.info(message);
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
