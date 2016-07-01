package cn.bestwu.framework.data.annotation;

import org.springframework.http.HttpMethod;

import java.lang.annotation.*;

/**
 * 注解在repositoryInterface上表示所有接口都可用，
 * repositoryInterface上未注解，根据方法上的cn.bestwu.framework.data.annotation.SupportedHttpMethods注解映射可用接口
 *
 * @author Peter Wu
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RepositoryRestResource {

	/**
	 * @return 是否自动引出接口
	 */
	boolean exported() default true;

	/**
	 * {}表示支持注解的方法可映射的所有方法
	 *
	 * @return supportedHttpMethods
	 */
	HttpMethod[] value() default {};

}