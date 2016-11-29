package cn.bestwu.framework.rest.support;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.hateoas.Link;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * 持久化实体资源
 *
 * @author Peter Wu
 */
public class PersistentEntityResource<T> extends SimpleResource<T> {

	private static final long serialVersionUID = 1009307069857374549L;
	@JsonIgnore
	private transient final PersistentEntity<?, ?> entity;

	public PersistentEntityResource(T content, PersistentEntity<?, ?> entity, Link... links) {
		super(content, links);
		Assert.notNull(entity);
		this.entity = entity;
	}

	private PersistentEntityResource(T content, PersistentEntity<?, ?> entity, Map<String, String> links) {
		super(content, links);
		this.entity = entity;
	}

	//--------------------------------------------
	public PersistentEntity<?, ?> getEntity() {
		return entity;
	}

	public <S> PersistentEntityResource<S> map(S content) {
		return new PersistentEntityResource<>(content, entity, getLinks());
	}

}
