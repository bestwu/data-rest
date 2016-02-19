package cn.bestwu.framework.rest.resolver;

import cn.bestwu.framework.rest.support.PersistentEntityResource;
import cn.bestwu.framework.rest.support.RepositoryResourceMetadata;
import cn.bestwu.framework.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.data.repository.support.RepositoryInvokerFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.StringUtils;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import java.beans.Introspector;

/**
 * ModelResource 处理
 *
 * @author Peter Wu
 */
public class ResourceMethodArgumentResolver extends DomainMethodArgumentResolver {

	private final Logger logger = LoggerFactory.getLogger(ResourceMethodArgumentResolver.class);

	private final RepositoryResourceMetadataHandlerMethodArgumentResolver resourceMetadataResolver;

	public ResourceMethodArgumentResolver(RepositoryResourceMetadataHandlerMethodArgumentResolver resourceMetadataResolver,
			RepositoryInvokerFactory invokerFactory, HttpMessageConverter<Object> messageConverter) {
		super(invokerFactory, messageConverter);
		this.resourceMetadataResolver = resourceMetadataResolver;
	}

	/*
	 * 支持的参数类型
	 *
	 */
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return PersistentEntityResource.class.isAssignableFrom(parameter.getParameterType());
	}

	/*
	 * 处理参数
	 *
	 */
	@Override
	public PersistentEntityResource<?> resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		RepositoryResourceMetadata resourceMetadata = resourceMetadataResolver.resolveArgument(parameter, mavContainer, webRequest,
				binderFactory);
		if (resourceMetadata == null) {
			return null;
		}
		Class<?> domainType = resourceMetadata.getDomainType();
		Object content;
		if (webRequest.getNativeRequest(HttpServletRequest.class).getMethod().equals(RequestMethod.PUT.name())) {
			content = getObjectForUpdate(domainType, webRequest, binderFactory);
		} else {
			String name = Introspector.decapitalize(domainType.getSimpleName());
			content = (mavContainer.containsAttribute(name) ?
					mavContainer.getModel().get(name) : createAttribute(name, domainType, binderFactory, webRequest));
		}
		content = readObject(parameter, content, mavContainer, webRequest, binderFactory);

		if (logger.isDebugEnabled()) {
			logger.debug("请求实体：{}", StringUtil.valueOf(content));
		}

		return new PersistentEntityResource<>(content, resourceMetadata.getEntity());
	}

	/*
	 * 从spring 源码 修改来的
	 */
	protected final Object createAttribute(String attributeName, Class<?> modelClass,
			WebDataBinderFactory binderFactory, NativeWebRequest request) throws Exception {

		String value = getRequestValueForAttribute(attributeName, request);
		if (value != null) {
			Object attribute = createAttributeFromRequestValue(
					value, attributeName, modelClass, binderFactory, request);
			if (attribute != null) {
				return attribute;
			}
		}

		return BeanUtils.instantiateClass(modelClass);
	}

	/*
	 * 从spring 源码 修改来的
	 */
	protected String getRequestValueForAttribute(String attributeName, NativeWebRequest request) {
		if (StringUtils.hasText(request.getParameter(attributeName))) {
			return request.getParameter(attributeName);
		} else {
			return null;
		}
	}

	/*
	 * 从spring 源码 修改来的
	 */
	protected Object createAttributeFromRequestValue(String sourceValue, String attributeName,
			Class<?> modelClass, WebDataBinderFactory binderFactory, NativeWebRequest request)
			throws Exception {

		DataBinder binder = binderFactory.createBinder(request, null, attributeName);
		ConversionService conversionService = binder.getConversionService();
		if (conversionService != null) {
			TypeDescriptor source = TypeDescriptor.valueOf(String.class);
			TypeDescriptor target = TypeDescriptor.valueOf(modelClass);
			if (conversionService.canConvert(source, target)) {
				return binder.convertIfNecessary(sourceValue, modelClass);
			}
		}
		return null;
	}
}
