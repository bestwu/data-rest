package cn.bestwu.framework.rest.support;

import cn.bestwu.framework.rest.controller.BaseController;
import cn.bestwu.framework.rest.mapping.VersionRepositoryRestRequestMappingHandlerMapping;
import cn.bestwu.framework.util.StringUtil;
import org.atteo.evo.inflector.English;
import org.springframework.hateoas.core.AnnotationMappingDiscoverer;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * 资源 工具类
 *
 * @author Peter Wu
 */
public class ResourceUtil {

	public static final String SEPARATOR = ",";

	private static AnnotationMappingDiscoverer DISCOVERER = new AnnotationMappingDiscoverer(RequestMapping.class);

	/**
	 * 得到资源名
	 *
	 * @param clazz 类
	 * @return 资源名
	 */
	public static String getRepositoryBasePathName(Class<?> clazz) {
		return English.plural(StringUtils.uncapitalize(clazz.getSimpleName()));
	}

	/**
	 * @param request http 请求实体
	 * @return HTTP请求api 签名 不带版本信息
	 */
	public static String getRequestSignature(HttpServletRequest request) {
		HandlerMethod handlerMethod = (HandlerMethod) request.getAttribute(VersionRepositoryRestRequestMappingHandlerMapping.REQUEST_HANDLER_METHOD);
		if (handlerMethod == null) {
			return null;
		}
		String apiSignature = request.getMethod() + DISCOVERER.getMapping(handlerMethod.getMethod());

		String repositoryBasePathName = (String) request.getAttribute(VersionRepositoryRestRequestMappingHandlerMapping.REQUEST_REPOSITORY_BASE_PATH_NAME);
		if (repositoryBasePathName != null) {
			apiSignature = apiSignature.replace(BaseController.BASE_NAME, repositoryBasePathName);
		}
		String searchName = (String) request.getAttribute(VersionRepositoryRestRequestMappingHandlerMapping.REQUEST_REPOSITORY_SEARCH_NAME);
		if (searchName != null) {
			apiSignature = apiSignature.replace("{search}", searchName);
		}

		apiSignature = StringUtil.addUnderscores(apiSignature.replaceAll("[{}]", "").replace("/", "_")).toUpperCase();
		return apiSignature;
	}

	/**
	 * @param request http 请求实体
	 * @return HTTP请求api 版本
	 */
	public static String getRequestVersion(HttpServletRequest request) {
		String request_version_key = "REQUEST_VERSION";
		String version = (String) request.getAttribute(request_version_key);
		if (version == null) {
			Enumeration<String> accept = request.getHeaders("Accept");
			while (accept.hasMoreElements()) {
				String element = accept.nextElement();
				String[] split = element.split(",");
				for (String s : split) {
					version = getVersion(s);
					if (StringUtils.hasText(version)) {
						break;
					}
				}
			}
			if (!StringUtils.hasText(version))
				version = Version.DEFAULT_VERSION;

			request.setAttribute(request_version_key, version);
		}

		return version;
	}

	/**
	 * 解析版本号
	 *
	 * @param versionAcceptParam versionAcceptParam
	 * @return 版本号
	 */
	public static String getVersion(String versionAcceptParam) {
		return MediaType.valueOf(versionAcceptParam).getParameter(Version.VERSION_PARAM_NAME);
	}

}