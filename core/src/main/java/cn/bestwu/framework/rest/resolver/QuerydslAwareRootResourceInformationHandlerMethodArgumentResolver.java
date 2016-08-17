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
package cn.bestwu.framework.rest.resolver;

import cn.bestwu.framework.event.DefaultPredicateEvent;
import cn.bestwu.framework.rest.mapping.VersionRepositoryRestRequestMappingHandlerMapping;
import cn.bestwu.framework.rest.support.Resource;
import cn.bestwu.framework.rest.support.RootResourceInformation;
import com.querydsl.core.types.Predicate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.QuerydslRepositoryInvokerAdapter;
import org.springframework.data.querydsl.binding.FixQuerydslPredicateBuilder;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.repository.support.RepositoryInvoker;
import org.springframework.data.repository.support.RepositoryInvokerFactory;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;

/**
 * {@link HandlerMethodArgumentResolver} to create {@link RootResourceInformation} for injection into Spring MVC
 * controller methods.
 *
 * @author Peter Wu
 */
public class QuerydslAwareRootResourceInformationHandlerMethodArgumentResolver
		extends RootResourceInformationHandlerMethodArgumentResolver {

	private final Repositories repositories;
	private final FixQuerydslPredicateBuilder predicateBuilder;
	private final QuerydslBindingsFactory factory;
	private final ApplicationEventPublisher publisher;

	public QuerydslAwareRootResourceInformationHandlerMethodArgumentResolver(Repositories repositories,
			RepositoryInvokerFactory invokerFactory, RepositoryResourceMetadataHandlerMethodArgumentResolver resourceMetadataResolver,
			FixQuerydslPredicateBuilder predicateBuilder, QuerydslBindingsFactory factory, ApplicationEventPublisher publisher) {

		super(invokerFactory, resourceMetadataResolver);

		this.repositories = repositories;
		this.predicateBuilder = predicateBuilder;
		this.factory = factory;
		this.publisher = publisher;
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	protected RepositoryInvoker postProcess(RepositoryInvoker invoker, Class<?> modelType,
			NativeWebRequest webRequest) {

		HttpServletRequest nativeRequest = webRequest.getNativeRequest(HttpServletRequest.class);
		if (!"GET".equals(nativeRequest.getMethod()) || !nativeRequest.getServletPath().matches(VersionRepositoryRestRequestMappingHandlerMapping.COLLECTION_LOOKUP_PATH_REGEX)) {
			return invoker;
		}

		Object repository = repositories.getRepositoryFor(modelType);

		if (!QueryDslPredicateExecutor.class.isInstance(repository)) {
			return invoker;
		}

		ClassTypeInformation<?> type = ClassTypeInformation.from(modelType);

		QuerydslBindings bindings = factory.createBindingsFor(null, type);
		MultiValueMap<String, String> parameters = toMultiValueMap(webRequest.getParameterMap());
		Predicate predicate = predicateBuilder.getPredicate(type, parameters, bindings);
		{//设置默认条件
			Resource<Predicate> predicateResource = new Resource<>(predicate);
			publisher.publishEvent(new DefaultPredicateEvent(predicateResource, modelType));
			predicate = predicateResource.getContent();
		}

		return new QuerydslRepositoryInvokerAdapter(invoker, (QueryDslPredicateExecutor<Object>) repository, predicate);
	}

	/**
	 * Converts the given Map into a {@link MultiValueMap}.
	 *
	 * @param source must not be {@literal null}.
	 * @return MultiValueMap
	 */
	private static MultiValueMap<String, String> toMultiValueMap(Map<String, String[]> source) {

		MultiValueMap<String, String> result = new LinkedMultiValueMap<>();

		for (String key : source.keySet()) {
			result.put(key, Arrays.asList(source.get(key)));
		}

		return result;
	}
}
