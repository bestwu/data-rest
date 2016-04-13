package cn.bestwu.framework.support.client;

import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.charset.Charset;

public class PushbackBodyClientHttpResponseWrapper implements ClientHttpResponse {

	private final ClientHttpResponse response;

	private PushbackInputStream pushbackInputStream;

	private final int readlimit;

	public PushbackBodyClientHttpResponseWrapper(ClientHttpResponse response) {
		this.response = response;
		readlimit = 1024 * 1024 * 10;
	}

	@Override
	public HttpHeaders getHeaders() {
		return this.response.getHeaders();
	}

	@Override
	public InputStream getBody() throws IOException {
		return (this.pushbackInputStream != null ? this.pushbackInputStream : this.response.getBody());
	}

	public String readBody() throws IOException {
		InputStream body = response.getBody();
		if (body == null) {
			return null;
		} else if (body.markSupported()) {
			body.mark(readlimit);
			String bodyString = IOUtils.toString(body);
			body.reset();
			return bodyString;
		} else {
			this.pushbackInputStream = new PushbackInputStream(body, readlimit);
			String bodyString = IOUtils.toString(pushbackInputStream);
			pushbackInputStream.unread(bodyString.getBytes(Charset.forName("UTF-8")));
			return bodyString;
		}
	}

	@Override
	public HttpStatus getStatusCode() throws IOException {
		return this.response.getStatusCode();
	}

	@Override
	public int getRawStatusCode() throws IOException {
		return this.response.getRawStatusCode();
	}

	@Override
	public String getStatusText() throws IOException {
		return this.response.getStatusText();
	}

	@Override
	public void close() {
		this.response.close();
	}

}