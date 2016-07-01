package cn.bestwu.framework.event.annotation;

import java.lang.annotation.*;

/**
 * 处理修改实体后的操作
 *
 * @author Peter Wu
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface HandleAfterSave {

}
