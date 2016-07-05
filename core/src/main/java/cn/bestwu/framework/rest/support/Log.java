package cn.bestwu.framework.rest.support;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 日志模型
 *
 * @author Peter Wu
 */
@Setter
@Getter
public class Log implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 请求客户端IP地址
	 */
	private String ipAddress;
	/**
	 * 客户端用户名
	 */
	private String principalName;
	/**
	 * 请求方法
	 */
	private String requestMethod;
	/**
	 * 请求接口签名
	 */
	private String requestSignature;
	/**
	 * 请求servletPath
	 */
	private String servletPath;

	/**
	 * 请求头
	 */
	private String requestHeaders;
	/**
	 * 请求参数，不含二进制流
	 */
	private String parameters;

	private Long createdDate = System.currentTimeMillis();

}