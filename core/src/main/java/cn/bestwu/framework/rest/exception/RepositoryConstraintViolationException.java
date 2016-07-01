package cn.bestwu.framework.rest.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.Errors;

/**
 * 验证错误
 *
 * @author Peter Wu
 */
public class RepositoryConstraintViolationException extends DataIntegrityViolationException {

	private static final long serialVersionUID = -4789377071564956366L;

	private final Errors errors;

	public RepositoryConstraintViolationException(Errors errors) {
		super("Validation failed");
		this.errors = errors;
	}

	public Errors getErrors() {
		return errors;
	}

}
