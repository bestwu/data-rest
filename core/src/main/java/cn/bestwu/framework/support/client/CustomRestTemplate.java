package cn.bestwu.framework.support.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.feed.AtomFeedHttpMessageConverter;
import org.springframework.http.converter.feed.RssChannelHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.web.client.*;
import org.springframework.web.util.UriTemplate;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 可在请求头中设置版本信息
 *
 * @author Peter Wu
 */
public class CustomRestTemplate extends TestRestTemplate {
	protected final Logger logger = LoggerFactory.getLogger(CustomRestTemplate.class);

	private boolean print;

	public void setPrint(boolean print) {
		this.print = print;
	}

	public CustomRestTemplate(HttpClientOption... httpClientOptions) {
		super(httpClientOptions);
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setOutputStreaming(false);
		setRequestFactory(requestFactory);

		boolean romePresent =
				ClassUtils.isPresent("com.rometools.rome.feed.WireFeed", RestTemplate.class.getClassLoader());

		final boolean jaxb2Present =
				ClassUtils.isPresent("javax.xml.bind.Binder", RestTemplate.class.getClassLoader());

		final boolean jackson2Present =
				ClassUtils.isPresent("com.fasterxml.jackson.databind.ObjectMapper", RestTemplate.class.getClassLoader()) &&
						ClassUtils.isPresent("com.fasterxml.jackson.core.JsonGenerator", RestTemplate.class.getClassLoader());

		final boolean jackson2XmlPresent =
				ClassUtils.isPresent("com.fasterxml.jackson.dataformat.xml.XmlMapper", RestTemplate.class.getClassLoader());

		final boolean gsonPresent =
				ClassUtils.isPresent("com.google.gson.Gson", RestTemplate.class.getClassLoader());

		final List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
		messageConverters.add(new ByteArrayHttpMessageConverter());
		messageConverters.add(new StringHttpMessageConverter(Charset.forName("UTF-8")));//UTF-8 编码
		messageConverters.add(new ResourceHttpMessageConverter());
		messageConverters.add(new SourceHttpMessageConverter<>());
		messageConverters.add(new UTF8AllEncompassingFormHttpMessageConverter());

		if (romePresent) {
			messageConverters.add(new AtomFeedHttpMessageConverter());
			messageConverters.add(new RssChannelHttpMessageConverter());
		}

		if (jackson2XmlPresent) {
			messageConverters.add(new MappingJackson2XmlHttpMessageConverter());
		} else if (jaxb2Present) {
			messageConverters.add(new Jaxb2RootElementHttpMessageConverter());
		}

		if (jackson2Present) {
			messageConverters.add(new MappingJackson2HttpMessageConverter());
		} else if (gsonPresent) {
			messageConverters.add(new GsonHttpMessageConverter());
		}
		setMessageConverters(messageConverters);
	}

	public CustomRestTemplate(String username, String password, HttpClientOption... httpClientOptions) {
		super(username, password, httpClientOptions);
	}

	public <T> T execute(String url, HttpMethod method, RequestCallback requestCallback,
			ResponseExtractor<T> responseExtractor, MediaType mediaType, Object... urlVariables) throws RestClientException {
		URI expanded = new UriTemplate(url).expand(urlVariables);
		return doExecute(expanded, method, requestCallback, responseExtractor, mediaType);
	}

	public <T> T execute(String url, HttpMethod method, RequestCallback requestCallback,
			ResponseExtractor<T> responseExtractor, MediaType mediaType, Map<String, ?> urlVariables) throws RestClientException {
		URI expanded = new UriTemplate(url).expand(urlVariables);
		return doExecute(expanded, method, requestCallback, responseExtractor, mediaType);
	}

	@Override
	protected <T> T doExecute(URI url, HttpMethod method, RequestCallback requestCallback, ResponseExtractor<T> responseExtractor) throws RestClientException {
		return doExecute(url, method, requestCallback, responseExtractor, null);
	}

	protected <T> T doExecute(URI url, HttpMethod method, RequestCallback requestCallback,
			ResponseExtractor<T> responseExtractor, MediaType mediaType) throws RestClientException {

		Assert.notNull(url, "'url' must not be null");
		Assert.notNull(method, "'method' must not be null");
		ClientHttpResponse response = null;
		try {
			ClientHttpRequest request = createRequest(url, method);

			if (requestCallback != null) {
				requestCallback.doWithRequest(request);
			}

			HttpHeaders requestHeaders = request.getHeaders();

			if (mediaType != null) {
				requestHeaders.setAccept(Collections.singletonList(mediaType));
			}

			response = request.execute();

			if (print && logger.isDebugEnabled()) {
				logger.debug("------------------------------");
				logger.debug("requestHeaders:");
				requestHeaders.forEach((s, strings) -> logger.debug(s + " : " + strings));
				logger.debug("------------------------------");
			}

			HttpHeaders responseHeaders = response.getHeaders();
			if (print && logger.isDebugEnabled()) {
				logger.debug("------------------------------");
				logger.debug("responseHeaders:");
				responseHeaders.forEach((s, strings) -> logger.debug(s + " : " + strings));
				logger.debug("------------------------------");
			}

			handleResponse(url, method, response);
			if (responseExtractor != null) {
				if (print && logger.isDebugEnabled()) {
					logger.debug("------------------------------");
					response = new PushbackBodyClientHttpResponseWrapper(response);
					logger.debug("responseBody: \n" + ((PushbackBodyClientHttpResponseWrapper) response).readBody());
					logger.debug("------------------------------");
				}
				return responseExtractor.extractData(response);
			} else {
				return null;
			}
		} catch (IOException ex) {
			throw new ResourceAccessException("I/O error on " + method.name() +
					" request for \"" + url + "\":" + ex.getMessage(), ex);
		} finally {
			if (response != null) {
				response.close();
			}
		}
	}

