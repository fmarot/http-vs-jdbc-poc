package com.teamtter.httpdemo.client.filetransfer;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PublicFileDownloader {

	private URL serverUrl;

	public PublicFileDownloader(URL serverUrl) {
		this.serverUrl = serverUrl;
		
	}

	public void downloadFile(String toDownloadFilename) throws RuntimeException {
		
		try {
			URL fileUrl = new URL(serverUrl + "/files/" + toDownloadFilename);
			ReadableByteChannel readableByteChannel = Channels.newChannel(fileUrl.openStream());
			
			File downloadedFile = File.createTempFile(toDownloadFilename+"-", ".tmp");
			downloadedFile.deleteOnExit();
			
			try (FileOutputStream fileOutputStream = new FileOutputStream(downloadedFile);
					FileChannel fileChannel = fileOutputStream.getChannel()) {
				fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
			}
		} catch (Exception e) {
			log.error("PublicFileDownloader error: {}", toDownloadFilename, e);
		}
		
	}

}
