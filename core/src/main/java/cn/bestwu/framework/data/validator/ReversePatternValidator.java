package cn.bestwu.framework.data.validator;

import cn.bestwu.framework.data.validator.ReversePattern;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

/**
 * {@code ReversePattern} 的验证器
 *
 * @author Peter Wu
 */
public class ReversePatternValidator implements ConstraintValidator<ReversePattern, CharSequence> {

	private static final Log log = LoggerFactory.make();

	private java.util.regex.Pattern pattern;

	public void initialize(ReversePattern parameters) {
		ReversePattern.Flag[] flags = parameters.flags();
		int intFlag = 0;
		for (ReversePattern.Flag flag : flags) {
			intFlag = intFlag | flag.getValue();
		}

		try {
			pattern = java.util.regex.Pattern.compile(parameters.regexp(), intFlag);
		} catch (PatternSyntaxException e) {
			throw log.getInvalidRegularExpressionException(e);
		}
	}

	public boolean isValid(CharSequence value, ConstraintValidatorContext constraintValidatorContext) {
		if (value == null) {
			return true;
		}
		Matcher m = pattern.matcher(value);
		return !m.matches();
	}
}
