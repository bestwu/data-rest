package cn.bestwu.framework.rest.support;

import cn.bestwu.framework.data.annotation.EnableAllDataInOnePage;
import cn.bestwu.framework.data.annotation.RepositoryRestResource;
import cn.bestwu.framework.data.annotation.SearchResource;
import cn.bestwu.framework.data.annotation.SupportedHttpMethods;
import cn.bestwu.framework.rest.exception.ResourceNotFoundException;
import cn.bestwu.framework.util.ArrayUtil;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.repository.core.CrudMethods;
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
	private boolean exported;
	/**
	 * 数据模型 对应的 {repository}，默认
	 */
	private String pathName;

	private PersistentEntity<?, ?> entity;

	private Map<ResourceType, Set<String>> supportedHttpMethods;

	private Map<String, Method> searchMethods;

	private boolean enableAllDataInOnePage;
	//--------------------------------------------

	public RepositoryResourceMetadata(PersistentEntity<?, ?> entity, Class<?> repositoryInterface, CrudMethods crudMethods) {
		this.entity = entity;
		this.enableAllDataInOnePage = repositoryInterface.isAnnotationPresent(EnableAllDataInOnePage.class);
		//SearchResource
		Method[] methods = repositoryInterface.getMethods();
		Arrays.stream(methods).forEach(method -> {
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
		});

		//RepositoryRestResource
		RepositoryRestResource annotation = repositoryInterface.getAnnotation(RepositoryRestResource.class);
		if (annotation != null) {
			exported = annotation.value();
			pathName = annotation.pathName();
			if ("".equals(pathName)) {
				pathName = ResourceUtil.getRepositoryBasePathName(entity.getType());
			}
		} else {
			exported = true;
			pathName = ResourceUtil.getRepositoryBasePathName(entity.getType());
		}

		if (exported) {
			Map<ResourceType, Set<String>> supportedHttpMethods = new HashMap<>();

			Method findAllMethod = crudMethods.getFindAllMethod();
			Method findOneMethod = crudMethods.getFindOneMethod();
			Method saveMethod = crudMethods.getSaveMethod();
			Method deleteMethod = crudMethods.getDeleteMethod();

			//ResourceType.COLLECTION
			Set<String> collectionMethods = new HashSet<>();

			if (exposesMethod(findAllMethod, SupportedHttpMethods.GET)) {
				collectionMethods.add(SupportedHttpMethods.GET);
			}
			if (exposesMethod(saveMethod, SupportedHttpMethods.POST)) {
				collectionMethods.add(SupportedHttpMethods.POST);
			}
			if (exposesMethod(deleteMethod, SupportedHttpMethods.DELETE)) {
				collectionMethods.add(SupportedHttpMethods.DELETE);
			}

			supportedHttpMethods.put(ResourceType.COLLECTION, Collections.unmodifiableSet(collectionMethods));

			//ResourceType.ITEM
			Set<String> itemMethods = new HashSet<>();

			if (exposesMethod(findOneMethod, SupportedHttpMethods.GET)) {
				itemMethods.add(SupportedHttpMethods.GET);
			}
			if (exposesMethod(saveMethod, SupportedHttpMethods.PUT)) {
				itemMethods.add(SupportedHttpMethods.PUT);
			}
			if (exposesMethod(deleteMethod, SupportedHttpMethods.DELETE)) {
				itemMethods.add(SupportedHttpMethods.DELETE);
			}

			supportedHttpMethods.put(ResourceType.ITEM, Collections.unmodifiableSet(itemMethods));

			this.supportedHttpMethods = Collections.unmodifiableMap(supportedHttpMethods);
		}
	}

	private boolean exposesMethod(Method method, String httpMethod) {
		if (method == null) {
			return false;
		}
		RepositoryRestResource restResource = method.getAnnotation(RepositoryRestResource.class);
		if (restResource != null && !restResource.value()) {
			return false;
		}
		SupportedHttpMethods supportedHttpMethods = method.getAnnotation(SupportedHttpMethods.class);
		return !(supportedHttpMethods != null && !ArrayUtil.contains(supportedHttpMethods.value(), httpMethod));
	}

	//--------------------------------------------

	public PersistentEntity<?, ?> getEntity() {
		return entity;
	}

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

	public void verifySupportedMethod(String requestHttpMethod, ResourceType resourceType) throws HttpRequestMethodNotSupportedException {
		if (resourceType == null) {
			throw new ResourceNotFoundException();
		}
		Set<String> supportedHttpMethods = this.supportedHttpMethods.get(resourceType);
		if (supportedHttpMethods == null || supportedHttpMethods.isEmpty()) {
			throw new ResourceNotFoundException();
		}
		if (!supportedHttpMethods.contains(requestHttpMethod)) {
			throw new HttpRequestMethodNotSupportedException(requestHttpMethod, supportedHttpMethods);
		}
	}

	public boolean enableAllDataInOnePage() {
		return enableAllDataInOnePage;
	}

	public Method getSearchMethod(String search) {
		if (CollectionUtils.isEmpty(searchMethods)) {
			return null;
		}
		return searchMethods.get(search);
	}
}
