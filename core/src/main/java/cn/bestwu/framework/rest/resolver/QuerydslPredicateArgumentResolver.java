package cn.bestwu.framework.rest.resolver;

import com.querydsl.core.types.Predicate;
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

	public QuerydslPredicateArgumentResolver(QuerydslBindingsFactory bindingsFactory, FixQuerydslPredicateBuilder predicateBuilder) {
		this.bindingsFactory = bindingsFactory;
		this.predicateBuilder = predicateBuilder;
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

		@SuppressWarnings("unchecked")
		Class<? extends QuerydslBinderCustomizer<?>> customizer = (Class<? extends QuerydslBinderCustomizer<?>>) (annotation == null
				? null : annotation.bindings());
		QuerydslBindings bindings = bindingsFactory.createBindingsFor(customizer, typeInformation);

		return predicateBuilder.getPredicate(typeInformation, parameters, bindings);
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