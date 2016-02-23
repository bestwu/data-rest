package cn.bestwu.framework;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用自定义DataRest框架
 *
 * @author Peter Wu
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(DataRestConfiguration.class)
public @interface EnableDataRest {
}
