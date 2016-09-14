package cn.bestwu.framework.event.annotation;

import java.lang.annotation.*;

/**
 * 处理实体全文搜索前的操作，主要为增加默认搜索条件
 *
 * @author Peter Wu
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface HandleQueryBuilder {

	/**
	 * @return 实体类型
	 */
	Class<?> value();
}
