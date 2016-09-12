package cn.bestwu.framework.rest.resolver;

import cn.bestwu.framework.rest.annotation.Model;
import cn.bestwu.framework.rest.controller.BaseController;
import cn.bestwu.framework.rest.exception.ResourceNotFoundException;
import cn.bestwu.framework.util.StringUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.data.repository.support.RepositoryInvokerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.DataBinder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.util.UriTemplate;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.beans.Introspector;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

/**
 * 资源 Model 参数处理
 *
 * @author Peter Wu
 */
@Slf4j
public class ModelMethodArgumentResolver implements HandlerMethodArgumentResolver {

	private final List<AbstractJackson2HttpMessageConverter> messageConverters;
	private final RepositoryInvokerFactory invokerFactory;
	private final String idParameterName = new UriTemplate(BaseController.ID_URI).getVariableNames().get(0);
	/**
	 * 修改前的实体 request 属性名
	 */
	public static final String OLD_MODEL = "OLD_MODEL";

	public ModelMethodArgumentResolver(RepositoryInvokerFactory invokerFactory, List<AbstractJackson2HttpMessageConverter> messageConverters) {
		this.invokerFactory = invokerFactory;
		this.messageConverters = messageConverters;
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(Model.class);
	}

	@Override public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		Class<?> modelType = parameter.getParameterType();
		Object model = resolveModel(parameter, mavContainer, webRequest, binderFactory, modelType);

		if (log.isDebugEnabled()) {
			log.debug("请求实体：" + StringUtil.valueOf(model));
		}

