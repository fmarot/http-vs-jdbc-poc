package com.teamtter.httpdemo.client.filetransfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamtter.httpdemo.common.Endpoints;
import com.teamtter.httpdemo.common.ServerInfos;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

@Slf4j
public class HttpFileUploader {

	private static final MediaType	mediaTypeOctet	= MediaType.parse(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE);

	private OkHttpClient			httpClient;

	private URI						uploadUrl;

	private ObjectMapper			objectMapper;

	public HttpFileUploader(OkHttpClient httpClient, URI uploadUri) {
		this.httpClient = httpClient;
		this.uploadUrl = uploadUri;
		objectMapper = new ObjectMapper();
	}

	public void uploadFile(File toUploadFile) throws IOException {
		try (InputStream is = new FileInputStream(toUploadFile)) {
			Response response = uploadStreamTo(is,
					Endpoints.UploadMethods.filename_var + "=" + toUploadFile.getName()
							+ "&"
							+ Endpoints.UploadMethods.filesize_var + "=" + toUploadFile.length());
		}
	}

	public ServerInfos queryServerInfos() throws IOException {
		String url = uploadUrl + "/" + Endpoints.UploadMethods.serverInfos;
		Request request = new Request.Builder()
				.url(url.toString())
				.get()
				.build();
		Call call = httpClient.newCall(request);
		Response response = call.execute();
		String json = response.body().string();

		ServerInfos itemWithOwner = objectMapper.readValue(json, ServerInfos.class);

		return itemWithOwner;
	}

	/** Warning: The InpuStream will be automatically closed at the end of the upload. </br>
	 * WARNING: see the comment in {@link InputStreamRequestBody} because it is non-standard... */
	public Response uploadStreamTo(InputStream inputStream, String queryParameters) throws IOException {
		RequestBody requestBody = new StreamRequestBody(mediaTypeOctet, inputStream);

		String url = uploadUrl + "/" + Endpoints.UploadMethods.file;
		Request request = new Request.Builder()
				.url(url + "?" + queryParameters)
				.post(requestBody)
				.build();
		Call call = httpClient.newCall(request);
		Response response = call.execute();

		return response;
	}

	/** The InpuStream will be automatically closed at the end of the upload
	 * DANGER WILL ROBINSON
	 * This is more a hack than real reliable code as retries are impossible, bypassing classic okhttp use where input
	 * may be resent? But there the stream would be closed... More info:
	 * https://github.com/square/okhttp/pull/1038
	 * https://github.com/thegrizzlylabs/sardine-android/pull/15
	 * https://stackoverflow.com/questions/25367888/upload-binary-file-with-okhttp-from-resources/25384793#25384793
	 */
	class StreamRequestBody extends RequestBody {
		private MediaType	mediaType;
		private InputStream	inputStream;

		/** The InpuStream will be automatically closed at the end of the upload */
		public StreamRequestBody(final MediaType mediaType, final InputStream inputStream) {
			this.mediaType = mediaType;
			this.inputStream = inputStream;
		}

		@Override
		public boolean isOneShot() {
			return true;
		}

		@Override
		public MediaType contentType() {
			return mediaType;
		}

		@Override
		public long contentLength() {
			return -1;	// hey we have an inputStream so we do not know the total size that will be in there
		}

		@Override
		public void writeTo(BufferedSink sink) throws IOException {
			Source source = null;
			try {
				source = Okio.source(inputStream);
				sink.writeAll(source);
			} finally {
				// Util.closeQuietly(inputStream);
				// Util.closeQuietly(source);
			}
		}
	}
}
