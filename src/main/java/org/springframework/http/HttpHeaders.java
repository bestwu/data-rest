/*
 * Copyright 2002-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.http;

import org.springframework.util.Assert;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.net.URI;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class HttpHeaders implements MultiValueMap<String, String>, Serializable {

	private static final long serialVersionUID = -8578554704772377436L;

	public static final String ACCEPT = "Accept";

	public static final String ACCEPT_CHARSET = "Accept-Charset";

	public static final String ACCEPT_ENCODING = "Accept-Encoding";

	public static final String ACCEPT_LANGUAGE = "Accept-Language";

	public static final String ACCEPT_RANGES = "Accept-Ranges";

	public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";

	public static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";

	public static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";

	public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";

	public static final String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";

	public static final String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";

	public static final String ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";

	public static final String ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";

	public static final String AGE = "Age";

	public static final String ALLOW = "Allow";

	public static final String AUTHORIZATION = "Authorization";

	public static final String CACHE_CONTROL = "Cache-Control";

	public static final String CONNECTION = "Connection";

	public static final String CONTENT_ENCODING = "Content-Encoding";

	public static final String CONTENT_DISPOSITION = "Content-Disposition";

	public static final String CONTENT_LANGUAGE = "Content-Language";

	public static final String CONTENT_LENGTH = "Content-Length";

	public static final String CONTENT_LOCATION = "Content-Location";

	public static final String CONTENT_RANGE = "Content-Range";

	public static final String CONTENT_TYPE = "Content-Type";

	public static final String COOKIE = "Cookie";

	public static final String DATE = "Date";

	public static final String ETAG = "ETag";

	public static final String EXPECT = "Expect";

	public static final String EXPIRES = "Expires";

	public static final String FROM = "From";

	public static final String HOST = "Host";

	public static final String IF_MATCH = "If-Match";

	public static final String IF_MODIFIED_SINCE = "If-Modified-Since";

	public static final String IF_NONE_MATCH = "If-None-Match";

	public static final String IF_RANGE = "If-Range";

	public static final String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";

	public static final String LAST_MODIFIED = "Last-Modified";

	public static final String LINK = "Link";

	public static final String LOCATION = "Location";

	public static final String MAX_FORWARDS = "Max-Forwards";

	public static final String ORIGIN = "Origin";

	public static final String PRAGMA = "Pragma";

	public static final String PROXY_AUTHENTICATE = "Proxy-Authenticate";

	public static final String PROXY_AUTHORIZATION = "Proxy-Authorization";

	public static final String RANGE = "Range";

	public static final String REFERER = "Referer";

	public static final String RETRY_AFTER = "Retry-After";

	public static final String SERVER = "Server";

	public static final String SET_COOKIE = "Set-Cookie";

	public static final String SET_COOKIE2 = "Set-Cookie2";

	public static final String TE = "TE";

	public static final String TRAILER = "Trailer";

	public static final String TRANSFER_ENCODING = "Transfer-Encoding";

	public static final String UPGRADE = "Upgrade";

	public static final String USER_AGENT = "User-Agent";

	public static final String VARY = "Vary";

	public static final String VIA = "Via";

	public static final String WARNING = "Warning";

	public static final String WWW_AUTHENTICATE = "WWW-Authenticate";

	private static final String[] DATE_FORMATS = new String[] {
			"EEE, dd MMM yyyy HH:mm:ss zzz",
			"EEE, dd-MMM-yy HH:mm:ss zzz",
			"EEE MMM dd HH:mm:ss yyyy"
	};

	private static TimeZone GMT = TimeZone.getTimeZone("GMT");

	private final Map<String, List<String>> headers;

	public HttpHeaders() {
		this(new LinkedCaseInsensitiveMap<>(8, Locale.ENGLISH), false);
	}

	private HttpHeaders(Map<String, List<String>> headers, boolean readOnly) {
		Assert.notNull(headers, "'headers' must not be null");
		if (readOnly) {
			Map<String, List<String>> map =
					new LinkedCaseInsensitiveMap<>(headers.size(), Locale.ENGLISH);
			for (Entry<String, List<String>> entry : headers.entrySet()) {
				List<String> values = Collections.unmodifiableList(entry.getValue());
				map.put(entry.getKey(), values);
			}
			this.headers = Collections.unmodifiableMap(map);
		} else {
			this.headers = headers;
		}
	}

	public void setAccept(List<MediaType> acceptableMediaTypes) {
		set(ACCEPT, MediaType.toString(acceptableMediaTypes));
	}

	public List<MediaType> getAccept() {
		String value = getFirst(ACCEPT);
		List<MediaType> result = (value != null ? MediaType.parseMediaTypes(value) : Collections.<MediaType>emptyList());

		// Some containers parse 'Accept' into multiple values
		if (result.size() == 1) {
			List<String> acceptHeader = get(ACCEPT);
			if (acceptHeader.size() > 1) {
				value = StringUtils.collectionToCommaDelimitedString(acceptHeader);
				result = MediaType.parseMediaTypes(value);
			}
		}

		return result;
	}

	public void setAccessControlAllowCredentials(boolean allowCredentials) {
		set(ACCESS_CONTROL_ALLOW_CREDENTIALS, Boolean.toString(allowCredentials));
	}

	public boolean getAccessControlAllowCredentials() {
		return Boolean.valueOf(getFirst(ACCESS_CONTROL_ALLOW_CREDENTIALS));
	}

	public void setAccessControlAllowHeaders(List<String> allowedHeaders) {
		set(ACCESS_CONTROL_ALLOW_HEADERS, toCommaDelimitedString(allowedHeaders));
	}

	public List<String> getAccessControlAllowHeaders() {
		return getFirstValueAsList(ACCESS_CONTROL_ALLOW_HEADERS);
	}

	public void setAccessControlAllowMethods(List<HttpMethod> allowedMethods) {
		set(ACCESS_CONTROL_ALLOW_METHODS, StringUtils.collectionToCommaDelimitedString(allowedMethods));
	}

	public List<HttpMethod> getAccessControlAllowMethods() {
		List<HttpMethod> result = new ArrayList<>();
		String value = getFirst(ACCESS_CONTROL_ALLOW_METHODS);
		if (value != null) {
			String[] tokens = value.split(",\\s*");
			for (String token : tokens) {
				result.add(HttpMethod.valueOf(token));
			}
		}
		return result;
	}

	public void setAccessControlAllowOrigin(String allowedOrigin) {
		set(ACCESS_CONTROL_ALLOW_ORIGIN, allowedOrigin);
	}

	public String getAccessControlAllowOrigin() {
		return getFirst(ACCESS_CONTROL_ALLOW_ORIGIN);
	}

	public void setAccessControlExposeHeaders(List<String> exposedHeaders) {
		set(ACCESS_CONTROL_EXPOSE_HEADERS, toCommaDelimitedString(exposedHeaders));
	}

	public List<String> getAccessControlExposeHeaders() {
		return getFirstValueAsList(ACCESS_CONTROL_EXPOSE_HEADERS);
	}

	public void setAccessControlMaxAge(long maxAge) {
		set(ACCESS_CONTROL_MAX_AGE, Long.toString(maxAge));
	}

	public long getAccessControlMaxAge() {
		String value = getFirst(ACCESS_CONTROL_MAX_AGE);
		return (value != null ? Long.parseLong(value) : -1);
	}

	public void setAccessControlRequestHeaders(List<String> requestHeaders) {
		set(ACCESS_CONTROL_REQUEST_HEADERS, toCommaDelimitedString(requestHeaders));
	}

	public List<String> getAccessControlRequestHeaders() {
		return getFirstValueAsList(ACCESS_CONTROL_REQUEST_HEADERS);
	}

	public void setAccessControlRequestMethod(HttpMethod requestedMethod) {
		set(ACCESS_CONTROL_REQUEST_METHOD, requestedMethod.name());
	}

	public HttpMethod getAccessControlRequestMethod() {
		String value = getFirst(ACCESS_CONTROL_REQUEST_METHOD);
		return (value != null ? HttpMethod.valueOf(value) : null);
	}

	public void setAcceptCharset(List<Charset> acceptableCharsets) {
		StringBuilder builder = new StringBuilder();
		for (Iterator<Charset> iterator = acceptableCharsets.iterator(); iterator.hasNext(); ) {
			Charset charset = iterator.next();
			builder.append(charset.name().toLowerCase(Locale.ENGLISH));
			if (iterator.hasNext()) {
				builder.append(", ");
			}
		}
		set(ACCEPT_CHARSET, builder.toString());
	}

	public List<Charset> getAcceptCharset() {
		List<Charset> result = new ArrayList<>();
		String value = getFirst(ACCEPT_CHARSET);
		if (value != null) {
			String[] tokens = value.split(",\\s*");
			for (String token : tokens) {
				int paramIdx = token.indexOf(';');
				String charsetName;
				if (paramIdx == -1) {
					charsetName = token;
				} else {
					charsetName = token.substring(0, paramIdx);
				}
				if (!charsetName.equals("*")) {
					result.add(Charset.forName(charsetName));
				}
			}
		}
		return result;
	}

	public void setAllow(Set<HttpMethod> allowedMethods) {
		set(ALLOW, StringUtils.collectionToCommaDelimitedString(allowedMethods));
	}

	public Set<HttpMethod> getAllow() {
		String value = getFirst(ALLOW);
		if (!StringUtils.isEmpty(value)) {
			List<HttpMethod> allowedMethod = new ArrayList<>(5);
			String[] tokens = value.split(",\\s*");
			for (String token : tokens) {
				allowedMethod.add(HttpMethod.valueOf(token));
			}
			return EnumSet.copyOf(allowedMethod);
		} else {
			return EnumSet.noneOf(HttpMethod.class);
		}
	}

	public void setCacheControl(String cacheControl) {
		set(CACHE_CONTROL, cacheControl);
	}

	public String getCacheControl() {
		return getFirst(CACHE_CONTROL);
	}

	public void setConnection(String connection) {
		set(CONNECTION, connection);
	}

	public void setConnection(List<String> connection) {
		set(CONNECTION, toCommaDelimitedString(connection));
	}

	public List<String> getConnection() {
		return getFirstValueAsList(CONNECTION);
	}

	public void setContentDispositionFormData(String name, String filename) {
		Assert.notNull(name, "'name' must not be null");
		StringBuilder builder = new StringBuilder("form-data; name=\"");
		builder.append(name).append('\"');
		if (filename != null) {
			builder.append("; filename=\"");
			builder.append(filename).append('\"');
		}
		set(CONTENT_DISPOSITION, builder.toString());
	}

	public void setContentLength(long contentLength) {
		set(CONTENT_LENGTH, Long.toString(contentLength));
	}

	public long getContentLength() {
		String value = getFirst(CONTENT_LENGTH);
		return (value != null ? Long.parseLong(value) : -1);
	}

	public void setContentType(MediaType mediaType) {
		Assert.isTrue(!mediaType.isWildcardType(), "'Content-Type' cannot contain wildcard type '*'");
		Assert.isTrue(!mediaType.isWildcardSubtype(), "'Content-Type' cannot contain wildcard subtype '*'");
		set(CONTENT_TYPE, mediaType.toString());
	}

	public MediaType getContentType() {
		String value = getFirst(CONTENT_TYPE);
		return (StringUtils.hasLength(value) ? MediaType.parseMediaType(value) : null);
	}

	public void setDate(long date) {
		setDate(DATE, date);
	}

	public long getDate() {
		return getFirstDate(DATE);
	}

	public void setETag(String eTag) {
		if (eTag != null) {
			Assert.isTrue(eTag.startsWith("\"") || eTag.startsWith("W/"),
					"Invalid eTag, does not start with W/ or \"");
			Assert.isTrue(eTag.endsWith("\""), "Invalid eTag, does not end with \"");
		}
		set(ETAG, eTag);
	}

	public String getETag() {
		return getFirst(ETAG);
	}

	public void setExpires(long expires) {
		setDate(EXPIRES, expires);
	}

	public long getExpires() {
		try {
			return getFirstDate(EXPIRES);
		} catch (IllegalArgumentException ex) {
			return -1;
		}
	}

	public void setIfModifiedSince(long ifModifiedSince) {
		setDate(IF_MODIFIED_SINCE, ifModifiedSince);
	}

	public long getIfModifiedSince() {
		return getFirstDate(IF_MODIFIED_SINCE);
	}

	public void setIfNoneMatch(String ifNoneMatch) {
		set(IF_NONE_MATCH, ifNoneMatch);
	}

	public void setIfNoneMatch(List<String> ifNoneMatchList) {
		set(IF_NONE_MATCH, toCommaDelimitedString(ifNoneMatchList));
	}

	protected String toCommaDelimitedString(List<String> list) {
		StringBuilder builder = new StringBuilder();
		for (Iterator<String> iterator = list.iterator(); iterator.hasNext(); ) {
			String ifNoneMatch = iterator.next();
			builder.append(ifNoneMatch);
			if (iterator.hasNext()) {
				builder.append(", ");
			}
		}
		return builder.toString();
	}

	public List<String> getIfNoneMatch() {
		return getFirstValueAsList(IF_NONE_MATCH);
	}

	protected List<String> getFirstValueAsList(String header) {
		List<String> result = new ArrayList<>();
		String value = getFirst(header);
		if (value != null) {
			String[] tokens = value.split(",\\s*");
			Collections.addAll(result, tokens);
		}
		return result;
	}

	public void setLastModified(long lastModified) {
		setDate(LAST_MODIFIED, lastModified);
	}

	public long getLastModified() {
		return getFirstDate(LAST_MODIFIED);
	}

	public void setLocation(URI location) {
		set(LOCATION, location.toASCIIString());
	}

	public URI getLocation() {
		String value = getFirst(LOCATION);
		return (value != null ? URI.create(value) : null);
	}

	public void setOrigin(String origin) {
		set(ORIGIN, origin);
	}

	public String getOrigin() {
		return getFirst(ORIGIN);
	}

	public void setPragma(String pragma) {
		set(PRAGMA, pragma);
	}

	public String getPragma() {
		return getFirst(PRAGMA);
	}

	public void setRange(List<HttpRange> ranges) {
		String value = HttpRange.toString(ranges);
		set(RANGE, value);
	}

	public List<HttpRange> getRange() {
		String value = getFirst(RANGE);
		return HttpRange.parseRanges(value);
	}

	public void setUpgrade(String upgrade) {
		set(UPGRADE, upgrade);
	}

	public String getUpgrade() {
		return getFirst(UPGRADE);
	}

	public long getFirstDate(String headerName) {
		String headerValue = getFirst(headerName);
		if (headerValue == null) {
			return -1;
		}
		for (String dateFormat : DATE_FORMATS) {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.US);
			simpleDateFormat.setTimeZone(GMT);
			try {
				return simpleDateFormat.parse(headerValue).getTime();
			} catch (ParseException ex) {
				// ignore
			}
		}

		return -1;
		//		throw new IllegalArgumentException("Cannot parse date value \"" + headerValue +
		//				"\" for \"" + headerName + "\" header");
	}

	public void setDate(String headerName, long date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMATS[0], Locale.US);
		dateFormat.setTimeZone(GMT);
		set(headerName, dateFormat.format(new Date(date)));
	}

	@Override
	public String getFirst(String headerName) {
		List<String> headerValues = this.headers.get(headerName);
		return (headerValues != null ? headerValues.get(0) : null);
	}

	@Override
	public void add(String headerName, String headerValue) {
		List<String> headerValues = this.headers.get(headerName);
		if (headerValues == null) {
			headerValues = new LinkedList<>();
			this.headers.put(headerName, headerValues);
		}
		headerValues.add(headerValue);
	}

	@Override
	public void set(String headerName, String headerValue) {
		List<String> headerValues = new LinkedList<>();
		headerValues.add(headerValue);
		this.headers.put(headerName, headerValues);
	}

	@Override
	public void setAll(Map<String, String> values) {
		for (Entry<String, String> entry : values.entrySet()) {
			set(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public Map<String, String> toSingleValueMap() {
		LinkedHashMap<String, String> singleValueMap = new LinkedHashMap<>(this.headers.size());
		for (Entry<String, List<String>> entry : this.headers.entrySet()) {
			singleValueMap.put(entry.getKey(), entry.getValue().get(0));
		}
		return singleValueMap;
	}

	// Map implementation

	@Override
	public int size() {
		return this.headers.size();
	}

	@Override
	public boolean isEmpty() {
		return this.headers.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return this.headers.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return this.headers.containsValue(value);
	}

	@Override
	public List<String> get(Object key) {
		return this.headers.get(key);
	}

	@Override
	public List<String> put(String key, List<String> value) {
		return this.headers.put(key, value);
	}

	@Override
	public List<String> remove(Object key) {
		return this.headers.remove(key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends List<String>> map) {
		this.headers.putAll(map);
	}

	@Override
	public void clear() {
		this.headers.clear();
	}

	@Override
	public Set<String> keySet() {
		return this.headers.keySet();
	}

	@Override
	public Collection<List<String>> values() {
		return this.headers.values();
	}

	@Override
	public Set<Entry<String, List<String>>> entrySet() {
		return this.headers.entrySet();
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof HttpHeaders)) {
			return false;
		}
		HttpHeaders otherHeaders = (HttpHeaders) other;
		return this.headers.equals(otherHeaders.headers);
	}

	@Override
	public int hashCode() {
		return this.headers.hashCode();
	}

	@Override
	public String toString() {
		return this.headers.toString();
	}

	public static HttpHeaders readOnlyHttpHeaders(HttpHeaders headers) {
		return new HttpHeaders(headers, true);
	}

}
