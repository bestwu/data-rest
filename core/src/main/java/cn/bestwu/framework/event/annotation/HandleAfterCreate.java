package cn.bestwu.framework.event.annotation;

import java.lang.annotation.*;

/**
 * 处理创建实体成功后操作的注解
 *
 * @author Peter Wu
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface HandleAfterCreate {

}
