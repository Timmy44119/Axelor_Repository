package com.avr.apps.docgen.common;

import com.avr.apps.docgen.exception.AvrException;
import com.avr.apps.docgen.common.function.ThrowConsumer;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpCoreContext;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * This class is wrapper to HttpRequest from apache
 */
public final class HttpRequest {

	private static final Integer[] HTTP_STATUS_VALID = new Integer[]{200, 201, 202};

	public <U> U postMutipartFromData(final String url, final ThrowConsumer<FormData> formDatas, final Class<U> clazz) throws Exception {
		FormData formData = new FormData();
		formDatas.accept(formData);
		return send(new HttpPost(url), formData.builder.build(), clazz);
	}

	/**
	 * Get File
	 * @param url
	 * @param formDatas
	 * @param filename
	 * @return
	 * @throws Exception
	 */
	public File postMutipartFromData(final String url, final String filename, final ThrowConsumer<FormData> formDatas) throws Exception {
		FormData formData = new FormData();
		formDatas.accept(formData);
		String fullPath = Files.createTempDir().getAbsolutePath();
		return sendAndGetFile(new HttpPost(url), formData.builder.build(), new File(fullPath, filename));
	}

	/**
	 * Method static post
	 *
	 * @param url
	 * @param object
	 * @param header
	 * @return
	 * @throws IOException
	 */
	public <U> U post(final String url, String object, final Consumer<HttpHeader> header, final Class<U> clazz) throws Exception {
		if (ObjectUtils.isEmpty(object)) object = "";
		StringEntity stringEntity = new StringEntity(object);
		return send(header, new HttpPost(url), stringEntity, clazz);
	}

	/**
	 * Method static get
	 *
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public <U> U get(final String url, final Consumer<HttpHeader> header, final Class<U> clazz) throws Exception {
		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet(url);
		if (ObjectUtils.notEmpty(header)) {
			HttpHeader httpHeader = new HttpHeader();
			header.accept(httpHeader);
			for (Map.Entry<String, String> entry : httpHeader.header.entrySet()) {
				httpGet.addHeader(entry.getKey(), entry.getValue());
			}
		}
		HttpCoreContext httpCoreContext = new HttpCoreContext();
		CloseableHttpResponse resp = client.execute(httpGet, httpCoreContext);
		U res = getResponse(resp, clazz);
		resp.close();
		client.close();
		return res;
	}

	/**
	 * Method static put
	 *
	 * @param url
	 * @param object
	 * @param header
	 * @return
	 * @throws IOException
	 */
	public <U> U put(final String url, final String object, final Consumer<HttpHeader> header, final Class<U> clazz) throws Exception {
		StringEntity stringEntity = new StringEntity(object);
		return send(header, new HttpPut(url), stringEntity, clazz);
	}

	/**
	 * Method static patch
	 *
	 * @param url
	 * @param object
	 * @param header
	 * @return
	 * @throws IOException
	 */
	public <U> U patch(final String url, final String object, final Consumer<HttpHeader> header, final Class<U> clazz) throws Exception {
		StringEntity stringEntity = new StringEntity(object);
		return send(header, new HttpPatch(url), stringEntity, clazz);
	}

	/**
	 * Method static delete
	 *
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public <U> U delete(final String url, final Class<U> clazz) throws Exception {
		CloseableHttpClient client = HttpClients.createDefault();
		HttpDelete httpDelete = new HttpDelete(url);
		CloseableHttpResponse resp = client.execute(httpDelete);
		U res = getResponse(resp, clazz);
		resp.close();
		client.close();
		return res;
	}

	/**
	 * @param header
	 * @param httpEntityEnclosingRequestBase
	 * @param stringEntity
	 * @param clazz
	 * @param <U>
	 * @return
	 * @throws Exception
	 */
	private <U> U send(Consumer<HttpHeader> header, HttpEntityEnclosingRequestBase httpEntityEnclosingRequestBase, StringEntity stringEntity, final Class<U> clazz) throws Exception {
		if (ObjectUtils.notEmpty(header)) {
			HttpHeader httpHeader = new HttpHeader();
			header.accept(httpHeader);
			for (Map.Entry<String, String> entry : httpHeader.header.entrySet()) {
				httpEntityEnclosingRequestBase.setHeader(entry.getKey(), entry.getValue());
			}
			stringEntity.setContentType(httpHeader.contentType.toString());
			stringEntity.setChunked(true);
		}

		return send(httpEntityEnclosingRequestBase, stringEntity, clazz);
	}

