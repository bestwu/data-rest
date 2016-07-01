package cn.bestwu.framework.event.annotation;

import java.lang.annotation.*;

/**
 * 处理查询实体结果中单个实体的操作
 *
 * @author Peter Wu
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface HandleItemResource {
}
