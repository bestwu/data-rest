package cn.bestwu.framework.data.validator;

import cn.bestwu.framework.util.CellUtil;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * {@code ChinaCell} 验证器
 *
 * @author Peter wu
 */
public class ChinaCellValidator implements ConstraintValidator<ChinaCell, String> {
	public ChinaCellValidator() {
	}

	@Override public void initialize(ChinaCell constraintAnnotation) {
	}

	public boolean isValid(String charSequence, ConstraintValidatorContext constraintValidatorContext) {
		return CellUtil.isChinaCell(charSequence);
	}
}

