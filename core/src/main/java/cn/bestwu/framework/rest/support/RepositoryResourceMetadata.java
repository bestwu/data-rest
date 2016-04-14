package cn.bestwu.framework.rest.support;

import cn.bestwu.framework.data.annotation.EnableAllDataInOnePage;
import cn.bestwu.framework.data.annotation.RepositoryRestResource;
import cn.bestwu.framework.data.annotation.SearchResource;
import cn.bestwu.framework.rest.exception.ResourceNotFoundException;
import cn.bestwu.framework.util.ArrayUtil;
import cn.bestwu.framework.util.ResourceUtil;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.repository.core.CrudMethods;
import org.springframework.http.HttpMethod;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

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
	private String pathName;

	private PersistentEntity<?, ?> entity;

	private Map<ResourceType, Set<HttpMethod>> supportedHttpMethods;

	private Map<String, Method> searchMethods;

	private boolean enableAllDataInOnePage;
	private boolean contained = false;
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
			exported = annotation.exported();
			contained = annotation.contained();
		} else {
			exported = false;
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

			supportedHttpMethods.put(ResourceType.COLLECTION, Collections.unmodifiableSet(collectionMethods));

			//ResourceType.ITEM
			Set<HttpMethod> itemMethods = new HashSet<>();

			if (exposesMethod(findOneMethod, HttpMethod.GET)) {
				itemMethods.add(HttpMethod.HEAD);
				itemMethods.add(HttpMethod.GET);
			}
			if (exposesMethod(saveMethod, HttpMethod.PUT)) {
				itemMethods.add(HttpMethod.PUT);
			}
			if (exposesMethod(deleteMethod, HttpMethod.DELETE)) {
				itemMethods.add(HttpMethod.DELETE);
			}

			supportedHttpMethods.put(ResourceType.ITEM, Collections.unmodifiableSet(itemMethods));

			this.supportedHttpMethods = Collections.unmodifiableMap(supportedHttpMethods);
		}
	}

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
			throw new HttpRequestMethodNotSupportedException(requestHttpMethod.name(), supportedHttpMethods.stream().map(Enum::name).collect(Collectors.toSet()));
		}
	}

	public Set<HttpMethod> getSupportedHttpMethods(ResourceType resourceType) {
		return supportedHttpMethods.get(resourceType);
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
