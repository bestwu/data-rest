package cn.bestwu.framework.data.annotation;

import java.lang.annotation.*;

@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RepositoryRestResource {

	/**
	 * Flag indicating whether this resource is exported at all.
	 *
	 * @return {@literal true} if the resource is to be exported, {@literal false} otherwise.
	 */
	boolean value() default true;

	/**
	 * The path segment under which this resource is to be exported.
	 *
	 * @return A valid path segment.
	 */
	String pathName() default "";

}