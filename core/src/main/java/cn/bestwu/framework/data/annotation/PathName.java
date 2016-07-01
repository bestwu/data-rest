package cn.bestwu.framework.data.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Resource PathName
 * 实体对应path名称
 *
 * @author Peter Wu
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PathName {
	/**
	 * @return 名称
	 */
	String value();
}
