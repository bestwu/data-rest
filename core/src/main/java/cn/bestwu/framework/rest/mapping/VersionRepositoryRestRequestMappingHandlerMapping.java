package cn.bestwu.framework.rest.mapping;

import cn.bestwu.framework.rest.annotation.RepositoryRestController;
import cn.bestwu.framework.rest.controller.BaseController;
import cn.bestwu.framework.rest.exception.ResourceNotFoundException;
import cn.bestwu.framework.rest.support.ProxyPathMapper;
import cn.bestwu.framework.rest.support.RepositoryResourceMetadata;
import cn.bestwu.framework.rest.support.ResourceType;
import cn.bestwu.framework.rest.support.Version;
import cn.bestwu.framework.util.ArrayUtil;
import cn.bestwu.framework.util.ResourceUtil;
import org.springframework.hateoas.core.AnnotationMappingDiscoverer;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.bestwu.framework.util.ResourceUtil.API_SIGNATURE;
import static cn.bestwu.framework.util.ResourceUtil.REQUEST_VERSION;
import static org.hibernate.search.util.AnalyzerUtils.log;

/**
 * 支持Rest @RequestMapping produces MediaType 参数 version的RequestMappingHandlerMapping
 *
 * @author Peter Wu
 */
public class VersionRepositoryRestRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

	/**
	 * repository_base_path_name request 属性参数
	 */
	public static final String REQUEST_REPOSITORY_BASE_PATH_NAME = "request_repository_base_path_name";
	/**
	 * repository_search_name request 属性参数
	 */
	public static final String REQUEST_REPOSITORY_SEARCH_NAME = "request_repository_search_name";
	/**
	 * repository_resource_metadata  request 属性参数
	 */
	public static final String REQUEST_REPOSITORY_RESOURCE_METADATA = "request_repository_resource_metadata";

	/**
	 * collection 类 regex
	 */
	public static final String COLLECTION_LOOKUP_PATH_REGEX = "^/[^/]+/?$";

	/**
	 * 忽略的lookupPath
	 */
	private final String[] IGNORED_LOOKUP_PATH = { "/oauth/authorize", "/oauth/token", "/oauth/check_token", "/oauth/confirm_access", "/oauth/error" };

	private final RepositoryResourceMappings repositoryResourceMappings;
	private final ProxyPathMapper proxyPathMapper;

	private static AnnotationMappingDiscoverer DISCOVERER = new AnnotationMappingDiscoverer(RequestMapping.class);

	public VersionRepositoryRestRequestMappingHandlerMapping(RepositoryResourceMappings repositoryResourceMappings,
			ProxyPathMapper proxyPathMapper) {
		Assert.notNull(repositoryResourceMappings);
		this.repositoryResourceMappings = repositoryResourceMappings;
		this.proxyPathMapper = proxyPathMapper;
	}

	/**
	 * 查找映射方法
	 *
	 * @param lookupPath lookupPath
	 * @param request    request
	 * @return HandlerMethod
	 * @throws Exception Exception
	 */
	@Override protected HandlerMethod lookupHandlerMethod(String lookupPath, HttpServletRequest request) throws Exception {

		if (proxyPathMapper != null) {
			lookupPath = proxyPathMapper.getProxyPath(lookupPath);
		}

		if (ArrayUtil.contains(IGNORED_LOOKUP_PATH, lookupPath)) {
			API_SIGNATURE.set(lookupPath);
			return null;
		}

		{
			String version = null;
			Enumeration<String> accept = request.getHeaders("Accept");
			while (accept.hasMoreElements()) {
				String element = accept.nextElement();
				String[] split = element.split(",");
				for (String s : split) {
					version = MediaType.valueOf(s).getParameter(Version.VERSION_PARAM_NAME);
					if (StringUtils.hasText(version)) {
						break;
					}
				}
			}
			if (!StringUtils.hasText(version))
				version = request.getParameter("_" + Version.VERSION_PARAM_NAME);
			if (!StringUtils.hasText(version))
				version = Version.DEFAULT_VERSION;

			REQUEST_VERSION.set(version);
		}

		HandlerMethod handlerMethod = super.lookupHandlerMethod(lookupPath, request);

		if (handlerMethod == null) {
			return null;
		}

		{
			String apiSignature = request.getMethod().toLowerCase() + DISCOVERER.getMapping(handlerMethod.getMethod());

			String repositoryBasePathName = (String) request.getAttribute(VersionRepositoryRestRequestMappingHandlerMapping.REQUEST_REPOSITORY_BASE_PATH_NAME);
			if (repositoryBasePathName != null) {
				apiSignature = apiSignature.replace(BaseController.BASE_NAME, repositoryBasePathName);
			}
			String searchName = (String) request.getAttribute(VersionRepositoryRestRequestMappingHandlerMapping.REQUEST_REPOSITORY_SEARCH_NAME);
			if (searchName != null) {
				apiSignature = apiSignature.replace("{search}", searchName);
			}

			apiSignature = apiSignature.replaceAll("[{}]", "").replace("/", "_");
			if (log.isDebugEnabled()) {
				log.debug("请求签名：" + apiSignature);
			}

			API_SIGNATURE.set(apiSignature);
		}

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

					repositoryResourceMetadata.verifySupportedMethod(HttpMethod.valueOf(request.getMethod()), resourceType);

					return handlerMethod;
				} else
					throw new ResourceNotFoundException(lookupPath);
			} else
				return null;
		}
	}

	/**
	 * @param lookupPath lookupPath
	 * @return BasePathName
	 */
	public static String getBasePathName(String lookupPath) {
		int secondSlashIndex = lookupPath.indexOf('/', lookupPath.startsWith("/") ? 1 : 0);
		boolean noSecondSlashIndex = secondSlashIndex == -1;
		return noSecondSlashIndex ? lookupPath.substring(1) : lookupPath.substring(1, secondSlashIndex);
	}

	/**
	 * 处理无HandlerMethod时
	 *
	 * @param requestMappingInfos requestMappingInfos
	 * @param lookupPath          lookupPath
	 * @param request             request
	 * @return null
	 * @throws ServletException ServletException
	 */
	@Override protected HandlerMethod handleNoMatch(Set<RequestMappingInfo> requestMappingInfos, String lookupPath, HttpServletRequest request) throws ServletException {
		return null;
	}

	/**
	 * @param request request
	 * @return Comparator
	 */
	@Override protected Comparator<RequestMappingInfo> getMappingComparator(HttpServletRequest request) {
		return (info1, info2) -> {
			int result = versionCompareTo(getVersions(info1.getProducesCondition().getProducibleMediaTypes()), getVersions(info2.getProducesCondition().getProducibleMediaTypes()), request);//版本比较
			if (result != 0) {
				return result;
			}
			return info1.compareTo(info2, request);
		};
	}

	/**
	 * @param mediaTypes request请求mediaTypes
	 * @return 版本号
	 */
	private List<String> getVersions(Set<MediaType> mediaTypes) {
		return mediaTypes.stream().map(mediaType -> mediaType.getParameter(Version.VERSION_PARAM_NAME)).filter(StringUtils::hasText).collect(Collectors.toList());
	}

	/**
	 * 比较版本号
	 *
	 * @param me      me
	 * @param other   other
	 * @param request request
	 * @return int
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

		String acceptedVersion = ResourceUtil.REQUEST_VERSION.get();
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

	/**
	 * 比较MediaTypes对应版本
	 *
	 * @param versions1 versions1
	 * @param index1    index1
	 * @param versions2 versions2
	 * @param index2    index2
	 * @return int
	 */
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

	/**
	 * 版本相等
	 *
	 * @param versions        versions
	 * @param acceptedVersion acceptedVersion
	 * @return int
	 */
	private int equalVersion(List<String> versions, String acceptedVersion) {
		for (int i = 0; i < versions.size(); i++) {
			String currentVersion = versions.get(i);
			if (Version.equals(acceptedVersion, currentVersion)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 版本包含
	 *
	 * @param versions        versions
	 * @param acceptedVersion acceptedVersion
	 * @return int
	 */
	private int includedVersion(List<String> versions, String acceptedVersion) {
		for (int i = 0; i < versions.size(); i++) {
			String currentVersion = versions.get(i);
			if (Version.included(acceptedVersion, currentVersion)) {
				return i;
			}
		}
		return -1;
	}

}
