package cn.bestwu.framework.rest.controller;

import cn.bestwu.framework.data.JpaSearchRepository;
import cn.bestwu.framework.data.SearchRepository;
import cn.bestwu.framework.rest.annotation.RepositoryRestController;
import cn.bestwu.framework.rest.exception.ResourceNotFoundException;
import cn.bestwu.framework.rest.support.PersistentEntityResource;
import cn.bestwu.framework.rest.support.RootResourceInformation;
import org.hibernate.search.annotations.Indexed;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Entity 模型 基本控制类
 * 对应资源的CRUD操作API
 */
@RepositoryRestController
@RequestMapping(value = BaseController.BASE_URI) public class RepositorySearchController extends BaseController {

	@Autowired(required = false)
	private SearchRepository searchRepository;

	/*
	 * 全文搜索
	 */
	@RequestMapping(value = "/search", method = RequestMethod.GET)
	public Object search(RootResourceInformation resourceInformation, String keyword, Pageable pageable, boolean highLight) throws NoSuchMethodException {
		if (searchRepository == null) {
			throw new ResourceNotFoundException();
		}
		Assert.hasText(keyword, getText("param.notnull", "keyword"));

		Class<?> domainType = resourceInformation.getDomainType();
		if (JpaSearchRepository.class.equals(AopProxyUtils.ultimateTargetClass(searchRepository)) && !domainType.isAnnotationPresent(Indexed.class)) {
			throw new ResourceNotFoundException();
		}
		Page page = searchRepository.search(domainType, keyword, pageable, highLight);
		Link selfRel = ControllerLinkBuilder
				.linkTo(RepositorySearchController.class, RepositorySearchController.class.getMethod("search", RootResourceInformation.class, String.class, Pageable.class, boolean.class),
						resourceInformation.getPathName())
				.withSelfRel();
		return ok(new PersistentEntityResource<Object>(page, resourceInformation.getEntity(), selfRel));
	}

}
