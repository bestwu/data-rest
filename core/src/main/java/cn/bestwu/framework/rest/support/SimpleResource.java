package cn.bestwu.framework.rest.support;

import cn.bestwu.framework.util.ArrayUtil;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.hateoas.Link;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 简单资源
 *
 * @author Peter Wu
 */
public class SimpleResource<T> extends Resource<T> {

	@JsonView(Object.class)
	private Map<String, String> links;

	public SimpleResource(T content, Link... links) {
		super(content);
		if (ArrayUtil.isNotEmpty(links)) {
			this.links = new HashMap<>();
			Arrays.stream(links).forEach(link -> this.links.put(link.getRel(), link.getHref()));
		}
	}

	protected SimpleResource(T content, Map<String, String> links) {
		super(content);
		this.links = links;
	}

	//--------------------------------------------

	/**
	 * @return 链接
	 */
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
}
