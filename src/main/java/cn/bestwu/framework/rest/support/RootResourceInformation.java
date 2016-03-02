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
package cn.bestwu.framework.rest.support;

import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.repository.support.RepositoryInvoker;

/**
 * Meta-information about the root repository resource.
 *
 * @author Peter Wu
 */
public class RootResourceInformation {

	private final RepositoryResourceMetadata resourceMetadata;
	private final RepositoryInvoker invoker;

	public RootResourceInformation(RepositoryResourceMetadata metadata, RepositoryInvoker invoker) {

		this.resourceMetadata = metadata;

		if (resourceMetadata == null) {
			this.invoker = null;
		} else {
			this.invoker = invoker;
		}
	}

	public RepositoryInvoker getInvoker() {
		return invoker;
	}

	public RepositoryResourceMetadata getResourceMetadata() {
		return resourceMetadata;
	}

	public PersistentEntity<?, ?> getEntity() {
		return resourceMetadata.getEntity();
	}

	public Class<?> getModelType() {
		return resourceMetadata.getModelType();
	}

	public String getPathName() {
		return resourceMetadata.getPathName();
	}
}
