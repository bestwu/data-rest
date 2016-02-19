package cn.bestwu.framework.rest.controller;

import cn.bestwu.framework.data.SearchRepository;
import cn.bestwu.framework.rest.annotation.RepositoryRestController;
import cn.bestwu.framework.rest.exception.ResourceNotFoundException;
import cn.bestwu.framework.rest.support.PersistentEntityResource;
import cn.bestwu.framework.rest.support.RootResourceInformation;
import com.mysema.commons.lang.Assert;
import org.apache.lucene.search.Sort;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.jpa.Search;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Entity 模型 基本控制类
 * 对应资源的CRUD操作API
 */
@RepositoryRestController
@ConditionalOnClass(Search.class)
@RequestMapping(value = BaseController.BASE_URI) public class RepositorySearchController extends BaseController {

	@Autowired
	private SearchRepository tvSearch;

	/*
	 * 全文搜索
	 */
	@RequestMapping(value = "/search", method = RequestMethod.GET)
	public Object search(RootResourceInformation resourceInformation, String keyword, Pageable pageable, Sort sort, boolean highLight) throws NoSuchMethodException {
		Assert.hasText(keyword, getText("param.notnull", "keyword"));

		Class<?> domainType = resourceInformation.getDomainType();
		if (!domainType.isAnnotationPresent(Indexed.class)) {
			throw new ResourceNotFoundException();
		}
		Page page = tvSearch.search(domainType, keyword, pageable, sort, highLight, null);
		Link selfRel = ControllerLinkBuilder
				.linkTo(RepositorySearchController.class, RepositorySearchController.class.getMethod("search", RootResourceInformation.class, String.class, Pageable.class, Sort.class, boolean.class),
						resourceInformation.getPathName())
				.withSelfRel();
		return ok(new PersistentEntityResource<Object>(page, resourceInformation.getEntity(), selfRel));
	}

}
