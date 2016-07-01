package cn.bestwu.framework.rest.support;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.JsonView;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 一般资源
 *
 * @author Peter Wu
 */
@XmlRootElement
@JsonRootName("xml")
public class Resource<T> {

	@JsonUnwrapped
	@XmlAnyElement
	@JsonView(Object.class)
	private T content;

	public Resource() {
	}

	public Resource(T content) {
		this.content = content;
	}

	public T getContent() {
		return content;
	}

	public void setContent(T content) {
		this.content = content;
	}
}
