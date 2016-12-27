package cn.bestwu.framework.rest.support;

import cn.bestwu.framework.data.annotation.DisableSelfRel;
import cn.bestwu.framework.event.ItemResourceEvent;
import cn.bestwu.framework.rest.controller.RepositoryEntityController;
import cn.bestwu.framework.util.ResourceUtil;
import cn.bestwu.lang.util.Sha1DigestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.auditing.AuditableBeanWrapper;
import org.springframework.data.auditing.AuditableBeanWrapperFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.repository.support.Repositories;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.*;

/**
 * 响应客户端
 *
 * @author Peter Wu
 */

public class Response {

	@Value("${server.client-cache:true}")
	private boolean supportClientCache;
	@Value("${server.exact:false}")
	protected boolean exact;
	@Autowired
	private AuditableBeanWrapperFactory auditableBeanWrapperFactory;
	@Autowired
	private Repositories repositories;
	@Autowired
	protected ApplicationEventPublisher publisher;
	@Autowired(required = false)
	protected HttpServletRequest request;

	protected PersistentEntity<?, ?> getPersistentEntity(Class<?> domainType) {
		return repositories.getPersistentEntity(domainType);
	}

	/**
	 * Retruns the default headers to be returned for the given {@link PersistentEntity} and value. Will set {@link ETag}
	 * and {@code Last-Modified} headers if applicable.
	 *
	 * @param entity must not be {@literal null}.
	 * @param value  must not be {@literal null}.
	 * @return HttpHeaders
	 */
	protected HttpHeaders prepareHeaders(PersistentEntity<?, ?> entity, Object value) {

		// Add ETag
		HttpHeaders headers = ETag.from(entity, value, exact).addTo(new HttpHeaders());

		// Add Last-Modified
		AuditableBeanWrapper wrapper = getAuditableBeanWrapper(value);

		if (wrapper == null) {
			return headers;
		}

		Calendar lastModifiedDate = wrapper.getLastModifiedDate();

		if (lastModifiedDate != null) {
			headers.setLastModified(lastModifiedDate.getTimeInMillis());
		}
		return cacheControl(headers);
	}

	/**
	 * @return 不支持客户端缓存，不支持客户端保存数据的响应头
	 */
	public static HttpHeaders noCache() {
		HttpHeaders headers = new HttpHeaders();
		headers.setCacheControl("no-cache, no-store, max-age=0, must-revalidate");
		headers.setPragma("no-cache");
		headers.setExpires(-1);
		return headers;
	}

	/**
	 * @param headers 响应头
	 * @return 不支持支持客户端缓存，支持客户端保存数据的响应头，即支持必须revalidate的缓存
	 */
	public static HttpHeaders cacheControl(HttpHeaders headers) {
		if (headers == null) {
			headers = new HttpHeaders();
		}
		headers.setCacheControl("no-cache, must-revalidate");
		headers.setPragma("no-cache");
		headers.setExpires(-1);
		return headers;
	}

	/**
	 * Returns the {@link AuditableBeanWrapper} for the given source.
	 *
	 * @param source can be {@literal null}.
	 * @return AuditableBeanWrapper
	 */
	private AuditableBeanWrapper getAuditableBeanWrapper(Object source) {
		return auditableBeanWrapperFactory.getBeanWrapperFor(source);
	}

	/**
	 * 成功创建资源
	 *
	 * @param entityResource entityResource
	 * @return 201 ResponseEntity
	 */
	protected ResponseEntity created(PersistentEntityResource<?> entityResource) {
		itemResourceHandle(entityResource);
		ResponseEntity.BodyBuilder created;
		if (noSelfRel(entityResource)) {
			created = ResponseEntity.status(HttpStatus.CREATED);
		} else {
			created = ResponseEntity.created(URI.create(entityResource.getLinks().get(Link.REL_SELF)));
		}

		if (supportClientCache)
			return created.headers(prepareHeaders(entityResource.getEntity(), entityResource.getContent()))
					.body(entityResource);
		else
			return created.headers(noCache()).body(entityResource);
	}

	/**
	 * 成功更新资源
	 *
	 * @param entityResource entityResource
	 * @return 200 ResponseEntity
	 */
	protected ResponseEntity updated(PersistentEntityResource<?> entityResource) {
		itemResourceHandle(entityResource);
		ResponseEntity.BodyBuilder location = ResponseEntity.ok();
		if (!noSelfRel(entityResource)) {
			location.location(URI.create(entityResource.getLinks().get(Link.REL_SELF)));
		}
		if (supportClientCache)
			return location.headers(prepareHeaders(entityResource.getEntity(), entityResource.getContent()))
					.body(entityResource);
		else
			return location.headers(noCache()).body(entityResource);
	}

