/*
 * Copyright 2012-2015 the original author or authors.
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

import cn.bestwu.framework.rest.support.RepositoryResourceMetadata;
import cn.bestwu.framework.rest.support.RootResourceInformation;
import org.springframework.core.MethodParameter;
import org.springframework.data.repository.support.RepositoryInvoker;
import org.springframework.data.repository.support.RepositoryInvokerFactory;
import org.springframework.util.Assert;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * {@link HandlerMethodArgumentResolver} to create {@link RootResourceInformation} for injection into Spring MVC
 * controller methods.
 *
 * @author Peter Wu
 */
public class RootResourceInformationHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

	private final RepositoryInvokerFactory invokerFactory;
	private final RepositoryResourceMetadataHandlerMethodArgumentResolver resourceMetadataResolver;

	public RootResourceInformationHandlerMethodArgumentResolver(
			RepositoryInvokerFactory invokerFactory, RepositoryResourceMetadataHandlerMethodArgumentResolver resourceMetadataResolver) {

		Assert.notNull(invokerFactory, "invokerFactory must not be null!");
		Assert.notNull(resourceMetadataResolver, "ResourceMetadataHandlerMethodArgumentResolver must not be null!");

		this.invokerFactory = invokerFactory;
		this.resourceMetadataResolver = resourceMetadataResolver;
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return RootResourceInformation.class.isAssignableFrom(parameter.getParameterType());
	}

	@Override
	public RootResourceInformation resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

		RepositoryResourceMetadata resourceMetadata = resourceMetadataResolver.resolveArgument(parameter, mavContainer, webRequest,
				binderFactory);

		Class<?> domainType = resourceMetadata.getModelType();
		RepositoryInvoker repositoryInvoker = invokerFactory.getInvokerFor(domainType);

		return new RootResourceInformation(resourceMetadata,
				postProcess(repositoryInvoker, domainType, webRequest));
	}

	protected RepositoryInvoker postProcess(RepositoryInvoker invoker, Class<?> domainType,
			NativeWebRequest webRequest) {
		return invoker;
	}
}
