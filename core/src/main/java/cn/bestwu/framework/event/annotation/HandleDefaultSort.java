package cn.bestwu.framework.event.annotation;

import java.lang.annotation.*;

/**
 * 处理实体列表查询默认排序
 *
 * @author Peter Wu
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface HandleDefaultSort {

	/**
	 * @return 实体类型
	 */
	Class<?> value();
}
