package cn.bestwu.framework;

import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.lang.annotation.*;

/**
 * 启用自定义DataRest框架
 *
 * @author Peter Wu
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@EnableJpaAuditing
@Import(DataRestConfiguration.class)
public @interface EnableDataRest {
}
