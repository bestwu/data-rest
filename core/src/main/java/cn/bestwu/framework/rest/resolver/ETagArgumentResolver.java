
package cn.bestwu.framework.rest.resolver;

import cn.bestwu.framework.rest.support.ETag;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import static org.springframework.http.HttpHeaders.IF_MATCH;

/**
 * ETagArgumentResolver
 *
 * @author Peter Wu
 */
public class ETagArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.getParameterType().equals(ETag.class);
	}

	@Override
	public ETag resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		return ETag.from(webRequest.getHeader(IF_MATCH));
	}
}
