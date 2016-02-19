package cn.bestwu.framework.rest.resolver;

import cn.bestwu.framework.rest.annotation.Domain;
import cn.bestwu.framework.rest.controller.BaseController;
import cn.bestwu.framework.rest.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.repository.support.RepositoryInvokerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.validation.BindException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.WebDataBinder;
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
import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * 资源 Domain 参数处理
 *
 * @author Peter Wu
 */
public class DomainMethodArgumentResolver implements HandlerMethodArgumentResolver {

	private final HttpMessageConverter<Object> messageConverter;
	private final RepositoryInvokerFactory invokerFactory;
	private final String idParameterName = new UriTemplate(BaseController.ID_URI).getVariableNames().get(0);

	public DomainMethodArgumentResolver(RepositoryInvokerFactory invokerFactory, HttpMessageConverter<Object> messageConverter) {
		this.invokerFactory = invokerFactory;
		this.messageConverter = messageConverter;
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(Domain.class) && parameter.getParameterType().getClassLoader() != null;
	}

	@Override public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		Object existingObject = getObjectForUpdate(parameter.getParameterType(), webRequest, binderFactory);

		return readObject(parameter, existingObject, mavContainer, webRequest, binderFactory);
	}

	protected String getId(String idParameterName, NativeWebRequest webRequest, WebDataBinderFactory binderFactory)
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

	protected Object getObjectForUpdate(Class<?> domainClass, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

		Serializable id = getId(idParameterName, webRequest, binderFactory);
		if (id == null) {
			throw new IllegalArgumentException("id不能为空");
		}

		Object existingObject = invokerFactory.getInvokerFor(domainClass).invokeFindOne(id);

		if (existingObject == null) {
			throw new ResourceNotFoundException();
		}
		return existingObject;
	}

	protected Object readObject(MethodParameter parameter, Object domainObject, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		HttpServletRequest nativeRequest = webRequest.getNativeRequest(HttpServletRequest.class);
		Class<?> domainClass = domainObject.getClass();
		String name = Introspector.decapitalize(domainClass.getSimpleName());

		if (webRequest.getParameterMap().isEmpty()) {
			ServletServerHttpRequest request = new ServletServerHttpRequest(nativeRequest);
			MediaType contentType = request.getHeaders().getContentType();
			if (messageConverter.canRead(domainClass, contentType)) {
				ObjectMapper mapper = ((MappingJackson2HttpMessageConverter) messageConverter).getObjectMapper();
				try {
					mapper.readerForUpdating(domainObject).readValue(request.getBody());
				} catch (IOException ignored) {
				}
			}
		}

		WebDataBinder binder = binderFactory.createBinder(webRequest, domainObject, name);
		if (binder.getTarget() != null) {
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

		return binder.convertIfNecessary(binder.getTarget(), domainClass);
	}

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

	protected void bindRequestParameters(WebDataBinder binder, NativeWebRequest request) {
		ServletRequest servletRequest = request.getNativeRequest(ServletRequest.class);
		ServletRequestDataBinder servletBinder = (ServletRequestDataBinder) binder;
		servletBinder.bind(servletRequest);
	}

}
