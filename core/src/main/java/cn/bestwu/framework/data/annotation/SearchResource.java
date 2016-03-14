package cn.bestwu.framework.data.annotation;

import java.lang.annotation.*;

@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface SearchResource {

	/**
	 * The path segment under which this resource is to be exported.
	 *
	 * @return A valid path segment.
	 */
	String value() default "";

}