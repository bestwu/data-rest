package cn.bestwu.framework.data.annotation;

import java.lang.annotation.*;

/**
 * 高亮属性
 *
 * @author Peter Wu
 */
@Target({ ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface HighLight {

}
