package cn.bestwu.framework.rest.support;

import cn.bestwu.framework.event.ResourceAddLinkEvent;
import cn.bestwu.framework.rest.controller.RepositoryEntityController;
import cn.bestwu.framework.util.Sha1DigestUtil;
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
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;

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
	@Autowired
	private AuditableBeanWrapperFactory auditableBeanWrapperFactory;
	@Autowired
	private Repositories repositories;
	@Autowired
	protected ApplicationEventPublisher publisher;

	protected PersistentEntity<?, ?> getPersistentEntity(Class<?> modelType) {
		return repositories.getPersistentEntity(modelType);
	}

	/**
	 * Retruns the default headers to be returned for the given {@link PersistentEntity} and value. Will set {@link ETag}
	 * and {@code Last-Modified} headers if applicable.
	 *
	 * @param entity must not be {@literal null}.
	 * @param value  must not be {@literal null}.
	 * @return HttpHeaders
	 */
	private HttpHeaders prepareHeaders(PersistentEntity<?, ?> entity, Object value) {

		// Add ETag
		HttpHeaders headers = ETag.from(entity, value).addTo(new HttpHeaders());

		// Add Last-Modified
		AuditableBeanWrapper wrapper = getAuditableBeanWrapper(value);

		if (wrapper == null) {
			return headers;
		}

		Calendar lastModifiedDate = wrapper.getLastModifiedDate();

		if (lastModifiedDate != null) {
			headers.setLastModified(lastModifiedDate.getTimeInMillis());
		}

		return headers;
	}

	private static HttpHeaders noCache() {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, max-age=0, must-revalidate");
		headers.add(HttpHeaders.PRAGMA, "no-cache");
		headers.add(HttpHeaders.EXPIRES, "-1");
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
		if (supportClientCache)
			return ResponseEntity.created(URI.create(entityResource.getLinks().get(Link.REL_SELF))).headers(prepareHeaders(entityResource.getEntity(), entityResource.getContent()))
					.body(entityResource);
		else
			return ResponseEntity.created(URI.create(entityResource.getLinks().get(Link.REL_SELF))).headers(noCache()).body(entityResource);
	}

	/**
	 * 成功更新资源
	 *
	 * @param entityResource entityResource
	 * @return 200 ResponseEntity
	 */
	protected ResponseEntity updated(PersistentEntityResource<?> entityResource) {
		if (supportClientCache)
			return ResponseEntity.ok().location(URI.create(entityResource.getLinks().get(Link.REL_SELF))).headers(prepareHeaders(entityResource.getEntity(), entityResource.getContent()))
					.body(entityResource);
		else
			return ResponseEntity.ok().location(URI.create(entityResource.getLinks().get(Link.REL_SELF))).headers(noCache()).body(entityResource);
	}

	/**
	 * @param object object
	 * @return 200 ResponseEntity
	 */
	protected ResponseEntity ok(Object object) {
		if (supportClientCache)
			return ResponseEntity.ok(object);
		else
			return ResponseEntity.ok().headers(noCache()).body(object);
	}

	/**
	 * 给Pageable 的各资源加自描述链接信息
	 *
	 * @param resource resource
	 * @return ResponseEntity
	 */
	protected ResponseEntity ok(PersistentEntityResource<?> resource) {
		Object source = resource.getContent();
		Assert.notNull(source);
		if (source instanceof Page<?>) {
			Page<?> page = (Page<?>) source;
			List<?> content = page.getContent();
			if (!content.isEmpty()) {
				ResourceConverter converter = getResourceConverter(resource);

				page = page.map(converter);
				ResponseEntity.BodyBuilder bodyBuilder = converter.getBodyBuilder();
				return bodyBuilder.body(resource.map(page));
			}
		} else if (source instanceof Iterable<?>) {
			Iterable<?> iterable = (Iterable<?>) source;
			if (iterable.iterator().hasNext()) {
				ResourceConverter converter = getResourceConverter(resource);

				List<PersistentEntityResource<?>> newContent = new ArrayList<>();
				iterable.forEach(object -> newContent.add(converter.convert(object)));
				ResponseEntity.BodyBuilder bodyBuilder = converter.getBodyBuilder();
				return bodyBuilder.body(resource.map(newContent));
			}
		} else {
			if (supportClientCache)
				return ResponseEntity.ok().headers(prepareHeaders(resource.getEntity(), resource.getContent())).body(resource);
			else
				return ResponseEntity.ok().headers(noCache()).body(resource);

		}

		if (supportClientCache)
			return ResponseEntity.ok(resource);
		else
			return ResponseEntity.ok().headers(noCache()).body(resource);
	}

	/**
	 * @param resource resource
	 * @return 资源转换器
	 */
	private ResourceConverter getResourceConverter(PersistentEntityResource<?> resource) {
		PersistentEntity<?, ?> entity = resource.getEntity();
		String repositoryBasePathName = ResourceUtil.getRepositoryBasePathName(entity.getType());
		ControllerLinkBuilder baseLinkBuilder = getBaseLinkBuilder(repositoryBasePathName);
		return new ResourceConverter(entity, baseLinkBuilder);
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

	protected Object getId(PersistentEntity<?, ?> persistentEntity, Object entity) {
		return persistentEntity.getIdentifierAccessor(entity).getIdentifier();
	}

	protected Object getId(Object entity) {
		return getPersistentEntity(entity.getClass()).getIdentifierAccessor(entity).getIdentifier();
	}

	protected ResponseEntity noContent() {
		return ResponseEntity.noContent().build();
	}

	/**
	 * 结果转换器
	 *
	 * @author Peter Wu
	 */
	public class ResourceConverter implements Converter<Object, PersistentEntityResource<?>> {

		private List<String> eTagValues = new ArrayList<>();
		private List<Long> LastModifieds = new ArrayList<>();

		private boolean hasEtag = true;
		private boolean hasLastModified = true;

		private PersistentEntity<?, ?> persistentEntity = null;
		private ControllerLinkBuilder baseLinkBuilder = null;

		public ResourceConverter(PersistentEntity<?, ?> persistentEntity, ControllerLinkBuilder baseLinkBuilder) {
			this.persistentEntity = persistentEntity;
			this.baseLinkBuilder = baseLinkBuilder;
		}

		@Override public PersistentEntityResource<?> convert(Object source) {
			if (supportClientCache) {
				if (hasEtag) {
					String eTagValue = ETag.getETagValue(persistentEntity, source);
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
							LastModifieds.add(lastModifiedDate.getTimeInMillis());
						}
					}
				}
			}

			Link link = baseLinkBuilder.slash(getId(persistentEntity, source)).withSelfRel();
			PersistentEntityResource<Object> resource = new PersistentEntityResource<>(source, persistentEntity, link);
			publisher.publishEvent(new ResourceAddLinkEvent(source, resource));
			return resource;
		}

		public ResponseEntity.BodyBuilder getBodyBuilder() {
			if (supportClientCache) {
				HttpHeaders httpHeaders = new HttpHeaders();
				if (hasEtag) {
					String eTagValue = "";
					int size = eTagValues.size();
					for (int i = 0; i < size; i++) {
						eTagValue = eTagValue.concat(eTagValues.get(i));
						if (i != size - 1)
							eTagValue = eTagValue.concat("|");
					}
					eTagValue = "\"".concat(Sha1DigestUtil.shaHex(eTagValue)).concat("\"");

					httpHeaders.setETag(eTagValue);
				}
				if (hasLastModified) {
					Collections.sort(LastModifieds, (x, y) -> (x < y) ? 1 : ((Objects.equals(x, y)) ? 0 : -1));
					httpHeaders.setLastModified(LastModifieds.get(0));
				}

				return ResponseEntity.ok().headers(httpHeaders);
			} else
				return ResponseEntity.ok().headers(noCache());
		}
	}
}
