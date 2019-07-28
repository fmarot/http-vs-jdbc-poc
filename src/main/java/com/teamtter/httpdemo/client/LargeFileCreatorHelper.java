package com.teamtter.httpdemo.client;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LargeFileCreatorHelper {

	public static void createLargeFileIfNotExists(File myLargeFile) {
		if (!myLargeFile.exists()) {
			try (RandomAccessFile f = new RandomAccessFile(myLargeFile, "rw")) {
				f.setLength(1024 * 1024 * 1024 * 2 - 1); // 2Go
			} catch (IOException e) {
				log.error("", e);
			}
		}
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
