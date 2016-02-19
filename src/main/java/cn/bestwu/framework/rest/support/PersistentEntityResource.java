package cn.bestwu.framework.rest.support;

import cn.bestwu.framework.util.ArrayUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.hateoas.Link;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Peter Wu
 */
public class PersistentEntityResource<T> extends Resource<T> {

	@JsonIgnore
	private PersistentEntity<?, ?> entity;
	@JsonView(Object.class)
	private Map<String, String> links;

	public PersistentEntityResource(T content, PersistentEntity<?, ?> entity, Link... links) {
		super(content);

		Assert.notNull(entity);

		if (ArrayUtil.isNotEmpty(links)) {
			this.links = new HashMap<>();
			Arrays.stream(links).forEach(link -> this.links.put(link.getRel(), link.getHref()));
		}
		this.entity = entity;
	}

	private PersistentEntityResource(T content, PersistentEntity<?, ?> entity, Map<String, String> links) {
		super(content);
		this.links = links;
		this.entity = entity;
	}

	//--------------------------------------------
	public PersistentEntity<?, ?> getEntity() {
		return entity;
	}

	public void setEntity(PersistentEntity<?, ?> entity) {
		this.entity = entity;
	}

	public Map<String, String> getLinks() {
		return links;
	}

	//--------------------------------------------
	public void add(Link... links) {
		if (this.links == null) {
			this.links = new HashMap<>();
		}
		Arrays.stream(links).forEach(link -> this.links.put(link.getRel(), link.getHref()));
	}

	public <S> PersistentEntityResource<S> map(S content) {
		return new PersistentEntityResource<>(content, entity, links);
	}

}