	/**
	 * 不支持客户端缓存
	 *
	 * @param object object
	 * @return 200 ResponseEntity
	 */
	protected ResponseEntity noCache(Object object) {
		return ok(object, false);
	}

	/**
	 * 支持客户端缓存
	 *
	 * @param object object
	 * @return 200 ResponseEntity
	 */
	protected ResponseEntity ok(Object object) {
		return ok(object, supportClientCache);
	}

	/**
	 * 给Pageable 的各资源加自描述链接信息
	 *
	 * @param resource           resource
	 * @param supportClientCache supportClientCache
	 * @return 200 ResponseEntity
	 */
	private ResponseEntity ok(Object resource, boolean supportClientCache) {
		if ("HEAD".equals(ResourceUtil.REQUEST_METHOD.get())) {
			HttpHeaders headers = new HttpHeaders();
			headers.add("Link", request.getRequestURL().toString());
			return new ResponseEntity<>(headers, HttpStatus.NO_CONTENT);
		}
		if (resource instanceof PersistentEntityResource) {
			PersistentEntityResource<?> persistentEntityResource = (PersistentEntityResource<?>) resource;
			PersistentEntity<?, ?> persistentEntity = persistentEntityResource.getEntity();
			Object source = persistentEntityResource.getContent();
			Assert.notNull(source);
			itemResourceHandle(persistentEntityResource);

			if (source instanceof Page<?>) {
				Page<?> page = (Page<?>) source;
				List<?> content = page.getContent();
				if (!content.isEmpty()) {
					ResourceConverter converter = getResourceConverter(persistentEntityResource, supportClientCache);

					page = page.map(converter);
					ResponseEntity.BodyBuilder bodyBuilder = converter.getBodyBuilder();
					return bodyBuilder.body(persistentEntityResource.map(page));
				}
			} else if (source instanceof Iterable<?>) {
				Iterable<?> iterable = (Iterable<?>) source;
				Iterator<?> iterator = iterable.iterator();
				if (iterator.hasNext()) {
					ResourceConverter converter = getResourceConverter(persistentEntityResource, supportClientCache);

					List<PersistentEntityResource<?>> newContent = new ArrayList<>();
					while (iterator.hasNext()){
						newContent.add(converter.convert(iterator.next()));
					}
					ResponseEntity.BodyBuilder bodyBuilder = converter.getBodyBuilder();
					return bodyBuilder.body(persistentEntityResource.map(newContent));
				}
			} else {
				if (supportClientCache)
					return ResponseEntity.ok().headers(prepareHeaders(persistentEntity, source)).body(resource);
				else
					return ResponseEntity.ok().headers(noCache()).body(resource);
			}
		}

		if (supportClientCache)
			return ResponseEntity.ok().headers(prepareHeaders(null, resource)).body(resource);
		else
			return ResponseEntity.ok().headers(noCache()).body(resource);
	}

	/**
	 * @param resource 待处理的资源
	 */
	private void itemResourceHandle(PersistentEntityResource<?> resource) {
		PersistentEntity<?, ?> persistentEntity = resource.getEntity();
		Object content = resource.getContent();

		if (content instanceof Iterable<?>) {
			if (noSelfRel(resource))
				resource.add(getBaseLinkBuilder(ResourceUtil.getRepositoryBasePathName(persistentEntity.getType())).withSelfRel());
		} else {
			publisher.publishEvent(new ItemResourceEvent(content, resource));
			if (!persistentEntity.getType().isAnnotationPresent(DisableSelfRel.class) && noSelfRel(resource)) {
				resource.add(getBaseLinkBuilder(ResourceUtil.getRepositoryBasePathName(persistentEntity.getType())).slash(getId(persistentEntity, content)).withSelfRel());
			}
		}
	}

	/**
	 * @param resource 资源
	 * @return 是否没有自身REL
	 */
	private boolean noSelfRel(PersistentEntityResource<?> resource) {
		return resource.getLinks() == null || resource.getLinks().get(Link.REL_SELF) == null;
	}

	/**
	 * @param content 资源内容
	 * @return 自身链接
	 */
	protected Link getEntitySelfLink(Object content) {
		Class<?> sourceClass = content.getClass();
		if (ClassUtils.isCglibProxy(content)) {
			sourceClass = sourceClass.getSuperclass();
		}
		PersistentEntity<?, ?> persistentEntity = getPersistentEntity(sourceClass);
		return getBaseLinkBuilder(ResourceUtil.getRepositoryBasePathName(persistentEntity.getType())).slash(getId(persistentEntity, content)).withSelfRel();
	}

