package cn.bestwu.framework.rest.annotation;

import cn.bestwu.framework.rest.support.TrimNotBlankValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.constraints.NotNull;
import java.lang.annotation.*;

/**
 * 不为空白验证
 * Created by Peter wu on 2015/3/6.
 *
 * @author Peter wu
 */
@Documented
@Constraint(
		validatedBy = { TrimNotBlankValidator.class }
)
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@ReportAsSingleViolation
@NotNull
public @interface TrimNotBlank {
	String message() default "{org.hibernate.validator.constraints.NotBlank.message}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER })
	@Retention(RetentionPolicy.RUNTIME)
	@Documented @interface List {
		TrimNotBlank[] value();
	}
}

