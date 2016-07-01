package cn.bestwu.framework.event.annotation;

import java.lang.annotation.*;

/**
 * 处理删除实体后有关联实体的操作
 *
 * @author Peter Wu
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface HandleAfterLinkDelete {

}
