package cn.bestwu.framework.rest.exception;

import cn.bestwu.framework.rest.support.ETag;
import org.springframework.util.Assert;

/**
 * An exception being thrown in case the {@link ETag} calculated for a particular object does not match an expected one.
 * 
 * @see ETag#verify(org.springframework.data.mapping.PersistentEntity, Object)
 */
public class ETagDoesntMatchException extends RuntimeException {

	private static final long serialVersionUID = 415835592506644699L;

	private final ETag expected;
	private final Object bean;

	/**
	 * Creates a new  for the given bean as well as the {@link ETag} it was expected to
	 * match.
	 * 
	 * @param bean must not be {@literal null}.
	 * @param expected must not be {@literal null}.
	 */
	public ETagDoesntMatchException(Object bean, ETag expected) {

		Assert.notNull(bean, "Target bean must not be null!");
		Assert.notNull(expected, "Expected ETag must not be null!");

		this.expected = expected;
		this.bean = bean;
	}

	/**
	 * Returns the bean not matching the {@link ETag}.
	 * 
	 * @return the bean
	 */
	public Object getBean() {
		return bean;
	}

	/**
	 * Returns the {@link ETag} the bean was expected to match.
	 * 
	 * @return the expected
	 */
	public ETag getExpectedETag() {
		return expected;
	}
}
