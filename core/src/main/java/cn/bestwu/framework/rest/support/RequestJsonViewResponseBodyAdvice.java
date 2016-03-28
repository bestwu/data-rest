package cn.bestwu.framework.rest.support;

import cn.bestwu.framework.rest.mapping.SerializationViewMappings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.servlet.mvc.method.annotation.AbstractMappingJacksonResponseBodyAdvice;

import javax.servlet.http.HttpServletRequest;

public class RequestJsonViewResponseBodyAdvice extends AbstractMappingJacksonResponseBodyAdvice {

	private final Logger logger = LoggerFactory.getLogger(RequestJsonViewResponseBodyAdvice.class);
	private SerializationViewMappings serializationViewMappings;

	public RequestJsonViewResponseBodyAdvice(SerializationViewMappings serializationViewMappings) {
		this.serializationViewMappings = serializationViewMappings;
	}

	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		return (super.supports(returnType, converterType) && returnType.getMethod().getReturnType().isAssignableFrom(HttpEntity.class));
	}

	@Override
	protected void beforeBodyWriteInternal(MappingJacksonValue bodyContainer, MediaType contentType,
			MethodParameter returnType, ServerHttpRequest request, ServerHttpResponse response) {
		beforeBodyWrite(bodyContainer, ((ServletServerHttpRequest) request).getServletRequest());
	}

	public void beforeBodyWrite(MappingJacksonValue bodyContainer, HttpServletRequest request) {
		Class<?> serializationView = serializationViewMappings.getSerializationView(request);
		if (logger.isDebugEnabled()) {
			logger.debug("serializationView:{}", serializationView == null ? null : serializationView.getSimpleName());
		}
		bodyContainer.setSerializationView(serializationView);
	}

}