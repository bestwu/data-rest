package cn.bestwu.framework.rest.resolver;

import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.springframework.core.MethodParameter;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.ArrayList;
import java.util.List;

public class SearchSortHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

	private static final String DEFAULT_PARAMETER = "sort";
	private static final SortField.Type DEFAULT_SORT_TYPE = SortField.Type.SCORE;
	private static final String DEFAULT_PROPERTY_DELIMITER = ",";

	private SortField.Type sortType = DEFAULT_SORT_TYPE;

	public SearchSortHandlerMethodArgumentResolver() {
	}

	public SearchSortHandlerMethodArgumentResolver(SortField.Type sortType) {
		this.sortType = sortType;
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return Sort.class.equals(parameter.getParameterType());
	}

	@Override
	public Sort resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

		String[] sortParameter = webRequest.getParameterValues(DEFAULT_PARAMETER);

		// No parameter
		if (sortParameter == null) {
			return null;
		}

		// Single empty parameter, e.g "sort="
		if (sortParameter.length == 1 && !StringUtils.hasText(sortParameter[0])) {
			return null;
		}

		return parseParameterIntoSort(sortParameter, DEFAULT_PROPERTY_DELIMITER);
	}

	private Sort parseParameterIntoSort(String[] source, String delimiter) {

		List<SortField> sortFields = new ArrayList<>();

		for (String part : source) {

			if (!StringUtils.hasText(part)) {
				continue;
			}

			String[] elements = part.split(delimiter);
			org.springframework.data.domain.Sort.Direction direction = elements.length == 1 ? null : org.springframework.data.domain.Sort.Direction.fromStringOrNull(elements[elements.length - 1]);

			boolean reverse = false;
			if (org.springframework.data.domain.Sort.Direction.DESC.equals(direction)) {
				reverse = true;
			}
			for (int i = 0; i < elements.length; i++) {

				if (i == elements.length - 1 && direction != null) {
					continue;
				}

				String property = elements[i];

				if (!StringUtils.hasText(property)) {
					continue;
				}

				sortFields.add(new SortField(property, sortType, reverse));
			}
		}
		return sortFields.isEmpty() ? null : new Sort(sortFields.toArray(new SortField[sortFields.size()]));
	}

	public void setSortType(SortField.Type sortType) {
		this.sortType = sortType;
	}
}