/*
 * Copyright 2014-2015 the original author or authors.
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
package cn.bestwu.framework.rest.support;

import cn.bestwu.framework.rest.exception.ETagDoesntMatchException;
import cn.bestwu.framework.util.Sha1DigestUtil;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import static org.springframework.util.StringUtils.trimLeadingCharacter;
import static org.springframework.util.StringUtils.trimTrailingCharacter;

/**
 * A value object to represent ETags.
 */
public final class ETag {

	public static final ETag NO_ETAG = new ETag(null);

	private final String value;

	private ETag(String value) {
		this.value = trimTrailingCharacter(trimLeadingCharacter(value, '"'), '"');
	}

	public static ETag from(String value) {
		return value == null ? NO_ETAG : new ETag(value);
	}

	public static ETag from(PersistentEntity<?, ?> entity, Object bean) {
		return from(getETagValue(entity, bean));
	}

	public static String getETagValue(PersistentEntity<?, ?> entity, Object bean) {
		Assert.notNull(entity, "PersistentEntity must not be null!");
		Assert.notNull(bean, "Target bean must not be null!");

		if (!entity.hasVersionProperty()) {
			return null;
		}

		PersistentPropertyAccessor accessor = entity.getPropertyAccessor(bean);
		String versionInfo = String.valueOf(accessor.getProperty(entity.getVersionProperty()));
		String idInfo = String.valueOf(accessor.getProperty(entity.getIdProperty()));
		return idInfo + ':' + versionInfo;
	}

	public void verify(PersistentEntity<?, ?> entity, Object target) {

		if (this == NO_ETAG || target == null) {
			return;
		}

		if (!this.equals(from(entity, target))) {
			throw new ETagDoesntMatchException(target, this);
		}
	}

	/**
	 * Returns whether the ETag matches the given {@link PersistentEntity} and target. A more dissenting way of
	 * checking matches as it does not match if the ETag is {@link #NO_ETAG}.
	 *
	 * @param entity must not be {@literal null}.
	 * @param target can be {@literal null}.
	 * @return 是否匹配
	 */
	public boolean matches(PersistentEntity<?, ?> entity, Object target) {

		if (this == NO_ETAG || target == null) {
			return false;
		}

		return this.equals(from(entity, target));
	}

	/**
	 * Adds the current ETag to the given headers.
	 *
	 * @param headers must not be {@literal null}.
	 * @return the {@link HttpHeaders} with the ETag header been set if the current ETag instance is not
	 * {@link #NO_ETAG}.
	 */
	public HttpHeaders addTo(HttpHeaders headers) {

		Assert.notNull(headers, "HttpHeaders must not be null!");
		String stringValue = toString();

		if (stringValue == null) {
			return headers;
		}

		headers.setETag(stringValue);
		return headers;
	}

	/* 
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return value == null ? null : "\"".concat(Sha1DigestUtil.shaHex(value)).concat("\"");
	}

	/* 
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}

		if (!(obj instanceof ETag)) {
			return false;
		}

		ETag that = (ETag) obj;

		return ObjectUtils.nullSafeEquals(this.value, that.value);
	}

	/* 
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return value.hashCode();
	}

}
