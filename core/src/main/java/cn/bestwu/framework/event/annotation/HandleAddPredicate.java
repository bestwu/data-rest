package cn.bestwu.framework.event.annotation;

import java.lang.annotation.*;

/**
 * @author Peter Wu
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface HandleAddPredicate {
	Class<?> value();
}