		return model;
	}

	/**
	 * 获取 Model
	 *
	 * @param parameter     parameter
	 * @param mavContainer  mavContainer
	 * @param webRequest    webRequest
	 * @param binderFactory binderFactory
	 * @param modelClass    modelClass
	 * @return Object
	 * @throws Exception Exception
	 */
	protected Object resolveModel(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory, Class<?> modelClass)
			throws Exception {
		Object content;
		if (webRequest.getNativeRequest(HttpServletRequest.class).getMethod().equals(RequestMethod.PUT.name())) {
			content = getObjectForUpdate(modelClass, webRequest, binderFactory);
		} else {
			String name = Introspector.decapitalize(modelClass.getSimpleName());
			content = (mavContainer.containsAttribute(name) ?
					mavContainer.getModel().get(name) : createAttribute(name, modelClass, binderFactory, webRequest));
		}

		return readObject(parameter, content, mavContainer, webRequest, binderFactory);
	}

	/**
	 * @param idParameterName id参数名
	 * @param webRequest      webRequest
	 * @param binderFactory   binderFactory
	 * @return 实体ID
	 * @throws Exception Exception
	 */
	private String getId(String idParameterName, NativeWebRequest webRequest, WebDataBinderFactory binderFactory)
			throws Exception {
		Object arg = resolveName(idParameterName, webRequest);

		if (arg == null) {
			arg = webRequest.getParameterValues(idParameterName);
		}

		if (binderFactory != null && arg != null) {
			WebDataBinder binder = binderFactory.createBinder(webRequest, null, idParameterName);
			return binder.convertIfNecessary(arg, String.class);
		}

		return null;
	}

	protected Object resolveName(String name,
			NativeWebRequest request) {
		Map uriTemplateVars = (Map) request
				.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE,
						RequestAttributes.SCOPE_REQUEST);
		return (uriTemplateVars != null) ? uriTemplateVars.get(name) : null;
	}

	/**
	 * @param modelClass    modelClass
	 * @param webRequest    webRequest
	 * @param binderFactory binderFactory
	 * @return Object 待更新实体
	 * @throws Exception Exception
	 */
	private Object getObjectForUpdate(Class<?> modelClass, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

		Serializable id = getId(idParameterName, webRequest, binderFactory);
		if (id == null) {
			throw new IllegalArgumentException("id不能为空");
		}

		Object objectForUpdate = invokerFactory.getInvokerFor(modelClass).invokeFindOne(id);

		if (objectForUpdate == null) {
			throw new ResourceNotFoundException();
		}

		Object oldModel = BeanUtils.instantiate(objectForUpdate.getClass());
		BeanUtils.copyProperties(objectForUpdate, oldModel);
		webRequest.setAttribute(OLD_MODEL, oldModel, NativeWebRequest.SCOPE_REQUEST);

		return objectForUpdate;
	}

	/**
	 * 从request 读取参数更新到实体
	 *
	 * @param parameter     parameter
	 * @param modelObject   modelObject
	 * @param mavContainer  mavContainer
	 * @param webRequest    webRequest
	 * @param binderFactory binderFactory
	 * @return Object
	 * @throws Exception Exception
	 */
	private Object readObject(MethodParameter parameter, Object modelObject, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		HttpServletRequest nativeRequest = webRequest.getNativeRequest(HttpServletRequest.class);
		Class<?> modelType = modelObject.getClass();
		String name = Introspector.decapitalize(modelType.getSimpleName());

		boolean readRequestBody = webRequest.getParameterMap().isEmpty();
		if (readRequestBody) {
			ServletServerHttpRequest request = new ServletServerHttpRequest(nativeRequest);
			MediaType contentType = request.getHeaders().getContentType();
			for (AbstractJackson2HttpMessageConverter messageConverter : messageConverters) {
				if (messageConverter.canRead(modelType, contentType)) {
					ObjectMapper mapper = messageConverter.getObjectMapper();
					mapper.readerForUpdating(modelObject).readValue(request.getBody());
					break;
				}
			}
		}

		WebDataBinder binder = binderFactory.createBinder(webRequest, modelObject, name);
		if (binder.getTarget() != null) {
			if (!readRequestBody)
				bindRequestParameters(binder, webRequest);
			validateIfApplicable(binder, parameter);
			if (binder.getBindingResult().hasErrors()) {
				throw new BindException(binder.getBindingResult());
			}
		}

		// Add resolved attribute and BindingResult at the end of the model
		Map<String, Object> bindingResultModel = binder.getBindingResult().getModel();
		mavContainer.removeAttributes(bindingResultModel);
		mavContainer.addAllAttributes(bindingResultModel);

		return binder.convertIfNecessary(binder.getTarget(), modelType);
	}

	/**
	 * 如果有必要验证参数
	 *
	 * @param binder      binder
	 * @param methodParam methodParam
	 */
	protected void validateIfApplicable(WebDataBinder binder, MethodParameter methodParam) {
		Annotation[] annotations = methodParam.getParameterAnnotations();
		for (Annotation ann : annotations) {
			Validated validatedAnn = AnnotationUtils.getAnnotation(ann, Validated.class);
			if (validatedAnn != null || ann.annotationType().getSimpleName().startsWith("Valid")) {
				Object hints = (validatedAnn != null ? validatedAnn.value() : AnnotationUtils.getValue(ann));
				Object[] validationHints = (hints instanceof Object[] ? (Object[]) hints : new Object[] { hints });
				binder.validate(validationHints);
				break;
			}
		}
	}

	/**
	 * 绑定请求参数
	 *
	 * @param binder  binder
	 * @param request request
	 */
	protected void bindRequestParameters(WebDataBinder binder, NativeWebRequest request) {
		ServletRequest servletRequest = request.getNativeRequest(ServletRequest.class);
		ServletRequestDataBinder servletBinder = (ServletRequestDataBinder) binder;
		servletBinder.bind(servletRequest);
	}

	/**
	 * 从spring 源码 修改来的
	 *
	 * @param attributeName attributeName
	 * @param modelClass    modelClass
	 * @param binderFactory binderFactory
	 * @param request       request
	 * @return Object
	 * @throws Exception Exception
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

	/**
	 * 从spring 源码 修改来的
	 *
	 * @param attributeName attributeName
	 * @param request       request
	 * @return return
	 */
	protected String getRequestValueForAttribute(String attributeName, NativeWebRequest request) {
		if (StringUtils.hasText(request.getParameter(attributeName))) {
			return request.getParameter(attributeName);
		} else {
			return null;
		}
	}

	/**
	 * 从spring 源码 修改来的
	 *
	 * @param sourceValue   sourceValue
	 * @param attributeName attributeName
	 * @param modelClass    modelClass
	 * @param binderFactory binderFactory
	 * @param request       request
	 * @return Object
	 * @throws Exception Exception
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
