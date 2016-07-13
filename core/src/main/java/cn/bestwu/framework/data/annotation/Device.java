package cn.bestwu.framework.data.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * 客户端设备信息注入，用于注释数据模型中的设备信息字段，类似{@code CreatedDate}
 *
 * @author Peter Wu
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = { FIELD, METHOD, ANNOTATION_TYPE })
public @interface Device {
}