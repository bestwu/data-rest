package cn.bestwu.framework.rest.mapping;

import cn.bestwu.framework.rest.annotation.RepositoryRestController;
import cn.bestwu.framework.rest.exception.ResourceNotFoundException;
import cn.bestwu.framework.rest.support.ProxyPathMapper;
import cn.bestwu.framework.rest.support.RepositoryResourceMetadata;
import cn.bestwu.framework.rest.support.ResourceType;
import cn.bestwu.framework.rest.support.Version;
import cn.bestwu.framework.util.ArrayUtil;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.MimeType;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.ProducesRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

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

	public final MediaType DEFAULT_MEDIA_TYPE = MediaType.parseMediaType("*/*");

	private final ContentNegotiationManager contentNegotiationManager;
	private final RepositoryResourceMappings repositoryResourceMappings;
	private final ProxyPathMapper proxyPathMapper;

	public VersionRepositoryRestRequestMappingHandlerMapping(ContentNegotiationManager contentNegotiationManager, RepositoryResourceMappings repositoryResourceMappings,
			ProxyPathMapper proxyPathMapper) {
		Assert.notNull(contentNegotiationManager);
		Assert.notNull(repositoryResourceMappings);
		this.contentNegotiationManager = contentNegotiationManager;
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
		result = versionsConditionCompareTo(me.getProducesCondition(), other.getProducesCondition(), request);//版本比较
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

	/*
	 * ProducesCondition CompareTo
	 */
	private int versionsConditionCompareTo(ProducesRequestCondition me, ProducesRequestCondition other, HttpServletRequest request) {
		try {

			List<MediaType> meProducibleMediaTypes = new ArrayList<>(me.getProducibleMediaTypes());
			if (meProducibleMediaTypes.isEmpty()) {
				meProducibleMediaTypes.add(DEFAULT_MEDIA_TYPE);
			}
			List<MediaType> otherProducibleMediaTypes = new ArrayList<>(other.getProducibleMediaTypes());
			if (otherProducibleMediaTypes.isEmpty()) {
				otherProducibleMediaTypes.add(DEFAULT_MEDIA_TYPE);
			}
			meProducibleMediaTypes.sort(VERSION_COMPARATOR);
			otherProducibleMediaTypes.sort(VERSION_COMPARATOR);

			List<MediaType> acceptedMediaTypes = getAcceptedMediaTypes(request);
			for (MediaType acceptedMediaType : acceptedMediaTypes) {
				int thisIndex, otherIndex, result;

				thisIndex = equalVersion(meProducibleMediaTypes, acceptedMediaType);
				otherIndex = equalVersion(otherProducibleMediaTypes, acceptedMediaType);
				result = compareMatchingMediaTypes(meProducibleMediaTypes, thisIndex, otherProducibleMediaTypes, otherIndex);
				if (result != 0) {
					return result;
				}

				thisIndex = includedVersion(meProducibleMediaTypes, acceptedMediaType);
				otherIndex = includedVersion(otherProducibleMediaTypes, acceptedMediaType);
				result = compareMatchingMediaTypes(meProducibleMediaTypes, thisIndex, otherProducibleMediaTypes, otherIndex);
				if (result != 0) {
					return result;
				}
			}
			return 0;
		} catch (HttpMediaTypeNotAcceptableException ex) {
			// should never happen
			throw new IllegalStateException("Cannot compare without having any requested media types", ex);
		}
	}

	/**
	 * 含版本的MediaType 比较器
	 */
	public static final Comparator<MediaType> VERSION_COMPARATOR = new MimeType.SpecificityComparator<MediaType>() {

		@Override public int compare(MediaType mimeType1, MediaType mimeType2) {
			//VERSION compare
			int qualityComparison = compareVersion(mimeType1, mimeType2);
			if (qualityComparison != 0) {
				return qualityComparison;  // audio/*;q=0.7 < audio/*;q=0.3
			}
			return super.compare(mimeType1, mimeType2);
		}

		/*
		 * 比较版本
		 */
		private int compareVersion(MediaType mediaType1, MediaType mediaType2) {
			String version1 = mediaType1.getParameter(Version.VERSION_PARAM_NAME);
			String version2 = mediaType2.getParameter(Version.VERSION_PARAM_NAME);
			if (!StringUtils.hasText(version1)) {
				version1 = Version.DEFAULT_VERSION;
			}
			if (!StringUtils.hasText(version2)) {
				version2 = Version.DEFAULT_VERSION;
			}

			return Version.compareVersion(version1, version2);
		}

	};

	private int compareMatchingMediaTypes(List<MediaType> mediaTypes1, int index1, List<MediaType> mediaTypes2, int index2) {

		int result = 0;
		if (index1 != index2) {
			result = index2 - index1;
		} else if (index1 != -1) {
			MediaType mediaType1 = mediaTypes1.get(index1);
			MediaType mediaType2 = mediaTypes2.get(index2);
			result = VERSION_COMPARATOR.compare(mediaType1, mediaType2);
			result = (result != 0) ? result : mediaType1.compareTo(mediaType2);
		}
		return result;
	}

	private int equalVersion(List<MediaType> mediaTypes, MediaType accepteMediaType) {
		for (int i = 0; i < mediaTypes.size(); i++) {
			MediaType currentMediaType = mediaTypes.get(i);
			if (equals(accepteMediaType, currentMediaType)) {
				return i;
			}
		}
		return -1;
	}

	private int includedVersion(List<MediaType> mediaTypes, MediaType mediaType) {
		for (int i = 0; i < mediaTypes.size(); i++) {
			MediaType currentMediaType = mediaTypes.get(i);
			if (included(mediaType, currentMediaType)) {
				return i;
			}
		}
		return -1;
	}

	private boolean equals(MediaType accepteMediaType, MediaType currentMediaType) {
		String currentVersion = currentMediaType.getParameter(Version.VERSION_PARAM_NAME);
		String acceptedVersion = accepteMediaType.getParameter(Version.VERSION_PARAM_NAME);
		if (!StringUtils.hasText(currentVersion)) {
			currentVersion = Version.DEFAULT_VERSION;
		}
		if (!StringUtils.hasText(acceptedVersion)) {
			return true;
		} else
			return acceptedVersion.equalsIgnoreCase(currentVersion);
	}

	private boolean included(MediaType accepteMediaType, MediaType currentMediaType) {
		String acceptedVersion = accepteMediaType.getParameter(Version.VERSION_PARAM_NAME);
		String currentVersion = currentMediaType.getParameter(Version.VERSION_PARAM_NAME);
		if (!StringUtils.hasText(currentVersion)) {
			currentVersion = Version.DEFAULT_VERSION;
		}
		if (!StringUtils.hasText(acceptedVersion)) {
			return true;
		} else
			return currentVersion.contains(acceptedVersion) || currentVersion.matches(acceptedVersion);
	}

	private List<MediaType> getAcceptedMediaTypes(HttpServletRequest request) throws HttpMediaTypeNotAcceptableException {
		List<MediaType> mediaTypes = this.contentNegotiationManager.resolveMediaTypes(new ServletWebRequest(request));
		return mediaTypes.isEmpty() ? Collections.singletonList(MediaType.ALL) : mediaTypes;
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
