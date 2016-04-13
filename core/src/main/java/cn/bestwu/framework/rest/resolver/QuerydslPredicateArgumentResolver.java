package cn.bestwu.framework.rest.resolver;

import cn.bestwu.framework.event.AddPredicateEvent;
import cn.bestwu.framework.event.DefaultPredicateEvent;
import cn.bestwu.framework.rest.support.Resource;
import com.mysema.query.types.Predicate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.MethodParameter;
import org.springframework.data.querydsl.binding.*;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.TypeInformation;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Arrays;
import java.util.Map;

/**
 * 自定义QuerydslPredicateArgumentResolver
 *
 * @author Peter Wu
 */
public class QuerydslPredicateArgumentResolver implements HandlerMethodArgumentResolver {

	private final QuerydslBindingsFactory bindingsFactory;
	private final FixQuerydslPredicateBuilder predicateBuilder;
	private final ApplicationEventPublisher publisher;

	public QuerydslPredicateArgumentResolver(QuerydslBindingsFactory bindingsFactory, FixQuerydslPredicateBuilder predicateBuilder, ApplicationEventPublisher publisher) {
		this.bindingsFactory = bindingsFactory;
		this.predicateBuilder = predicateBuilder;
		this.publisher = publisher;
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter) {

		if (Predicate.class.equals(parameter.getParameterType())) {
			return true;
		}

		if (parameter.hasParameterAnnotation(QuerydslPredicate.class)) {
			throw new IllegalArgumentException(String.format("Parameter at position %s must be of type Predicate but was %s.",
					parameter.getParameterIndex(), parameter.getParameterType()));
		}

		return false;
	}

	@Override
	public Predicate resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();

		for (Map.Entry<String, String[]> entry : webRequest.getParameterMap().entrySet()) {
			parameters.put(entry.getKey(), Arrays.asList(entry.getValue()));
		}

		QuerydslPredicate annotation = parameter.getParameterAnnotation(QuerydslPredicate.class);
		TypeInformation<?> typeInformation = extractTypeInfo(parameter).getActualType();

		Class<? extends QuerydslBinderCustomizer> customizer = annotation == null ? null : annotation.bindings();
		QuerydslBindings bindings = bindingsFactory.createBindingsFor(customizer, typeInformation);

		Class<?> modelType = typeInformation.getType();
		{//设置默认条件
			publisher.publishEvent(new DefaultPredicateEvent(parameters, modelType));
		}
		Predicate predicate = predicateBuilder.getPredicate(typeInformation, parameters, bindings);
		{//添加条件
			Resource<Predicate> predicateResource = new Resource<>(predicate);
			publisher.publishEvent(new AddPredicateEvent(predicateResource, modelType));
			predicate = predicateResource.getContent();
		}
		return predicate;
	}

	static TypeInformation<?> extractTypeInfo(MethodParameter parameter) {

		QuerydslPredicate annotation = parameter.getParameterAnnotation(QuerydslPredicate.class);

		if (annotation != null && !Object.class.equals(annotation.root())) {
			return ClassTypeInformation.from(annotation.root());
		}

		return detectModelType(ClassTypeInformation.fromReturnTypeOf(parameter.getMethod()));
	}

	private static TypeInformation<?> detectModelType(TypeInformation<?> source) {

		if (source.getTypeArguments().isEmpty()) {
			return source;
		}

		TypeInformation<?> actualType = source.getActualType();

		if (source != actualType) {
			return detectModelType(actualType);
		}

		if (source instanceof Iterable) {
			return source;
		}

		return detectModelType(source.getComponentType());
	}
}