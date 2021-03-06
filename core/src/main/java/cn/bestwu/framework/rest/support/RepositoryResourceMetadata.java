package cn.bestwu.framework.rest.support;

import cn.bestwu.framework.data.annotation.DisableSelfRel;
import cn.bestwu.framework.data.annotation.EnableAllDataInOnePage;
import cn.bestwu.framework.data.annotation.RepositoryRestResource;
import cn.bestwu.framework.data.annotation.SearchResource;
import cn.bestwu.framework.rest.exception.ResourceNotFoundException;
import cn.bestwu.framework.util.ResourceUtil;
import cn.bestwu.lang.util.ArrayUtil;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.repository.core.CrudMethods;
import org.springframework.http.HttpMethod;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import java.lang.reflect.Method;
import java.util.*;

/**
 * 资源元数据
 *
 * @author Peter Wu
 */
public class RepositoryResourceMetadata {

	/**
	 * this resource is exported
	 */
	private boolean exported = true;
	private String pathName;

	private PersistentEntity<?, ?> entity;

	private Map<ResourceType, Set<HttpMethod>> supportedHttpMethods;

	private Map<String, Method> searchMethods;

	private boolean enableAllDataInOnePage;
	private boolean contained = false;
	//--------------------------------------------

	/**
	 * 创建资源元数据
	 *
	 * @param entity              持久实体
	 * @param repositoryInterface 仓库接口
	 * @param crudMethods         增删改查方法
	 */
	public RepositoryResourceMetadata(PersistentEntity<?, ?> entity, Class<?> repositoryInterface, CrudMethods crudMethods) {
		this.entity = entity;
		this.enableAllDataInOnePage = repositoryInterface.isAnnotationPresent(EnableAllDataInOnePage.class);
		//SearchResource
		Method[] methods = repositoryInterface.getMethods();
		for (Method method : methods) {
			SearchResource searchResource = method.getAnnotation(SearchResource.class);
			if (searchResource != null) {
				String searchResourceName = searchResource.value();
				if (!StringUtils.hasText(searchResourceName)) {
					searchResourceName = StringUtils.uncapitalize(method.getName().replace("find", "").replace("findBy", "").replace("query", "").replace("queryBy", ""));
				}
				if (searchMethods == null) {
					searchMethods = new HashMap<>();
				}
				searchMethods.put(searchResourceName, method);
			}
		}

		//RepositoryRestResource
		RepositoryRestResource annotation = repositoryInterface.getAnnotation(RepositoryRestResource.class);
		if (annotation != null) {
			exported = annotation.exported();
		} else {
			contained = true;
		}

		pathName = ResourceUtil.getRepositoryBasePathName(entity.getType());

		if (exported) {
			Map<ResourceType, Set<HttpMethod>> supportedHttpMethods = new HashMap<>();

			Method findAllMethod = crudMethods.getFindAllMethod();
			Method findOneMethod = crudMethods.getFindOneMethod();
			Method saveMethod = crudMethods.getSaveMethod();
			Method deleteMethod = crudMethods.getDeleteMethod();

			//ResourceType.COLLECTION
			Set<HttpMethod> collectionMethods = new HashSet<>();

			if (exposesMethod(findAllMethod, HttpMethod.GET)) {
				collectionMethods.add(HttpMethod.HEAD);
				collectionMethods.add(HttpMethod.GET);
			}
			if (exposesMethod(saveMethod, HttpMethod.POST)) {
				collectionMethods.add(HttpMethod.POST);
			}
			if (exposesMethod(deleteMethod, HttpMethod.DELETE)) {
				collectionMethods.add(HttpMethod.DELETE);
			}

			if (!collectionMethods.isEmpty()) {
				supportedHttpMethods.put(ResourceType.COLLECTION, Collections.unmodifiableSet(collectionMethods));
			}

			//ResourceType.ITEM
			Set<HttpMethod> itemMethods = new HashSet<>();

			if (exposesMethod(findOneMethod, HttpMethod.GET) && !entity.getType().isAnnotationPresent(DisableSelfRel.class)) {
				itemMethods.add(HttpMethod.HEAD);
				itemMethods.add(HttpMethod.GET);
			}
			if (exposesMethod(saveMethod, HttpMethod.PUT)) {
				itemMethods.add(HttpMethod.PUT);
			}
			if (exposesMethod(deleteMethod, HttpMethod.DELETE)) {
				itemMethods.add(HttpMethod.DELETE);
			}

			if (!itemMethods.isEmpty()) {
				supportedHttpMethods.put(ResourceType.ITEM, Collections.unmodifiableSet(itemMethods));
			}

			this.supportedHttpMethods = Collections.unmodifiableMap(supportedHttpMethods);

			if (supportedHttpMethods.isEmpty()) {
				this.exported = false;
			}
		}
	}

	/**
	 * @param method     仓库方法
	 * @param httpMethod 请求方法
	 * @return 是否支持请求访求
	 */
	private boolean exposesMethod(Method method, HttpMethod httpMethod) {
		if (method == null) {
			return false;
		}
		RepositoryRestResource annotation = method.getAnnotation(RepositoryRestResource.class);
		if (annotation == null) {
			return !contained;
		}
		if (!annotation.exported()) {
			return false;
		}
		HttpMethod[] array = annotation.value();
		if (array.length == 0) {
			return true;
		}
		return ArrayUtil.contains(array, httpMethod);
	}

	//--------------------------------------------

	public PersistentEntity<?, ?> getEntity() {
		return entity;
	}

	/**
	 * @return 持久化实体是否展开到控制层
	 */
	public boolean isExported() {
		return exported;
	}

	public String getPathName() {
		return pathName;
	}

	//--------------------------------------------
	public Class<?> getModelType() {
		return entity.getType();
	}

	/**
	 * 是否支持请求方法
	 *
	 * @param requestHttpMethod 请求方法
	 * @param resourceType      资源类型
	 * @throws HttpRequestMethodNotSupportedException 如果不支持抛出此异常
	 */
	public void verifySupportedMethod(HttpMethod requestHttpMethod, ResourceType resourceType) throws HttpRequestMethodNotSupportedException {
		if (HttpMethod.OPTIONS.equals(requestHttpMethod)) {
			return;
		}
		if (resourceType == null) {
			throw new ResourceNotFoundException();
		}
		Set<HttpMethod> supportedHttpMethods = this.supportedHttpMethods.get(resourceType);
		if (supportedHttpMethods == null || supportedHttpMethods.isEmpty()) {
			throw new ResourceNotFoundException();
		}
		if (!supportedHttpMethods.contains(requestHttpMethod)) {
			Set<String> methods = new HashSet<>();
			for (HttpMethod method : supportedHttpMethods) {
				methods.add(method.name());
			}
			throw new HttpRequestMethodNotSupportedException(requestHttpMethod.name(), methods);
		}
	}

	/**
	 * @param resourceType 资源类型
	 * @return 支持的请求方法
	 */
	public Set<HttpMethod> getSupportedHttpMethods(ResourceType resourceType) {
		return supportedHttpMethods.get(resourceType);
	}

	/**
	 * @return 是否支持全部结果一页响应
	 */
	public boolean enableAllDataInOnePage() {
		return enableAllDataInOnePage;
	}

	/**
	 * @param search search名
	 * @return 对应方法
	 */
	public Method getSearchMethod(String search) {
		if (CollectionUtils.isEmpty(searchMethods)) {
			return null;
		}
		return searchMethods.get(search);
	}
}
