package cn.bestwu.framework.rest.support;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class Log implements Serializable {
	private static final long serialVersionUID = 1L;

	private String ipAddress;
	private String principalName;
	private String requestMethod;
	private String requestSignature;
	private String servletPath;

	private String requestHeaders;
	private String parameters;

}