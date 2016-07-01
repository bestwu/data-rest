package cn.bestwu.framework.support.client;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.charset.Charset;

/**
 * 响应结果包装类
 * 可反复取响应内容
 *
 * @author Peter Wu
 */
public class PushbackBodyClientHttpResponseWrapper implements ClientHttpResponse {

	private final ClientHttpResponse response;
	private Charset defaultCharset = Charset.forName("UTF-8");
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
			String bodyString = StreamUtils.copyToString(body, defaultCharset);
			body.reset();
			return bodyString;
		} else {
			this.pushbackInputStream = new PushbackInputStream(body, readlimit);
			String bodyString = StreamUtils.copyToString(pushbackInputStream, defaultCharset);

			pushbackInputStream.unread(bodyString.getBytes(defaultCharset));
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