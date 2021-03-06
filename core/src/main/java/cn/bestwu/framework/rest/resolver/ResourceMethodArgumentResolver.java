package cn.bestwu.framework.rest.resolver;

import cn.bestwu.framework.rest.support.PersistentEntityResource;
import cn.bestwu.framework.rest.support.RepositoryResourceMetadata;
import org.springframework.core.MethodParameter;
import org.springframework.data.repository.support.RepositoryInvokerFactory;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;

/**
 * ModelResource 处理
 *
 * @author Peter Wu
 */
public class ResourceMethodArgumentResolver extends DomainMethodArgumentResolver {

	private final RepositoryResourceMetadataHandlerMethodArgumentResolver resourceMetadataResolver;

	public ResourceMethodArgumentResolver(RepositoryResourceMetadataHandlerMethodArgumentResolver resourceMetadataResolver,
			RepositoryInvokerFactory invokerFactory, List<AbstractJackson2HttpMessageConverter> messageConverters) {
		super(invokerFactory, messageConverters);
		this.resourceMetadataResolver = resourceMetadataResolver;
	}

	/**
	 * 支持的参数类型
	 *
	 * @param parameter parameter
	 * @return 是否支持
	 */
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return PersistentEntityResource.class.isAssignableFrom(parameter.getParameterType());
	}

	/**
	 * 处理参数
	 *
	 * @param parameter     parameter
	 * @param mavContainer  mavContainer
	 * @param webRequest    webRequest
	 * @param binderFactory binderFactory
	 * @return PersistentEntityResource
	 * @throws Exception Exception
	 */
	@Override
	public PersistentEntityResource<?> resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		RepositoryResourceMetadata resourceMetadata = resourceMetadataResolver.resolveArgument(parameter, mavContainer, webRequest,
				binderFactory);
		if (resourceMetadata == null) {
			return null;
		}
		Class<?> domainType = resourceMetadata.getModelType();
		return new PersistentEntityResource<>(resolveModel(parameter, mavContainer, webRequest, binderFactory, domainType), resourceMetadata.getEntity());
	}

}
