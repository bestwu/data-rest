package cn.bestwu.framework.rest.support;

import cn.bestwu.framework.rest.exception.ETagDoesntMatchException;
import cn.bestwu.framework.util.Sha1DigestUtil;
import org.springframework.core.serializer.DefaultSerializer;
import org.springframework.core.serializer.Serializer;
import org.springframework.core.serializer.support.SerializationFailedException;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import java.io.ByteArrayOutputStream;

import static org.springframework.util.StringUtils.trimLeadingCharacter;
import static org.springframework.util.StringUtils.trimTrailingCharacter;

/**
 * A value object to represent ETags.
 */
public final class ETag {

	public static final ETag NO_ETAG = new ETag(null);

	private final String value;

	private static final Serializer<Object> serializer = new DefaultSerializer();

	private ETag(String value) {
		this.value = trimTrailingCharacter(trimLeadingCharacter(value, '"'), '"');
	}

	public static ETag from(String value) {
		return value == null ? NO_ETAG : new ETag(value);
	}

	public static ETag from(PersistentEntity<?, ?> entity, Object bean, boolean exact) {
		return from(getETagValue(entity, bean, exact));
	}

	public static String getETagValue(PersistentEntity<?, ?> entity, Object bean, boolean exact) {
		Assert.notNull(bean, "Target bean must not be null!");

		if (exact || entity == null) {
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream(1024);
			try {
				serializer.serialize(bean, byteStream);
				return Sha1DigestUtil.shaHex(new String(byteStream.toByteArray()));
			} catch (Throwable ex) {
				throw new SerializationFailedException("Failed to serialize object using " + serializer.getClass().getSimpleName(), ex);
			}
		} else {
			if (!entity.hasVersionProperty()) {
				return null;
			}

			PersistentPropertyAccessor accessor = entity.getPropertyAccessor(bean);
			String versionInfo = String.valueOf(accessor.getProperty(entity.getVersionProperty()));
			String idInfo = String.valueOf(accessor.getProperty(entity.getIdProperty()));
			return idInfo + ':' + versionInfo;
		}

	}

	public void verify(PersistentEntity<?, ?> entity, Object target, boolean exact) {

		if (this == NO_ETAG || target == null) {
			return;
		}

		if (!this.equals(from(entity, target, exact))) {
			throw new ETagDoesntMatchException(target, this);
		}
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
