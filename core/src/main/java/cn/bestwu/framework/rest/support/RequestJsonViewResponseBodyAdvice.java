package cn.bestwu.framework.rest.support;

import cn.bestwu.framework.rest.mapping.SerializationViewMappings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.servlet.mvc.method.annotation.AbstractMappingJacksonResponseBodyAdvice;

/**
 * 请求JSON视图时响应结果处理
 *
 * @author Peter Wu
 */
@Slf4j
public class RequestJsonViewResponseBodyAdvice extends AbstractMappingJacksonResponseBodyAdvice {

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
		beforeBodyWrite(bodyContainer);
	}

	/**
	 * 写入响应BODY前加入序列化视图
	 *
	 * @param bodyContainer bodyContainer
	 */
	public void beforeBodyWrite(MappingJacksonValue bodyContainer) {
		Class<?> serializationView = serializationViewMappings.getSerializationView();
		if (log.isDebugEnabled()) {
			log.debug("serializationView:" + (serializationView == null ? null : serializationView.getSimpleName()));
		}
		bodyContainer.setSerializationView(serializationView);
	}

}