	public <T> T getForObject(String url, Class<T> responseType, MediaType mediaType, Object... urlVariables) throws RestClientException {
		RequestCallback requestCallback = acceptHeaderRequestCallback(responseType);
		HttpMessageConverterExtractor<T> responseExtractor =
				new HttpMessageConverterExtractor<>(responseType, getMessageConverters());
		return execute(url, HttpMethod.GET, requestCallback, responseExtractor, mediaType, urlVariables);
	}

	public <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType, MediaType mediaType, Map<String, ?> urlVariables) throws RestClientException {
		RequestCallback requestCallback = acceptHeaderRequestCallback(responseType);
		ResponseExtractor<ResponseEntity<T>> responseExtractor = responseEntityExtractor(responseType);
		return execute(url, HttpMethod.GET, requestCallback, responseExtractor, mediaType, urlVariables);
	}

	public <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType, MediaType mediaType, Object... urlVariables) throws RestClientException {
		RequestCallback requestCallback = acceptHeaderRequestCallback(responseType);
		ResponseExtractor<ResponseEntity<T>> responseExtractor = responseEntityExtractor(responseType);
		return execute(url, HttpMethod.GET, requestCallback, responseExtractor, mediaType, urlVariables);
	}

	public <T> T postForObject(String url, Object request, Class<T> responseType, MediaType mediaType, Object... uriVariables)
			throws RestClientException {
		RequestCallback requestCallback = httpEntityCallback(request, responseType);
		HttpMessageConverterExtractor<T> responseExtractor =
				new HttpMessageConverterExtractor<>(responseType, getMessageConverters());
		return execute(url, HttpMethod.POST, requestCallback, responseExtractor, mediaType, uriVariables);
	}

	public <T> ResponseEntity<T> postForEntity(String url, Object request, Class<T> responseType, MediaType mediaType, Object... uriVariables)
			throws RestClientException {
		RequestCallback requestCallback = httpEntityCallback(request, responseType);
		ResponseExtractor<ResponseEntity<T>> responseExtractor = responseEntityExtractor(responseType);
		return execute(url, HttpMethod.POST, requestCallback, responseExtractor, mediaType, uriVariables);
	}

	public <T> T putForObject(String url, Object request, Class<T> responseType, Object... uriVariables) {
		RequestCallback requestCallback = httpEntityCallback(request, responseType);
		HttpMessageConverterExtractor<T> responseExtractor =
				new HttpMessageConverterExtractor<>(responseType, getMessageConverters());
		return execute(url, HttpMethod.PUT, requestCallback, responseExtractor, null, null, uriVariables);
	}

	public <T> T putForObject(String url, Object request, Class<T> responseType, MediaType mediaType, Object... uriVariables) {
		RequestCallback requestCallback = httpEntityCallback(request, responseType);
		HttpMessageConverterExtractor<T> responseExtractor =
				new HttpMessageConverterExtractor<>(responseType, getMessageConverters());
		return execute(url, HttpMethod.PUT, requestCallback, responseExtractor, mediaType, uriVariables);
	}

	public <T> ResponseEntity<T> putForEntity(String url, Object request, Class<T> responseType, MediaType mediaType, Object... uriVariables)
			throws RestClientException {

		RequestCallback requestCallback = httpEntityCallback(request, responseType);
		ResponseExtractor<ResponseEntity<T>> responseExtractor = responseEntityExtractor(responseType);
		return execute(url, HttpMethod.PUT, requestCallback, responseExtractor, mediaType, uriVariables);
	}

	public <T> ResponseEntity<T> putForEntity(String url, Object request, Class<T> responseType, Object... uriVariables)
			throws RestClientException {

		RequestCallback requestCallback = httpEntityCallback(request, responseType);
		ResponseExtractor<ResponseEntity<T>> responseExtractor = responseEntityExtractor(responseType);
		return execute(url, HttpMethod.PUT, requestCallback, responseExtractor, null, uriVariables);
	}

	public <T> ResponseEntity<T> deleteForEntity(String url, MediaType mediaType, Object... uriVariables) throws RestClientException {
		ResponseExtractor<ResponseEntity<T>> responseExtractor = responseEntityExtractor(null);
		return execute(url, HttpMethod.DELETE, null, responseExtractor, mediaType, uriVariables);
	}

	public <T> ResponseEntity<T> deleteForEntity(String url, MediaType mediaType, Map<String, ?> urlVariables) throws RestClientException {
		ResponseExtractor<ResponseEntity<T>> responseExtractor = responseEntityExtractor(null);
		return execute(url, HttpMethod.DELETE, null, responseExtractor, mediaType, urlVariables);
	}

	public <T> ResponseEntity<T> deleteForEntity(String url, Object... uriVariables) throws RestClientException {
		ResponseExtractor<ResponseEntity<T>> responseExtractor = responseEntityExtractor(null);
		return execute(url, HttpMethod.DELETE, null, responseExtractor, null, uriVariables);
	}

	public <T> ResponseEntity<T> deleteForEntity(String url, Map<String, ?> urlVariables) throws RestClientException {
		ResponseExtractor<ResponseEntity<T>> responseExtractor = responseEntityExtractor(null);
		return execute(url, HttpMethod.DELETE, null, responseExtractor, null, urlVariables);
	}

}
