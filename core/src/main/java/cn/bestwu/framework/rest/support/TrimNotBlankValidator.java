package cn.bestwu.framework.rest.support;

import cn.bestwu.framework.rest.annotation.TrimNotBlank;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * {@code TrimNotBlank} 验证器
 *
 * @author Peter wu
 */
public class TrimNotBlankValidator implements ConstraintValidator<TrimNotBlank, CharSequence> {
	public TrimNotBlankValidator() {
	}

	@Override public void initialize(TrimNotBlank constraintAnnotation) {

	}

	public boolean isValid(CharSequence charSequence, ConstraintValidatorContext constraintValidatorContext) {
		return charSequence == null || StringUtils.hasText(charSequence);
	}
}

