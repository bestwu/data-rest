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

	/**
	 * 根据 repositories 填充映射缓存
	 *
	 * @param repositories repositories
	 */
	private void populateCache(Repositories repositories) {
		for (Class<?> entityClass : repositories) {
			org.springframework.data.repository.core.RepositoryInformation repositoryInformation = repositories.getRepositoryInformationFor(entityClass);
			Class<?> repositoryInterface = repositoryInformation.getRepositoryInterface();
			PersistentEntity<?, ?> entity = repositories.getPersistentEntity(entityClass);

			RepositoryResourceMetadata repositoryResourceMetadata = new RepositoryResourceMetadata(entity, repositoryInterface, repositoryInformation.getCrudMethods());
			String pathName = repositoryResourceMetadata.getPathName();
			if (!hasMetadataFor(pathName)) {
				cache.put(pathName, repositoryResourceMetadata);
			}
		}
	}

	/**
	 * @param basePathName 请求basePathName
	 * @return 资源元信息
	 */
	public RepositoryResourceMetadata getRepositoryResourceMetadata(String basePathName) {
		return cache.get(basePathName);
	}

	/**
	 * @param basePathName 请求basePathName
	 * @return 是否包含请求的资源元信息
	 */
	protected final boolean hasMetadataFor(String basePathName) {
		return cache.containsKey(basePathName);
	}
}
