package cn.bestwu.framework.data.annotation;

import java.lang.annotation.*;

/**
 * @author Peter Wu
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface SupportedHttpMethods {
	/**
	 * HTTP GET method
	 */
	String GET="GET";
	/**
	 * HTTP POST method
	 */
	String POST="POST";
	/**
	 * HTTP PUT method
	 */
	String PUT="PUT";
	/**
	 * HTTP DELETE method
	 */
	String DELETE="DELETE";
	/**
	 * HTTP HEAD method
	 */
	String HEAD="HEAD";
	/**
	 * HTTP OPTIONS method
	 */
	String OPTIONS="OPTIONS";

	String[] value();
}
