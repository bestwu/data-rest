package cn.bestwu.framework.event.annotation;

import java.lang.annotation.*;

/**
 * 处理创建新实体前的操作
 *
 * @author Peter Wu
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface HandleBeforeCreate {

}
