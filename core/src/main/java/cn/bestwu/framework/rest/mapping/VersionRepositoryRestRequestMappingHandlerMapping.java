package cn.bestwu.framework.rest.mapping;

import cn.bestwu.framework.rest.annotation.RepositoryRestController;
import cn.bestwu.framework.rest.exception.ResourceNotFoundException;
import cn.bestwu.framework.rest.support.*;
import cn.bestwu.framework.util.ArrayUtil;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 支持Rest @RequestMapping produces MediaType 参数 version的RequestMappingHandlerMapping
 *
 * @author Peter Wu
 */
public class VersionRepositoryRestRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

	public static final String REQUEST_HANDLER_METHOD = "request_handler_method";
	public static final String REQUEST_REPOSITORY_BASE_PATH_NAME = "request_repository_base_path_name";
	public static final String REQUEST_REPOSITORY_SEARCH_NAME = "request_repository_search_name";
	public static final String REQUEST_REPOSITORY_RESOURCE_METADATA = "request_repository_resource_metadata";

	public static final String COLLECTION_LOOKUP_PATH_REGEX = "^/[^/]+/?$";

	private final String[] IGNORED_LOOKUP_PATH = { "/oauth/authorize", "/oauth/token", "/oauth/check_token", "/oauth/confirm_access", "/oauth/error" };

	private final RepositoryResourceMappings repositoryResourceMappings;
	private final ProxyPathMapper proxyPathMapper;

	public VersionRepositoryRestRequestMappingHandlerMapping(RepositoryResourceMappings repositoryResourceMappings,
			ProxyPathMapper proxyPathMapper) {
		Assert.notNull(repositoryResourceMappings);
		this.repositoryResourceMappings = repositoryResourceMappings;
		this.proxyPathMapper = proxyPathMapper;
	}

	/*
	 * 查找映射方法
	 */
	@Override protected HandlerMethod lookupHandlerMethod(String lookupPath, HttpServletRequest request) throws Exception {

		if (proxyPathMapper != null) {
			lookupPath = proxyPathMapper.getProxyPath(lookupPath);
		}

		if (ArrayUtil.contains(IGNORED_LOOKUP_PATH, lookupPath)) {
			return null;
		}

		HandlerMethod handlerMethod = super.lookupHandlerMethod(lookupPath, request);

		if (handlerMethod == null) {
			return null;
		}

		request.setAttribute(REQUEST_HANDLER_METHOD, handlerMethod);

		//不是data-rest controller
		Class<?> beanType = handlerMethod.getBeanType();
		if (!beanType.isAnnotationPresent(RepositoryRestController.class)) {
			return handlerMethod;
		}

		//是data-rest controller
		{
			String basePathName = getBasePathName(lookupPath);

			//ROOT 请求
			if (!StringUtils.hasText(basePathName)) {
				return handlerMethod;
			}

			request.setAttribute(REQUEST_REPOSITORY_BASE_PATH_NAME, basePathName);

			RepositoryResourceMetadata repositoryResourceMetadata = repositoryResourceMappings.getRepositoryResourceMetadata(basePathName);
			request.setAttribute(REQUEST_REPOSITORY_RESOURCE_METADATA, repositoryResourceMetadata);
			if (repositoryResourceMetadata != null) {
				String SEARCH_LOOKUP_PATH_REGEX = "^/[^/]+/search(/[^/]+)?/?$";
				if (lookupPath.matches(SEARCH_LOOKUP_PATH_REGEX)) {//search 请求
					return handlerMethod;
				}
				if (repositoryResourceMetadata.isExported()) {
					ResourceType resourceType = null;
					String ITEM_LOOKUP_PATH_REGEX = "^/[^/]+/[^/]+/?$";
					if (lookupPath.matches(ITEM_LOOKUP_PATH_REGEX)) {
						resourceType = ResourceType.ITEM;
					} else if (lookupPath.matches(COLLECTION_LOOKUP_PATH_REGEX)) {
						resourceType = ResourceType.COLLECTION;
					}

					repositoryResourceMetadata.verifySupportedMethod(request.getMethod(), resourceType);

					return handlerMethod;
				} else
					throw new ResourceNotFoundException(lookupPath);
			} else
				return null;
		}
	}

	public static String getBasePathName(String lookupPath) {
		int secondSlashIndex = lookupPath.indexOf('/', lookupPath.startsWith("/") ? 1 : 0);
		boolean noSecondSlashIndex = secondSlashIndex == -1;
		return noSecondSlashIndex ? lookupPath.substring(1) : lookupPath.substring(1, secondSlashIndex);
	}

	@Override protected HandlerMethod handleNoMatch(Set<RequestMappingInfo> requestMappingInfos, String lookupPath, HttpServletRequest request) throws ServletException {
		return null;
	}

	@Override protected Comparator<RequestMappingInfo> getMappingComparator(HttpServletRequest request) {
		return (info1, info2) -> compareTo(info1, info2, request);
	}

	/*
	 * MappingComparator
	 */
	private int compareTo(RequestMappingInfo me, RequestMappingInfo other, HttpServletRequest request) {
		int result = me.getPatternsCondition().compareTo(other.getPatternsCondition(), request);
		if (result != 0) {
			return result;
		}
		result = me.getParamsCondition().compareTo(other.getParamsCondition(), request);
		if (result != 0) {
			return result;
		}
		result = me.getHeadersCondition().compareTo(other.getHeadersCondition(), request);
		if (result != 0) {
			return result;
		}
		result = me.getConsumesCondition().compareTo(other.getConsumesCondition(), request);
		if (result != 0) {
			return result;
		}
		result = versionCompareTo(getVersions(me.getProducesCondition().getProducibleMediaTypes()), getVersions(other.getProducesCondition().getProducibleMediaTypes()), request);//版本比较
		if (result != 0) {
			return result;
		}
		result = me.getProducesCondition().compareTo(other.getProducesCondition(), request);
		if (result != 0) {
			return result;
		}
		result = me.getMethodsCondition().compareTo(other.getMethodsCondition(), request);
		if (result != 0) {
			return result;
		}
		result = customConditionCompareTo(me.getCustomCondition(), other.getCustomCondition(), request);
		if (result != 0) {
			return result;
		}
		return 0;
	}

	private List<String> getVersions(Set<MediaType> mediaTypes) {
		return mediaTypes.stream().map(mediaType -> mediaType.getParameter(Version.VERSION_PARAM_NAME)).filter(StringUtils::hasText).collect(Collectors.toList());
	}

	/*
	 * ProducesCondition CompareTo
	 */
	private int versionCompareTo(List<String> me, List<String> other, HttpServletRequest request) {
		if (me.isEmpty()) {
			me.add(Version.DEFAULT_VERSION);
		}
		if (other.isEmpty()) {
			other.add(Version.DEFAULT_VERSION);
		}
		Comparator<String> comparator = Version::compareVersion;
		me.sort(comparator);
		other.sort(comparator);

		String acceptedVersion = ResourceUtil.getRequestVersion(request);
		int thisIndex, otherIndex, result;

		thisIndex = equalVersion(me, acceptedVersion);
		otherIndex = equalVersion(other, acceptedVersion);
		result = compareMatchingMediaTypes(me, thisIndex, other, otherIndex);
		if (result != 0) {
			return result;
		}

		thisIndex = includedVersion(me, acceptedVersion);
		otherIndex = includedVersion(other, acceptedVersion);
		result = compareMatchingMediaTypes(me, thisIndex, other, otherIndex);
		if (result != 0) {
			return result;
		}
		return 0;
	}

	private int compareMatchingMediaTypes(List<String> versions1, int index1, List<String> versions2, int index2) {

		int result = 0;
		if (index1 != index2) {
			result = index2 - index1;
		} else if (index1 != -1) {
			String version1 = versions1.get(index1);
			String version2 = versions2.get(index2);
			result = Version.compareVersion(version1, version2);
			result = (result != 0) ? result : version1.compareTo(version2);
		}
		return result;
	}

	private int equalVersion(List<String> versions, String accepteVersion) {
		for (int i = 0; i < versions.size(); i++) {
			String currentVersion = versions.get(i);
			if (accepteVersion.equals(currentVersion)) {
				return i;
			}
		}
		return -1;
	}

	private int includedVersion(List<String> versions, String acceptedVersion) {
		for (int i = 0; i < versions.size(); i++) {
			String currentVersion = versions.get(i);
			if (Version.included(acceptedVersion, currentVersion)) {
				return i;
			}
		}
		return -1;
	}

	//------------------------------------------------------------------------------

	/*
	 * CustomCondition CompareTo
	 */
	@SuppressWarnings("unchecked")
	private int customConditionCompareTo(RequestCondition me, RequestCondition other, HttpServletRequest request) {
		if (me == null && other == null) {
			return 0;
		} else if (me == null) {
			return 1;
		} else if (other == null) {
			return -1;
		} else {
			Class<?> clazz = me.getClass();
			Class<?> otherClazz = other.getClass();
			if (!clazz.equals(otherClazz)) {
				throw new ClassCastException("Incompatible request conditions: " + clazz + " and " + otherClazz);
			}
			return me.compareTo(other, request);
		}
	}

}
