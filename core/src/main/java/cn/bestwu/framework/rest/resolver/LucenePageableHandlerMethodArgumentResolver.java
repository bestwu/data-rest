package cn.bestwu.framework.rest.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

public class LucenePageableHandlerMethodArgumentResolver extends PageableHandlerMethodArgumentResolver {

	private LuceneSortHandlerMethodArgumentResolver sortResolver;

	public LucenePageableHandlerMethodArgumentResolver() {
		this(null);
	}

	public LucenePageableHandlerMethodArgumentResolver(LuceneSortHandlerMethodArgumentResolver sortResolver) {
		this.sortResolver = sortResolver == null ? new LuceneSortHandlerMethodArgumentResolver() : sortResolver;
	}

	@Override public Pageable resolveArgument(MethodParameter methodParameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
		Pageable pageable = super.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);
		String sortValue = webRequest.getParameter(sortResolver.getSortParameter(methodParameter));
		if (StringUtils.hasText(sortValue)) {
			Sort sort = sortResolver.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);
			pageable = new PageRequest(pageable.getPageNumber(), pageable.getPageSize(), sort);
		}
		return pageable;
	}
}