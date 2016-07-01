package cn.bestwu.framework.event.annotation;

import java.lang.annotation.*;

/**
 * 处理QueryDsl查询默认Predicate
 *
 * @author Peter Wu
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface HandleDefaultPredicate {

	/**
	 * @return 实体类型
	 */
	Class<?> value();
}
