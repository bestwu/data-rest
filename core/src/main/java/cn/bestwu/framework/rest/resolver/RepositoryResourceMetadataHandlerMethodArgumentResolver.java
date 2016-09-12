/*
 * Copyright 2012-2014 the original author or authors.
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

import cn.bestwu.framework.rest.mapping.RepositoryResourceMappings;
import cn.bestwu.framework.rest.mapping.VersionRepositoryRestRequestMappingHandlerMapping;
import cn.bestwu.framework.rest.support.RepositoryResourceMetadata;
import org.springframework.core.MethodParameter;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.util.ClassUtils.isAssignable;

/**
 * {@link HandlerMethodArgumentResolver} to create {@link RepositoryResourceMetadata} instances.
 *
 * @author Peter Wu
 */
public class RepositoryResourceMetadataHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

	private final RepositoryResourceMappings repositoryResourceMappings;

	public RepositoryResourceMetadataHandlerMethodArgumentResolver(RepositoryResourceMappings repositoryResourceMappings) {
		this.repositoryResourceMappings = repositoryResourceMappings;
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return isAssignable(parameter.getParameterType(), RepositoryInformation.class);
	}

	@Override
	public RepositoryResourceMetadata resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		HttpServletRequest nativeRequest = webRequest.getNativeRequest(HttpServletRequest.class);
		String requestRepositoryResourceMetadataKey = VersionRepositoryRestRequestMappingHandlerMapping.REQUEST_REPOSITORY_RESOURCE_METADATA;
		String requestRepositoryBasePathNameKey = VersionRepositoryRestRequestMappingHandlerMapping.REQUEST_REPOSITORY_BASE_PATH_NAME;

		RepositoryResourceMetadata repositoryResourceMetadata = (RepositoryResourceMetadata) nativeRequest.getAttribute(requestRepositoryResourceMetadataKey);
		String repositoryBasePathName = (String) nativeRequest.getAttribute(requestRepositoryBasePathNameKey);
		if (repositoryResourceMetadata == null) {
			repositoryResourceMetadata = repositoryResourceMappings.getRepositoryResourceMetadata(repositoryBasePathName);
		}
		if (repositoryBasePathName != null && repositoryResourceMetadata == null) {
			throw new IllegalArgumentException(String.format("Could not resolve repository metadata for %s.", repositoryBasePathName));
		}
		nativeRequest.setAttribute(requestRepositoryResourceMetadataKey, repositoryResourceMetadata);
		return repositoryResourceMetadata;
	}
}
