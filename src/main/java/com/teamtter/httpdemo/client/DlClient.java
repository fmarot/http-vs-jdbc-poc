package com.teamtter.httpdemo.client;

import java.io.File;
import java.io.IOException;
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
	private String			serverUrl;

	@Option(names = { "-u", "--upload" }, description = "file to upload")
	private File			toUploadFile;

	@Option(names = { "-d", "--download" }, description = "file to download in http")
	private String			toDownloadFile;

	@Option(names = { "-djdbc", "--downloadjdbc" }, description = "file to download in jdbc")
	private String			toDownloadFileJdbc;

	public static void main(String[] args) throws IOException {
		int exitCode = new CommandLine(new DlClient()).execute(args);
		System.exit(exitCode);
	}

	@Override
	public Integer call() throws Exception {
		serverUrl = serverUrl.replaceAll("/+$", "");	// replace trailing '/'
		OkHttpClient httpClient = buildHttpClient();
		if (toUploadFile != null) {
			new FileUploader(httpClient, serverUrl + "/" + Endpoints.upload).uploadFile(toUploadFile);
		}
		if (!StringUtils.isEmpty(toDownloadFile)) {
			new FileDownloader(httpClient).downloadFile(toDownloadFile);
		}
		if (!StringUtils.isEmpty(toDownloadFileJdbc)) {
			new FileDownloaderJdbc().downloadFile(toDownloadFileJdbc);
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
		logging.setLevel(Level.BODY);

		OkHttpClient client = new OkHttpClient().newBuilder()
				.addInterceptor(logging)
				.build();

		return client;
	}

}
