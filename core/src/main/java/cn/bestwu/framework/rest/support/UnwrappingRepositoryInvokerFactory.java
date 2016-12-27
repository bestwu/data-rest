/*
 * Copyright 2015 the original author or authors.
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

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.support.RepositoryInvoker;
import org.springframework.data.repository.support.RepositoryInvokerFactory;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.MultiValueMap;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;

/**
 * {@link RepositoryInvokerFactory} that wraps the {@link RepositoryInvokerFactory} returned by the delegate with one
 * that automatically unwraps JDK 8 {@link Optional} and Guava {@link com.google.common.base.Optional}s.
 *
 * @author Peter Wu
 */
public class UnwrappingRepositoryInvokerFactory implements RepositoryInvokerFactory {

	private static final List<Converter<Object, Object>> CONVERTERS;

	static {

		List<Converter<Object, Object>> converters = new ArrayList<>();
		ClassLoader classLoader = UnwrappingRepositoryInvokerFactory.class.getClassLoader();

		// Add unwrapper for Java 8 Optional

		if (ClassUtils.isPresent("java.util.Optional", classLoader)) {
			converters.add(new Converter<Object, Object>() {
				@Override public Object convert(Object source) {
					return source instanceof Optional ? ((Optional<?>) source).orElse(null) : source;
				}
			});
		}

		// Add unwrapper for Guava Optional

		if (ClassUtils.isPresent("com.google.common.base.Optional", classLoader)) {

			converters.add(new Converter<Object, Object>() {
				@Override public Object convert(Object source) {
					return source instanceof com.google.common.base.Optional ? ((com.google.common.base.Optional<?>) source)
							.orNull() : source;
				}
			});
		}

		CONVERTERS = Collections.unmodifiableList(converters);
	}

	private final RepositoryInvokerFactory delegate;

	/**
	 * Creates a new  UnwrappingRepositoryInvokerFactory.
	 *
	 * @param delegate must not be {@literal null}.
	 */
	public UnwrappingRepositoryInvokerFactory(RepositoryInvokerFactory delegate) {

		Assert.notNull(delegate, "Delegate RepositoryInvokerFactory must not be null!");

		this.delegate = delegate;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.data.repository.support.RepositoryInvokerFactory#getInvokerFor(java.lang.Class)
	 */
	@Override
	public RepositoryInvoker getInvokerFor(Class<?> domainType) {
		return new UnwrappingRepositoryInvoker(delegate.getInvokerFor(domainType), CONVERTERS);
	}

	/**
	 * {@link RepositoryInvoker} that post-processes invocations of {@link RepositoryInvoker#invokeFindOne(Serializable)}
	 * and {@link #invokeQueryMethod(Method, MultiValueMap, Pageable, Sort)} using the given {@link Converter}s.
	 *
	 * @author Oliver Gierke
	 */
	private static class UnwrappingRepositoryInvoker implements RepositoryInvoker {

		private final RepositoryInvoker delegate;
		private final Collection<Converter<Object, Object>> converters;

		/**
		 * Creates a new UnwrappingRepositoryInvoker for the given delegate and {@link Converter}s.
		 *
		 * @param delegate   must not be {@literal null}.
		 * @param converters must not be {@literal null}.
		 */
		public UnwrappingRepositoryInvoker(RepositoryInvoker delegate, Collection<Converter<Object, Object>> converters) {

			Assert.notNull(delegate, "Delegate RepositoryInvoker must not be null!");
			Assert.notNull(converters, "Converters must not be null!");

			this.delegate = delegate;
			this.converters = converters;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.repository.support.RepositoryInvoker#invokeFindOne(java.io.Serializable)
		 */
		@SuppressWarnings("unchecked")
		public <T> T invokeFindOne(Serializable id) {
			return (T) postProcess(delegate.invokeFindOne(id));
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.data.repository.support.RepositoryInvoker#invokeQueryMethod(java.lang.reflect.Method, java.util.Map, org.springframework.data.domain.Pageable, org.springframework.data.domain.Sort)
		 */
		@Override
		@SuppressWarnings("deprecation")
		public Object invokeQueryMethod(Method method, Map<String, String[]> parameters, Pageable pageable, Sort sort) {
			return postProcess(delegate.invokeQueryMethod(method, parameters, pageable, sort));
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.data.repository.support.RepositoryInvoker#invokeQueryMethod(java.lang.reflect.Method, org.springframework.util.MultiValueMap, org.springframework.data.domain.Pageable, org.springframework.data.domain.Sort)
		 */
		@Override
		public Object invokeQueryMethod(Method method, MultiValueMap<String, ?> parameters,
				Pageable pageable, Sort sort) {
			return postProcess(delegate.invokeQueryMethod(method, parameters, pageable, sort));
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.data.repository.support.RepositoryInvocationInformation#hasDeleteMethod()
		 */
		@Override
		public boolean hasDeleteMethod() {
			return delegate.hasDeleteMethod();
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.data.repository.support.RepositoryInvocationInformation#hasFindAllMethod()
		 */
		@Override
		public boolean hasFindAllMethod() {
			return delegate.hasFindAllMethod();
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.data.repository.support.RepositoryInvocationInformation#hasFindOneMethod()
		 */
		@Override
		public boolean hasFindOneMethod() {
			return delegate.hasFindOneMethod();
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.data.repository.support.RepositoryInvocationInformation#hasSaveMethod()
		 */
		@Override
		public boolean hasSaveMethod() {
			return delegate.hasSaveMethod();
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.data.repository.support.RepositoryInvoker#invokeDelete(java.io.Serializable)
		 */
		@Override
		public void invokeDelete(Serializable id) {
			delegate.invokeDelete(id);
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.data.repository.support.RepositoryInvoker#invokeFindAll(org.springframework.data.domain.Pageable)
		 */
		@Override
		public Iterable<Object> invokeFindAll(Pageable pageable) {
			return delegate.invokeFindAll(pageable);
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.data.repository.support.RepositoryInvoker#invokeFindAll(org.springframework.data.domain.Sort)
		 */
		@Override
		public Iterable<Object> invokeFindAll(Sort sort) {
			return delegate.invokeFindAll(sort);
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.data.repository.support.RepositoryInvoker#invokeSave(java.lang.Object)
		 */
		@Override
		public <T> T invokeSave(T object) {
			return delegate.invokeSave(object);
		}

		/**
		 * Invokes the configured converters for the given result.
		 *
		 * @param result can be {@literal null}.
		 * @return Object
		 */
		private Object postProcess(Object result) {

			for (Converter<Object, Object> converter : converters) {
				result = converter.convert(result);
			}

			return result;
		}
	}
}
