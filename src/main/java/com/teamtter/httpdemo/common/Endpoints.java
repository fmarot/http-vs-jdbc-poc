package com.teamtter.httpdemo.common;

public class Endpoints {
	public static final String download = "/dl";

	public class DownloadMethods {
		public static final String file = "/file";
		public static final String filename_var = "filename";
		public static final String file_spring = "/file_spring";
	}

	public static final String upload = "/upload";
	
	public class UploadMethods {
		public static final String file = "/file";
		public static final String filename_var = "filename";
		public static final String filesize_var = "filesize";
		
		public static final String serverInfos = "serverInfos";
		public static final String uploadMultipartFiles = "uploadMultipartFiles";
		
	}
}
