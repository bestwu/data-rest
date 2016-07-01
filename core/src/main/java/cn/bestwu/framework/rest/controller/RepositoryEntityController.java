package cn.bestwu.framework.rest.controller;

import cn.bestwu.api.sign.ApiSign;
import cn.bestwu.framework.event.*;
import cn.bestwu.framework.rest.annotation.RepositoryRestController;
import cn.bestwu.framework.rest.exception.ResourceNotFoundException;
import cn.bestwu.framework.rest.mapping.VersionRepositoryRestRequestMappingHandlerMapping;
import cn.bestwu.framework.rest.support.ETag;
import cn.bestwu.framework.rest.support.PersistentEntityResource;
import cn.bestwu.framework.rest.support.ResourceType;
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
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Entity 模型 基本控制类
 * 对应资源的CRUD操作API
 *
 * @author Peter Wu
 */
@ApiSign
@RepositoryRestController
@RequestMapping(value = BaseController.BASE_URI) public class RepositoryEntityController extends BaseController {

	private static final String LINK_HEADER = "Link";
	private static final List<String> ACCEPT_PATCH_HEADERS = Collections.singletonList(//
			MediaType.APPLICATION_JSON_VALUE);

	/**
	 * {@code HEAD /{repsoitory}}
	 *
	 * @param resourceInformation resourceInformation
	 * @return ResponseEntity
	 */
	@RequestMapping(method = RequestMethod.HEAD)
	public ResponseEntity<?> headCollectionResource(RootResourceInformation resourceInformation) {
		HttpHeaders headers = new HttpHeaders();
		headers.add(LINK_HEADER, getBaseLinkBuilder(resourceInformation.getPathName()).withSelfRel().toString());

		return new ResponseEntity<>(headers, HttpStatus.NO_CONTENT);
	}

	/**
	 * {@code HEAD /{repsoitory}/{id}}
	 *
	 * @param resourceInformation resourceInformation
	 * @param id                  id
	 * @return ResponseEntity
	 */
	@RequestMapping(value = ID_URI, method = RequestMethod.HEAD)
	public ResponseEntity<?> headForItemResource(RootResourceInformation resourceInformation, @PathVariable String id) {

		Object domainObject = getItemResource(resourceInformation, id);

		HttpHeaders headers = prepareHeaders(resourceInformation.getEntity(), domainObject);
		headers.add(LINK_HEADER, getBaseLinkBuilder(resourceInformation.getPathName()).slash(id).withSelfRel().toString());

		return new ResponseEntity<>(headers, HttpStatus.NO_CONTENT);
	}

	/**
	 * {@code OPTIONS /{repository}}.
	 *
	 * @param information information
	 * @return ResponseEntity
	 */
	@RequestMapping(method = RequestMethod.OPTIONS)
	public ResponseEntity<?> optionsForCollectionResource(RootResourceInformation information) {

		HttpHeaders headers = new HttpHeaders();
		Set<HttpMethod> supportedMethods = information.getResourceMetadata().getSupportedHttpMethods(ResourceType.COLLECTION);

		headers.setAllow(supportedMethods);

		return new ResponseEntity<>(headers, HttpStatus.OK);
	}

	/**
	 * {@code OPTIONS /{repository}/{id}}.
	 *
	 * @param information information
	 * @return ResponseEntity
	 */
	@RequestMapping(value = ID_URI, method = RequestMethod.OPTIONS)
	public ResponseEntity<?> optionsForItemResource(RootResourceInformation information) {

		HttpHeaders headers = new HttpHeaders();
		Set<HttpMethod> supportedMethods = information.getResourceMetadata().getSupportedHttpMethods(ResourceType.ITEM);

		headers.setAllow(supportedMethods);
		headers.put("Accept-Patch", ACCEPT_PATCH_HEADERS);

		return new ResponseEntity<>(headers, HttpStatus.OK);
	}

	/**
	 * Repository方法搜索
	 *
	 * @param resourceInformation resourceInformation
	 * @param parameters          parameters
	 * @param search              search
	 * @param pageable            pageable
	 * @return return
	 * @throws NoSuchMethodException NoSuchMethodException
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

	/**
	 * 查看资源列表
	 *
	 * @param resourceInformation resourceInformation
	 * @param pageable            分页参数
	 * @param all                 是否返回所有结果
	 * @return return
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

	/**
	 * 查看单个资源
	 *
	 * @param resourceInformation resourceInformation
	 * @param id                  id
	 * @return return
	 */
	@RequestMapping(value = ID_URI, method = RequestMethod.GET)
	public Object show(RootResourceInformation resourceInformation, @PathVariable String id) {
		Object content = getItemResource(resourceInformation, id);

		publisher.publishEvent(new BeforeShowEvent(content));

		return ok(new PersistentEntityResource<>(content, resourceInformation.getEntity()));
	}

	/**
	 * @param resourceInformation resourceInformation
	 * @param id                  id
	 * @return 单个资源
	 */
	private Object getItemResource(RootResourceInformation resourceInformation, @PathVariable String id) {
		RepositoryInvoker invoker = resourceInformation.getInvoker();

		Object content = invoker.invokeFindOne(id);

		if (content == null) {
			throw new ResourceNotFoundException();
		}

		return content;
	}

	/**
	 * 创建资源
	 *
	 * @param resourceInformation resourceInformation
	 * @param resource            要创建的资源
	 * @return return
	 */
	@RequestMapping(method = RequestMethod.POST)
	public Object create(RootResourceInformation resourceInformation, @Valid PersistentEntityResource resource) {
		RepositoryInvoker invoker = resourceInformation.getInvoker();

		Object content = resource.getContent();

		publisher.publishEvent(new BeforeCreateEvent(content));
		invoker.invokeSave(content);
		publisher.publishEvent(new AfterCreateEvent(content));

		return created(resource);
	}

	/**
	 * 修改资源
	 *
	 * @param resourceInformation resourceInformation
	 * @param resource            resource
	 * @param id                  id
	 * @param eTag                eTag
	 * @return return
	 */
	@RequestMapping(value = ID_URI, method = RequestMethod.PUT)
	public Object update(RootResourceInformation resourceInformation, @Valid PersistentEntityResource resource, @PathVariable String id, ETag eTag) {
		RepositoryInvoker invoker = resourceInformation.getInvoker();

		Object content = resource.getContent();

		eTag.verify(resourceInformation.getEntity(), resourceInformation.getModelType());

		publisher.publishEvent(new BeforeSaveEvent(content));
		invoker.invokeSave(content);
		publisher.publishEvent(new AfterSaveEvent(content));

		publisher.publishEvent(new AfterLinkSaveEvent(content, getOldModel()));

		return updated(resource);
	}

	/**
	 * 删除资源
	 *
	 * @param resourceInformation resourceInformation
	 * @param id                  id
	 * @return return
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
		publisher.publishEvent(new AfterDeleteEvent(one));

		return noContent();
	}

	/**
	 * 删除多个资源
	 *
	 * @param resourceInformation resourceInformation
	 * @param id                  id
	 * @return return
	 * @throws IllegalAccessException IllegalAccessException
	 * @throws InstantiationException InstantiationException
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
				publisher.publishEvent(new AfterDeleteEvent(one));
			} catch (EmptyResultDataAccessException | ResourceNotFoundException ignored) {
			}
		});

		return noContent();
	}

}
