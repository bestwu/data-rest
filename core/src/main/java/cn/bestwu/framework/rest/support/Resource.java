package cn.bestwu.framework.rest.support;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.JsonView;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * 一般资源
 *
 * @author Peter Wu
 */
@XmlRootElement
@JsonRootName("xml")
public class Resource<T> implements Serializable {

	private static final long serialVersionUID = 7824159918137870439L;
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