	/**
	 * @param resource           resource
	 * @param supportClientCache supportClientCache
	 * @return 资源转换器
	 */
	private ResourceConverter getResourceConverter(PersistentEntityResource<?> resource, boolean supportClientCache) {
		PersistentEntity<?, ?> entity = resource.getEntity();
		String repositoryBasePathName = ResourceUtil.getRepositoryBasePathName(entity.getType());
		ControllerLinkBuilder baseLinkBuilder = getBaseLinkBuilder(repositoryBasePathName);
		return new ResourceConverter(supportClientCache, entity, baseLinkBuilder);
	}

	/**
	 * 得到基本LinkBuilder
	 *
	 * @param resourceName 资源名
	 * @return 得到基本LinkBuilder
	 */
	protected ControllerLinkBuilder getBaseLinkBuilder(String resourceName) {
		return ControllerLinkBuilder.linkTo(RepositoryEntityController.class, resourceName);
	}

	/**
	 * @param persistentEntity persistentEntity
	 * @param entity           entity
	 * @return 实体ID
	 */
	protected Object getId(PersistentEntity<?, ?> persistentEntity, Object entity) {
		return persistentEntity.getIdentifierAccessor(entity).getIdentifier();
	}

	/**
	 * 响应空白内容
	 *
	 * @return 204
	 */
	protected ResponseEntity noContent() {
		return ResponseEntity.noContent().build();
	}

	/**
	 * 结果转换器
	 *
	 * @author Peter Wu
	 */
	private class ResourceConverter implements Converter<Object, PersistentEntityResource<?>> {

		private List<String> eTagValues = new ArrayList<>();
		private List<Long> lastModifieds = new ArrayList<>();

		private boolean hasEtag = true;
		private boolean hasLastModified = true;
		private final boolean supportClientCache;

		private PersistentEntity<?, ?> persistentEntity = null;
		private ControllerLinkBuilder baseLinkBuilder = null;

		public ResourceConverter(boolean supportClientCache, PersistentEntity<?, ?> persistentEntity, ControllerLinkBuilder baseLinkBuilder) {
			this.supportClientCache = supportClientCache;
			this.persistentEntity = persistentEntity;
			this.baseLinkBuilder = baseLinkBuilder;
		}

		@Override public PersistentEntityResource<?> convert(Object source) {
			if (supportClientCache) {
				if (hasEtag) {
					String eTagValue = ETag.getETagValue(persistentEntity, source, exact);
					if (eTagValue == null) {
						hasEtag = false;
					} else {
						eTagValues.add(eTagValue);
					}
				}

				// Add Last-Modified
				if (hasLastModified) {
					AuditableBeanWrapper wrapper = getAuditableBeanWrapper(source);
					if (wrapper == null) {
						hasLastModified = false;
					} else {
						Calendar lastModifiedDate = wrapper.getLastModifiedDate();
						if (lastModifiedDate == null) {
							hasLastModified = false;
						} else {
							lastModifieds.add(lastModifiedDate.getTimeInMillis());
						}
					}
				}
			}

			PersistentEntityResource<Object> resource = new PersistentEntityResource<>(source, persistentEntity);
			publisher.publishEvent(new ItemResourceEvent(source, resource));
			if (!persistentEntity.getType().isAnnotationPresent(DisableSelfRel.class) && noSelfRel(resource)) {
				resource.add(baseLinkBuilder.slash(getId(persistentEntity, source)).withSelfRel());
			}
			return resource;
		}

		/**
		 * @return BodyBuilder
		 */
		public ResponseEntity.BodyBuilder getBodyBuilder() {
			if (supportClientCache) {
				HttpHeaders headers = new HttpHeaders();
				if (hasEtag) {
					String eTagValue = "";
					int size = eTagValues.size();
					for (int i = 0; i < size; i++) {
						eTagValue = eTagValue.concat(eTagValues.get(i));
						if (i != size - 1)
							eTagValue = eTagValue.concat("|");
					}
					eTagValue = "\"".concat(Sha1DigestUtil.shaHex(eTagValue)).concat("\"");

					headers.setETag(eTagValue);
				}
				if (hasLastModified) {
					Collections.sort(lastModifieds,new Comparator<Long>() {
						@Override public int compare(Long x, Long y) {
							return (x < y) ? 1 : ((Objects.equals(x, y)) ? 0 : -1);
						}
					});
					headers.setLastModified(lastModifieds.get(0));
				}

				return ResponseEntity.ok().headers(cacheControl(headers));
			} else
				return ResponseEntity.ok().headers(noCache());
		}
	}
}
