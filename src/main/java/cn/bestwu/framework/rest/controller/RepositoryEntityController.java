package cn.bestwu.framework.rest.controller;

import cn.bestwu.framework.event.*;
import cn.bestwu.framework.rest.annotation.RepositoryRestController;
import cn.bestwu.framework.rest.exception.ResourceNotFoundException;
import cn.bestwu.framework.rest.mapping.VersionRepositoryRestRequestMappingHandlerMapping;
import cn.bestwu.framework.rest.support.ETag;
import cn.bestwu.framework.rest.support.PersistentEntityResource;
import cn.bestwu.framework.rest.support.RootResourceInformation;
import org.springframework.core.MethodParameter;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.support.RepositoryInvoker;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.core.AnnotationAttribute;
import org.springframework.hateoas.core.MethodParameters;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Entity 模型 基本控制类
 * 对应资源的CRUD操作API
 */
@RepositoryRestController
@RequestMapping(value = BaseController.BASE_URI) public class RepositoryEntityController extends BaseController {

	/*
	 * Repository方法搜索
	 */
	@RequestMapping(value = "/search/{search}", method = RequestMethod.GET)
	public Object searchMethod(RootResourceInformation resourceInformation, @RequestParam MultiValueMap<String, Object> parameters, @PathVariable String search, Pageable pageable)
			throws NoSuchMethodException {
		Method searchMethod = resourceInformation.getResourceMetadata().getSearchMethod(search);
		if (searchMethod == null) {
			throw new ResourceNotFoundException();
		}
		request.setAttribute(VersionRepositoryRestRequestMappingHandlerMapping.REQUEST_REPOSITORY_SEARCH_NAME, search);

		RepositoryInvoker invoker = resourceInformation.getInvoker();

		MultiValueMap<String, Object> result = new LinkedMultiValueMap<>(parameters);
		MethodParameters methodParameters = new MethodParameters(searchMethod, new AnnotationAttribute(Param.class));

		for (Map.Entry<String, List<Object>> entry : parameters.entrySet()) {

			MethodParameter parameter = methodParameters.getParameter(entry.getKey());

			if (parameter == null) {
				continue;
			}
			//			ResourceMetadata metadata = mappings.getMetadataFor(parameter.getParameterType());
			//
			//			if (metadata != null && metadata.isExported())
			result.put(parameter.getParameterName(), entry.getValue());
		}
		Object o = invoker.invokeQueryMethod(searchMethod, result, pageable, null);

		if (o == null) {
			throw new ResourceNotFoundException();
		}

		Link selfLink = ControllerLinkBuilder
				.linkTo(RepositoryEntityController.class, RepositoryEntityController.class.getMethod("searchMethod", RootResourceInformation.class, MultiValueMap.class, String.class, Pageable.class),
						resourceInformation.getPathName(), search).withSelfRel();

		return ok(new PersistentEntityResource<>(o, resourceInformation.getEntity(), selfLink));
	}

	/*
	 * 查看资源列表
	 */
	@RequestMapping(method = RequestMethod.GET)
	public Object index(RootResourceInformation resourceInformation, Pageable pageable, boolean all) {

		RepositoryInvoker invoker = resourceInformation.getInvoker();

		pageable = getDefaultPageable(pageable, resourceInformation.getModelType());
		Iterable<?> results;
		if (all && resourceInformation.getResourceMetadata().enableAllDataInOnePage()) {
			results = invoker.invokeFindAll(pageable.getSort());
		} else {
			results = invoker.invokeFindAll(pageable);
		}

		return ok(new PersistentEntityResource<Object>(results, resourceInformation.getEntity(), getBaseLinkBuilder(resourceInformation.getPathName()).withSelfRel()));
	}

	/*
	 * 查看单个资源
	 */
	@RequestMapping(value = ID_URI, method = RequestMethod.GET)
	public Object show(RootResourceInformation resourceInformation, @PathVariable String id) {
		RepositoryInvoker invoker = resourceInformation.getInvoker();

		Object one = invoker.invokeFindOne(id);

		if (one == null) {
			throw new ResourceNotFoundException();
		}
		publisher.publishEvent(new BeforeShowEvent(one));
		PersistentEntityResource<Object> resource = new PersistentEntityResource<>(one, resourceInformation.getEntity(), getSelfRelLink(resourceInformation, id));
		publisher.publishEvent(new ResourceAddLinkEvent(one, resource));
		return ok(resource);
	}

	/*
	 * 创建资源
	 */
	@RequestMapping(method = RequestMethod.POST)
	public Object create(RootResourceInformation resourceInformation, @Valid PersistentEntityResource resource) {
		RepositoryInvoker invoker = resourceInformation.getInvoker();

		Object content = resource.getContent();

		publisher.publishEvent(new BeforeCreateEvent(content));
		invoker.invokeSave(content);
		publisher.publishEvent(new AfterCreateEvent(content));

		resource.add(getSelfRelLink(resourceInformation, getId(resourceInformation.getEntity(), content)));
		publisher.publishEvent(new ResourceAddLinkEvent(content, resource));
		return created(resource);
	}

	/*
	 * 修改资源
	 */
	@RequestMapping(value = ID_URI, method = RequestMethod.PUT)
	public Object update(RootResourceInformation resourceInformation, @Valid PersistentEntityResource resource, @PathVariable String id, ETag eTag) {
		RepositoryInvoker invoker = resourceInformation.getInvoker();

		Object content = resource.getContent();

		eTag.verify(resourceInformation.getEntity(), resourceInformation.getModelType());

		publisher.publishEvent(new BeforeSaveEvent(content));
		invoker.invokeSave(content);
		publisher.publishEvent(new AfterSaveEvent(content));

		resource.add(getSelfRelLink(resourceInformation, id));
		publisher.publishEvent(new ResourceAddLinkEvent(content, resource));
		return updated(resource);
	}

	/*
	 * 删除资源
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = ID_URI, method = RequestMethod.DELETE)
	public Object destroy(RootResourceInformation resourceInformation, @PathVariable String id) {
		RepositoryInvoker invoker = resourceInformation.getInvoker();

		Object one = invoker.invokeFindOne(id);

		if (one == null) {
			throw new ResourceNotFoundException();
		}
		publisher.publishEvent(new BeforeDeleteEvent(one));

		invoker.invokeDelete(id);

		return noContent();
	}

	/*
	 * 删除多个资源
	 *
	 */
	@RequestMapping(method = RequestMethod.DELETE)
	public Object batchDestroy(RootResourceInformation resourceInformation, String... id) throws IllegalAccessException, InstantiationException {
		RepositoryInvoker invoker = resourceInformation.getInvoker();

		if (id == null) {
			throw new IllegalArgumentException(getText("param.notnull", "id"));
		}
		Arrays.stream(id).forEach(i -> {
			try {
				Object one = invoker.invokeFindOne(i);

				if (one == null) {
					throw new ResourceNotFoundException();
				}
				publisher.publishEvent(new BeforeDeleteEvent(one));

				invoker.invokeDelete(i);
			} catch (EmptyResultDataAccessException | ResourceNotFoundException ignored) {
			}
		});

		return noContent();
	}

	/*
	 * 得到Link
	 */
	private Link getSelfRelLink(RootResourceInformation resourceInformation, Object id) {
		return getBaseLinkBuilder(resourceInformation.getPathName()).slash(id).withSelfRel();
	}

}
