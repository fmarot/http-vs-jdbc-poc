package com.teamtter.httpdemo.client.filetransfer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.Random;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LargeFileCreatorHelper {

	public static File createAutoDestructibleLargeFileInTmp(long nbBytes) throws IOException {
		Path path = Files.createTempFile("largeFileFrancois-", ".tmp");
		File file = path.toFile();
		file.deleteOnExit();

		byte[] randomBuffer = new byte[256];
		new Random().nextBytes(randomBuffer);
		 
		try (RandomAccessFile f = new RandomAccessFile(file, "rw")) {
			f.write(randomBuffer);
			f.setLength(nbBytes);
			f.seek(nbBytes - 256);
			f.write(randomBuffer);
		}
		log.info("Created file {} of size {}", file, nbBytes);
		return file;
	}

	public static String readableFileSize(long size) {
		if (size <= 0) {
			return "0";
		}
		final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
		int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
		return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}

}
