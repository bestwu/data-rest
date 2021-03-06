package cn.bestwu.framework.rest.controller;

import cn.bestwu.api.sign.ApiSign;
import cn.bestwu.framework.data.query.ResultHandler;
import cn.bestwu.framework.data.query.SearchRepository;
import cn.bestwu.framework.data.query.jpa.HighlightResultHandler;
import cn.bestwu.framework.data.query.jpa.JpaSearchRepository;
import cn.bestwu.framework.rest.annotation.RepositoryRestController;
import cn.bestwu.framework.rest.exception.ResourceNotFoundException;
import cn.bestwu.framework.rest.support.PersistentEntityResource;
import cn.bestwu.framework.rest.support.RootResourceInformation;
import org.hibernate.search.annotations.Indexed;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 全文搜索
 *
 * @author Peter Wu
 */
@ApiSign
@RepositoryRestController
@ConditionalOnBean(SearchRepository.class)
@RequestMapping(value = BaseController.BASE_URI) public class RepositorySearchController extends BaseController {

	@Autowired
	private SearchRepository searchRepository;
	@Autowired(required = false)
	private ResultHandler resultHandler;

	/**
	 * 全文搜索
	 *
	 * @param resourceInformation resourceInformation
	 * @param keyword             关键字
	 * @param pageable            pageable
	 * @param highLight           是否高亮
	 * @return return
	 * @throws NoSuchMethodException NoSuchMethodException
	 */
	@RequestMapping(value = "/search/fulltext", method = RequestMethod.GET)
	public Object search(RootResourceInformation resourceInformation, String keyword, Pageable pageable, boolean highLight) throws NoSuchMethodException {
		if (searchRepository == null) {
			throw new ResourceNotFoundException();
		}
		Assert.hasText(keyword, getText("param.notnull", "keyword"));

		Class<?> domainType = resourceInformation.getModelType();
		boolean isJpaSearchRepository = JpaSearchRepository.class.equals(AopProxyUtils.ultimateTargetClass(searchRepository));
		if (isJpaSearchRepository && !domainType.isAnnotationPresent(Indexed.class)) {
			throw new ResourceNotFoundException();
		}
		ResultHandler resultHandler = this.resultHandler;
		if (highLight) {
			if (resultHandler == null) {
				if (isJpaSearchRepository) {
					resultHandler = new HighlightResultHandler();
				} else {
					//MongodbSearchRepository 不支持默认高亮
				}
			}
		} else {
			resultHandler = null;
		}
		Page page = searchRepository.search(domainType, keyword, pageable, resultHandler);

		Link selfRel = ControllerLinkBuilder
				.linkTo(RepositorySearchController.class, RepositorySearchController.class.getMethod("search", RootResourceInformation.class, String.class, Pageable.class, boolean.class),
						resourceInformation.getPathName())
				.withSelfRel();
		return ok(new PersistentEntityResource<Object>(page, resourceInformation.getEntity(), selfRel));
	}

}
