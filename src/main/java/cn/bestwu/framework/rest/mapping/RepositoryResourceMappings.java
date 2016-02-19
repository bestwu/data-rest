package cn.bestwu.framework.rest.mapping;

import cn.bestwu.framework.rest.support.RepositoryResourceMetadata;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.repository.support.Repositories;

import java.util.HashMap;
import java.util.Map;

/**
 * RepositoryResource 映射类
 *
 * @author Peter Wu
 */
public class RepositoryResourceMappings {

	/**
	 * key: basePathName
	 */
	private final Map<String, RepositoryResourceMetadata> cache = new HashMap<>();

	public RepositoryResourceMappings(Repositories repositories) {
		this.populateCache(repositories);
	}

	private void populateCache(Repositories repositories) {
		repositories.forEach(entityClass -> {
			org.springframework.data.repository.core.RepositoryInformation repositoryInformation = repositories.getRepositoryInformationFor(entityClass);
			Class<?> repositoryInterface = repositoryInformation.getRepositoryInterface();
			PersistentEntity<?, ?> entity = repositories.getPersistentEntity(entityClass);

			RepositoryResourceMetadata repositoryResourceMetadata = new RepositoryResourceMetadata(entity, repositoryInterface, repositoryInformation.getCrudMethods());
			String pathName = repositoryResourceMetadata.getPathName();
			if (!hasMetadataFor(pathName)) {
				cache.put(pathName, repositoryResourceMetadata);
			}
		});
	}

	public RepositoryResourceMetadata getRepositoryResourceMetadata(String basePathName) {
		return cache.get(basePathName);
	}

	protected final boolean hasMetadataFor(String basePathName) {
		return cache.containsKey(basePathName);
	}
}
