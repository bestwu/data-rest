package cn.bestwu.framework.event.annotation;

import java.lang.annotation.*;

/**
 * 处理增加QueryDsl Predicate的注解
 *
 * @author Peter Wu
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface HandleAddPredicate {

	/**
	 * @return 实体类型
	 */
	Class<?> value();
}
