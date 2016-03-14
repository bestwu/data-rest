package cn.bestwu.framework.data.annotation;

import java.lang.annotation.*;

/**
 * 注解在repositoryInterface上表示所有接口都可用，
 * repositoryInterface上未注解，根据方法上的cn.bestwu.framework.data.annotation.SupportedHttpMethods注解映射可用接口
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RepositoryRestResource {

	/**
	 * HTTP GET method
	 */
	String GET = "GET";
	/**
	 * HTTP POST method
	 */
	String POST = "POST";
	/**
	 * HTTP PUT method
	 */
	String PUT = "PUT";
	/**
	 * HTTP DELETE method
	 */
	String DELETE = "DELETE";
	/**
	 * HTTP HEAD method
	 */
	String HEAD = "HEAD";
	/**
	 * HTTP OPTIONS method
	 */
	String OPTIONS = "OPTIONS";

	/**
	 * @return 是否自动引出接口
	 */
	boolean exported() default true;

	/**
	 * @return 是否以包含方式使用此注解
	 */
	boolean contained() default false;

	/**
	 * {}表示支持注解的方法可映射的所有方法
	 *
	 * @return supportedHttpMethods
	 */
	String[] value() default {};

	/**
	 * The path segment under which this resource is to be exported.
	 *
	 * @return A valid path segment.
	 */
	String pathName() default "";

}