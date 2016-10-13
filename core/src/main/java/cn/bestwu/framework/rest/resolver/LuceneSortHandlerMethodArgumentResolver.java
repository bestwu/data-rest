package cn.bestwu.framework.rest.resolver;

import cn.bestwu.framework.data.query.LuceneSort;
import cn.bestwu.framework.util.BooleanUtil;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.ArrayList;
import java.util.List;

public class LuceneSortHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

	private static final String DEFAULT_PARAMETER = "lucenceSort";
	private static final String DEFAULT_PROPERTY_DELIMITER = ",";
	private static final String DEFAULT_QUALIFIER_DELIMITER = "_";

	private String sortParameter = DEFAULT_PARAMETER;
	private String propertyDelimiter = DEFAULT_PROPERTY_DELIMITER;
	private String qualifierDelimiter = DEFAULT_QUALIFIER_DELIMITER;

	/**
	 * Configure the request parameter to lookup sort information from. Defaults to {@code sort}.
	 *
	 * @param sortParameter must not be {@literal null} or empty.
	 */
	public void setSortParameter(String sortParameter) {

		Assert.hasText(sortParameter);
		this.sortParameter = sortParameter;
	}

	/**
	 * Configures the delimiter used to separate property references and the direction to be sorted by. Defaults to
	 * {@code}, which means sort values look like this: {@code firstname,lastname,asc}.
	 *
	 * @param propertyDelimiter must not be {@literal null} or empty.
	 */
	public void setPropertyDelimiter(String propertyDelimiter) {

		Assert.hasText(propertyDelimiter, "Property delimiter must not be null or empty!");
		this.propertyDelimiter = propertyDelimiter;
	}

	/**
	 * Configures the delimiter used to separate the qualifier from the sort parameter. Defaults to {@code _}, so a
	 * qualified sort property would look like {@code qualifier_sort}.
	 *
	 * @param qualifierDelimiter the qualifier delimiter to be used or {@literal null} to reset to the default.
	 */
	public void setQualifierDelimiter(String qualifierDelimiter) {
		this.qualifierDelimiter = qualifierDelimiter == null ? DEFAULT_QUALIFIER_DELIMITER : qualifierDelimiter;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.web.method.support.HandlerMethodArgumentResolver#supportsParameter(org.springframework.core.MethodParameter)
	 */
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return LuceneSort.class.equals(parameter.getParameterType());
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.web.method.support.HandlerMethodArgumentResolver#resolveArgument(org.springframework.core.MethodParameter, org.springframework.web.method.support.ModelAndViewContainer, org.springframework.web.context.request.NativeWebRequest, org.springframework.web.bind.support.WebDataBinderFactory)
	 */
	@Override
	public LuceneSort resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

		String[] directionParameter = webRequest.getParameterValues(getSortParameter(parameter));

		// No parameter
		if (directionParameter == null) {
			return null;
		}

		// Single empty parameter, e.g "sort="
		if (directionParameter.length == 1 && !StringUtils.hasText(directionParameter[0])) {
			return null;
		}

		return parseParameterIntoSort(directionParameter, propertyDelimiter);
	}

	protected String getSortParameter(MethodParameter parameter) {

		StringBuilder builder = new StringBuilder();

		if (parameter != null && parameter.hasParameterAnnotation(Qualifier.class)) {
			builder.append(parameter.getParameterAnnotation(Qualifier.class).value()).append(qualifierDelimiter);
		}

		return builder.append(sortParameter).toString();
	}

	private LuceneSort parseParameterIntoSort(String[] source, String delimiter) {

		List<SortField> allOrders = new ArrayList<>();

		for (String part : source) {

			if (part == null) {
				continue;
			}

			String[] elements = part.split(delimiter);
			int length = elements.length;
			Boolean reverse = length < 2 ? null : BooleanUtil.toBooleanObject(elements[length - 1]);
			SortField.Type type = null;
			if (length >= 1) {
				if (reverse == null)
					type = getType(elements[length - 1]);
				else
					type = getType(elements[length - 2]);
			}
			for (int i = 0; i < length; i++) {

				if (i == length - 1 && (reverse != null || type != null) || i == length - 2 && type != null) {
					if (length == 1)
						allOrders.add(new SortField(null, type));
					continue;
				}

				String property = elements[i];

				if (!StringUtils.hasText(property)) {
					continue;
				}

				allOrders.add(new SortField(property, type == null ? SortField.Type.SCORE : type, reverse == null ? false : reverse));
			}
		}

		return allOrders.isEmpty() ? null : new LuceneSort(new Sort(allOrders.toArray(new SortField[allOrders.size()])));
	}

	private SortField.Type getType(String element) {
		try {
			return SortField.Type.valueOf(element);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

}