	/**
	 * @param httpEntityEnclosingRequestBase
	 * @param stringEntity
	 * @param clazz
	 * @param <U>
	 * @return
	 * @throws Exception
	 */
	private <U> U send(HttpEntityEnclosingRequestBase httpEntityEnclosingRequestBase, HttpEntity stringEntity, final Class<U> clazz) throws Exception {
		CloseableHttpClient client = HttpClients.createDefault();
		httpEntityEnclosingRequestBase.setEntity(stringEntity);
		CloseableHttpResponse resp = client.execute(httpEntityEnclosingRequestBase);
		U res = getResponse(resp, clazz);
		resp.close();
		client.close();
		return res;
	}

	/**
	 * @param httpEntityEnclosingRequestBase
	 * @param stringEntity
	 * @return
	 * @throws Exception
	 */
	private File sendAndGetFile(HttpEntityEnclosingRequestBase httpEntityEnclosingRequestBase, HttpEntity stringEntity, File file) throws Exception {
		CloseableHttpClient client = HttpClients.createDefault();
		httpEntityEnclosingRequestBase.setEntity(stringEntity);
		CloseableHttpResponse resp = client.execute(httpEntityEnclosingRequestBase);
		FileUtils.copyInputStreamToFile(getResponse(resp), file);
		resp.close();
		client.close();
		return file;
	}

	private Boolean isValidResponse(CloseableHttpResponse response) {
		int statusCode = response.getStatusLine().getStatusCode();
		return Arrays.asList(HTTP_STATUS_VALID).contains(statusCode);
	}

	private InputStream getResponse(CloseableHttpResponse response) throws IOException, AvrException {
		if (Boolean.TRUE.equals(isValidResponse(response))) {
			return response.getEntity().getContent();
		}
		throw new AvrException(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, "Invalide request code : %s %s", response
				.getStatusLine()
				.getStatusCode(), response.getStatusLine().getReasonPhrase());
	}

	private <U> U getResponse(CloseableHttpResponse response, Class<U> classType) throws Exception {
		if (Boolean.TRUE.equals(isValidResponse(response))) {
			String rep = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
			ObjectMapper mapper = new ObjectMapper();
			response.close();
			return mapper.readValue(rep, classType);
		}
		throw new AvrException(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, "Invalide request code : %s %s", response
				.getStatusLine()
				.getStatusCode(), response.getStatusLine().getReasonPhrase());
	}

	public static class HttpHeader {

		private Map<String, String> header;
		private ContentType contentType;

		HttpHeader() {
			this.header = new HashMap<>();
		}

		public void set(String key, String val) {
			this.header.put(key, val);
		}

		public void setContentType(ContentType contentType) {
			this.contentType = contentType;
		}
	}

	public static class FormData {

		private final MultipartEntityBuilder builder;

		public FormData() {
			builder = MultipartEntityBuilder.create();
		}

		public FormData addValue(String name, String content) {
			this.addValue(name, content, ContentType.APPLICATION_JSON);
			return this;
		}

		public FormData addValue(String name, String content, ContentType contentType) {
			StringBody stringBody = new StringBody(content, contentType);
			builder.addPart(name, stringBody);
			return this;
		}

		public FormData addFile(String name, File file) throws FileNotFoundException {
			builder.addBinaryBody(name, new FileInputStream(file), ContentType.APPLICATION_OCTET_STREAM, file.getName());
			return this;
		}
	}

}