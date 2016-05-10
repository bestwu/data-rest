package cn.bestwu.framework.rest.controller;

import cn.bestwu.framework.event.DefaultSortEvent;
import cn.bestwu.framework.rest.resolver.ModelMethodArgumentResolver;
import cn.bestwu.framework.rest.support.Response;
import cn.bestwu.framework.util.ParameterUtil;
import cn.bestwu.framework.util.ResourceUtil;
import cn.bestwu.framework.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.repository.support.RepositoryInvoker;
import org.springframework.data.repository.support.RepositoryInvokerFactory;
import org.springframework.util.ClassUtils;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

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

	@Autowired(required = false)
	protected HttpServletRequest request;

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

	public String getRealPath(String path) {
		return servletContext.getRealPath(path);
	}

	public String getUserAgent() {
		return StringUtil.valueOf(request.getHeaders("user-agent"));
	}

	protected String getDeviceInfo() {
		String user_agent = getUserAgent();
		if (user_agent.indexOf("Android") > 0 || user_agent.indexOf("Commons-HttpClient") > 0) {
			return "Android";
		} else if (user_agent.indexOf("iPhone") > 0) {
			return "iPhone";
		}
		return "WEB";
	}

	protected Pageable getDefaultPageable(Pageable pageable, Sort sort) {
		if (pageable.getSort() == null) {
			pageable = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), sort);
		}
		return pageable;
	}

	protected Pageable getDefaultPageable(Pageable pageable, Class<?> modelType) {
		if (pageable == null) {
			return null;
		}
		if (pageable.getSort() == null) {
			List<Sort.Order> orders = new ArrayList<>();
			publisher.publishEvent(new DefaultSortEvent(orders, modelType));
			if (orders.isEmpty()) {
				orders.add(new Sort.Order(Sort.Direction.DESC, "id"));
			}
			pageable = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), new Sort(orders));
		}
		return pageable;
	}

	protected boolean versionEquals(String version) {
		return ResourceUtil.equalsVersion(request, version);
	}

	protected boolean versionEndsWith(String suffix) {
		String requestVersion = ResourceUtil.getRequestVersion(request);
		return requestVersion != null && (requestVersion.endsWith(suffix.toLowerCase()) || requestVersion.endsWith(suffix.toUpperCase()));
	}

	protected Object getOldModel() {
		String oldModel = ModelMethodArgumentResolver.OLD_MODEL;
		return request.getAttribute(oldModel);
	}

	/**
	 * @param key 参数名称
	 * @return 是否存在此参数，此方法在request body方式提交数据时可能无效
	 */
	protected boolean hasParameter(String key) {
		return ParameterUtil.hasParameter(request.getParameterMap(), key);
	}

	protected boolean hasParameterKey(String key) {
		return ParameterUtil.hasParameterKey(request.getParameterMap(), key);
	}

	/**
	 * 设置已存在的相关资源
	 *
	 * @param source source
	 * @param action 操作
	 * @param <S>    S
	 */
	protected <S> void setRelatedProperty(S source, Consumer<S> action) {
		if (source != null) {
			Class<?> sourceClass = source.getClass();
			if (ClassUtils.isCglibProxy(source)) {
				sourceClass = sourceClass.getSuperclass();
			}
			PersistentEntity<?, ?> sourceEntity = getPersistentEntity(sourceClass);
			Object id = sourceEntity.getIdentifierAccessor(source).getIdentifier();
			if (id != null) {
				RepositoryInvoker repositoryInvoker = invokerFactory.getInvokerFor(sourceClass);
				source = repositoryInvoker.invokeFindOne((Serializable) id);
				if (source == null) {
					throw new IllegalArgumentException(getText("id.notFound"));
				}
			} else {
				source = null;
			}
		}
		action.accept(source);
	}

}
