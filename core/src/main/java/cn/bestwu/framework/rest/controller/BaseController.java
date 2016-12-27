package cn.bestwu.framework.rest.controller;

import cn.bestwu.framework.rest.annotation.DefaultSort;
import cn.bestwu.framework.rest.resolver.DomainMethodArgumentResolver;
import cn.bestwu.framework.rest.support.Response;
import cn.bestwu.framework.util.ResourceUtil;
import cn.bestwu.lang.util.ParameterUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.support.RepositoryInvokerFactory;
import org.springframework.util.StringUtils;

import javax.servlet.ServletContext;
import java.util.*;

/**
 * 基础控制器
 * 定义通用方法、异常处理
 *
 * @author Peter Wu
 */
public abstract class BaseController extends Response {

	/**
	 * 基本实体名
	 */
	public static final String BASE_NAME = "{repository}";
	/**
	 * iduri
	 */
	public static final String ID_URI = "/{id}";
	/**
	 * 基本URI
	 */
	static final String BASE_URI = "/" + BASE_NAME;

	@Autowired(required = false)
	private ServletContext servletContext;
	@Autowired
	private MessageSource messageSource;

	@Autowired
	private RepositoryInvokerFactory invokerFactory;

	/**
	 * 得到国际化信息 未找到时返回代码 code
	 *
	 * @param code 模板
	 * @param args 参数
	 * @return 信息
	 */
	public String getText(Object code, Object... args) {
		String codeString = String.valueOf(code);
		return messageSource.getMessage(codeString, args, codeString, request == null ? Locale.CHINA : request.getLocale());
	}

	/**
	 * 得到国际化信息，未找到时返回 {@code null}
	 *
	 * @param code 模板
	 * @param args 参数
	 * @return 信息
	 */
	public String getTextDefaultNull(Object code, Object... args) {
		return messageSource.getMessage(String.valueOf(code), args, null, request == null ? Locale.CHINA : request.getLocale());
	}

	/**
	 * @param path 路径
	 * @return 真实路径
	 */
	public String getRealPath(String path) {
		return servletContext.getRealPath(path);
	}

	/**
	 * @return UserAgent
	 */
	public String getUserAgent() {
		Enumeration<String> headers = request.getHeaders("user-agent");
		if (headers.hasMoreElements()) {
			return headers.nextElement();
		} else {
			return null;
		}
	}

	/**
	 * @return 客户端设备类型
	 */
	protected String getDeviceInfo() {
		String user_agent = getUserAgent();
		if (user_agent.indexOf("Android") > 0 || user_agent.indexOf("Commons-HttpClient") > 0) {
			return "Android";
		} else if (user_agent.indexOf("iPhone") > 0) {
			return "iPhone";
		}
		return "WEB";
	}

	/**
	 * @param pageable pageable
	 * @param sort     默认sort
	 * @return 增加默认sort的pageable
	 */
	protected Pageable getDefaultPageable(Pageable pageable, Sort sort) {
		if (pageable.getSort() == null) {
			pageable = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), sort);
		}
		return pageable;
	}

	/**
	 * @param pageable   pageable
	 * @param domainType 实体类型
	 * @return 增加默认sort的pageable
	 */
	protected Pageable getDefaultPageable(Pageable pageable, Class<?> domainType) {
		if (pageable == null) {
			return null;
		}
		if (pageable.getSort() == null) {
			Sort sort = null;
			DefaultSort defaultSort = domainType.getAnnotation(DefaultSort.class);
			if (defaultSort != null) {
				String[] value = defaultSort.value();
				if (value.length > 0) {
					sort = parseParameterIntoSort(value, ",");
				}
			}
			if (sort == null) {
				sort = new Sort(Collections.singletonList(new Sort.Order(Sort.Direction.DESC, getPersistentEntity(domainType).getIdProperty().getName())));
			}
			pageable = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), sort);
		}
		return pageable;
	}

	private Sort parseParameterIntoSort(String[] source, String delimiter) {

		List<Sort.Order> allOrders = new ArrayList<>();

		for (String part : source) {

			if (part == null) {
				continue;
			}

			String[] elements = part.split(delimiter);
			Sort.Direction direction = elements.length == 0 ? null : Sort.Direction.fromStringOrNull(elements[elements.length - 1]);

			for (int i = 0; i < elements.length; i++) {

				if (i == elements.length - 1 && direction != null) {
					continue;
				}

				String property = elements[i];

				if (!StringUtils.hasText(property)) {
					continue;
				}

				allOrders.add(new Sort.Order(direction, property));
			}
		}

		return allOrders.isEmpty() ? null : new Sort(allOrders);
	}

	/**
	 * @param version 比较的版本
	 * @return 请求版本是否为version
	 */
	protected boolean versionEquals(String version) {
		return ResourceUtil.equalsVersion(version);
	}

	/**
	 * @param suffix 后缀
	 * @return 请求的版本是否以suffix后缀结尾
	 */
	protected boolean versionEndsWith(String suffix) {
		String requestVersion = ResourceUtil.REQUEST_VERSION.get();
		return requestVersion != null && (requestVersion.endsWith(suffix.toLowerCase()) || requestVersion.endsWith(suffix.toUpperCase()));
	}

	/**
	 * @return 更新前的实体
	 */
	protected Object getOldModel() {
		String oldModel = DomainMethodArgumentResolver.OLD_DOMAIN;
		return request.getAttribute(oldModel);
	}

	/**
	 * @param key 参数名称
	 * @return 是否存在此参数（非空），此方法在request body方式提交数据时可能无效
	 */
	protected boolean hasParameter(String key) {
		return ParameterUtil.hasParameter(request.getParameterMap(), key);
	}

	/**
	 * @param key 参数名称
	 * @return 是否存在此参数（可为空）
	 */
	protected boolean hasParameterKey(String key) {
		return ParameterUtil.hasParameterKey(request.getParameterMap(), key);
	}

	//	/**
	//	 * 设置已存在的相关资源
	//	 *
	//	 * @param source source
	//	 * @param action 操作
	//	 * @param <S>    S
	//	 */
	//	protected <S> void setRelatedProperty(S source, Consumer<S> action) {
	//		if (source != null) {
	//			Class<?> sourceClass = source.getClass();
	//			if (ClassUtils.isCglibProxy(source)) {
	//				sourceClass = sourceClass.getSuperclass();
	//			}
	//			PersistentEntity<?, ?> sourceEntity = getPersistentEntity(sourceClass);
	//			Object id = sourceEntity.getIdentifierAccessor(source).getIdentifier();
	//			if (id != null) {
	//				RepositoryInvoker repositoryInvoker = invokerFactory.getInvokerFor(sourceClass);
	//				source = repositoryInvoker.invokeFindOne((Serializable) id);
	//				if (source == null) {
	//					throw new IllegalArgumentException(getText("id.notFound"));
	//				}
	//			} else {
	//				source = null;
	//			}
	//		}
	//		action.accept(source);
	//	}

}
