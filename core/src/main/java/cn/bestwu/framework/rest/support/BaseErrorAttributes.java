package cn.bestwu.framework.rest.support;

import cn.bestwu.framework.rest.controller.BaseController;
import cn.bestwu.framework.rest.exception.ResourceNotFoundException;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

/**
 * ErrorAttributes 错误属性
 *
 * @author Peter Wu
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BaseErrorAttributes extends BaseController implements ErrorAttributes, HandlerExceptionResolver,
		Ordered {

	private final String ERROR_ATTRIBUTE = BaseErrorAttributes.class.getName() + ".ERROR";
	protected final String KEY_STATUS = "status";
	protected final String KEY_MESSAGE = "message";
	protected final String KEY_HTTP_STATUS_CODE = "httpStatusCode";

	@Override public Map<String, Object> getErrorAttributes(RequestAttributes requestAttributes, boolean includeStackTrace) {
		String statusCode = null;
		Integer httpStatusCode = null;
		String message;
		Map<String, String> errors = new HashMap<>();

		Throwable e = getError(requestAttributes);

		if (logger.isDebugEnabled() && e != null) {
			logger.debug(e.getMessage(), e);
		}

		Map<String, Object> errorAttributes = new LinkedHashMap<>();

		if (e != null) {
			message = e.getMessage();

			if (logger.isDebugEnabled()) {
				logger.debug(message);
			}

			if (includeStackTrace) {
				addStackTrace(errorAttributes, e);
			}

			if (e instanceof ResourceNotFoundException || e instanceof EmptyResultDataAccessException) {
				httpStatusCode = HttpStatus.NOT_FOUND.value();
				if (!StringUtils.hasText(message)) {
					message = "Resource not found!";
				}
			} else if (e instanceof HttpRequestMethodNotSupportedException) {
				httpStatusCode = HttpStatus.METHOD_NOT_ALLOWED.value();
				if (!StringUtils.hasText(message)) {
					message = "Method not allowed!";
				}
			} else if (e instanceof BindException) {//参数错误
				httpStatusCode = HttpStatus.UNPROCESSABLE_ENTITY.value();
				//			errorMessage.setMessage(getText("data.valid.failed"));

				BindException er = (BindException) e;
				List<FieldError> fieldErrors = er.getFieldErrors();
				//			String errorMsg = getText("data.valid.failed");
				fieldErrors.forEach(fieldError -> {
					String defaultMessage = fieldError.getDefaultMessage();
					if (defaultMessage.contains("required type"))
						defaultMessage = getText(fieldError.getCode());
					errors.put(fieldError.getField(), getText(fieldError.getField()) + defaultMessage);
				});
				message = errors.values().iterator().next();

				if (!StringUtils.hasText(message)) {
					message = getText("data.valid.failed");
				}
			} else if (e instanceof IllegalArgumentException) {
				httpStatusCode = HttpStatus.UNPROCESSABLE_ENTITY.value();
				if (!StringUtils.hasText(message)) {
					message = getText("data.valid.failed");
				}
			} else if (e instanceof ConversionFailedException) {
				httpStatusCode = HttpStatus.UNPROCESSABLE_ENTITY.value();
				message = getText("typeMismatch", ((ConversionFailedException) e).getValue(), ((ConversionFailedException) e).getTargetType());
				if (!StringUtils.hasText(message)) {
					message = getText("data.valid.failed");
				}
			} else if (e instanceof ConstraintViolationException || e instanceof org.springframework.transaction.TransactionSystemException) {//数据验证
				if (e instanceof org.springframework.transaction.TransactionSystemException) {
					e = ((TransactionSystemException) e).getRootCause();
				}

				if (e instanceof ConstraintViolationException) {
					httpStatusCode = HttpStatus.UNPROCESSABLE_ENTITY.value();
					//			errorMessage.setMessage(getText("data.valid.failed"));

					ConstraintViolationException er = (ConstraintViolationException) e;
					Set<ConstraintViolation<?>> constraintViolations = er.getConstraintViolations();
					//			String errorMsg = getText("data.valid.failed");
					constraintViolations
							.forEach(constraintViolation -> errors
									.put(constraintViolation.getPropertyPath().toString(), getText(constraintViolation.getPropertyPath()) + constraintViolation.getMessage()));
					message = errors.values().iterator().next();

					if (!StringUtils.hasText(message)) {
						message = getText("data.valid.failed");
					}
				} else {
					logger.error(message, e);
				}
			} else if (e instanceof DataIntegrityViolationException) {
				String specificCauseMessage = ((DataIntegrityViolationException) e).getMostSpecificCause().getMessage();
				String duplicateRegex = "^Duplicate entry '(.*?)'.*";
				String constraintSubfix = "Cannot delete or update a parent row";
				if (specificCauseMessage.matches(duplicateRegex)) {
					httpStatusCode = HttpStatus.UNPROCESSABLE_ENTITY.value();
					message = getText("duplicate.entry", specificCauseMessage.replaceAll(duplicateRegex, "$1"));
					if (!StringUtils.hasText(message)) {
						message = getText("data.valid.failed");
					}
				} else if (specificCauseMessage.startsWith(constraintSubfix)) {
					httpStatusCode = HttpStatus.UNPROCESSABLE_ENTITY.value();
					message = getText("cannot.delete.update.parent");
					if (!StringUtils.hasText(message)) {
						message = getText("data.valid.failed");
					}
				} else
					message = ((DataIntegrityViolationException) e).getRootCause().getMessage();
			} else if (e instanceof HttpMediaTypeNotAcceptableException) {
				httpStatusCode = HttpStatus.NOT_ACCEPTABLE.value();
				message = "MediaType not Acceptable!Must ACCEPT:" + ((HttpMediaTypeNotAcceptableException) e).getSupportedMediaTypes();
			} else if (e instanceof IllegalStateException) {
				httpStatusCode = HttpStatus.BAD_REQUEST.value();
			} else if (e instanceof org.springframework.data.mapping.PropertyReferenceException) {
				httpStatusCode = HttpStatus.BAD_REQUEST.value();
			} else {
				handlerException(e, errorAttributes, errors);
				statusCode = (String) errorAttributes.get(KEY_STATUS);
				httpStatusCode = (Integer) errorAttributes.get(KEY_HTTP_STATUS_CODE);
				errorAttributes.remove(KEY_HTTP_STATUS_CODE);
				Object handedMessage = errorAttributes.get(KEY_MESSAGE);
				message = handedMessage == null ? message : (String) handedMessage;
			}
		} else {
			message = getAttribute(requestAttributes, "javax.servlet.error.message");
		}

		message = StringUtils.isEmpty(message) ? "No message available" : message;

		if (httpStatusCode == null) {
			httpStatusCode = getStatus(requestAttributes).value();
			if (httpStatusCode != 403 && httpStatusCode != 401 && httpStatusCode != 404) {
				logger.error(httpStatusCode + ":" + message, e);
			}
		}
		statusCode = statusCode == null ? String.valueOf(httpStatusCode) : statusCode;

		setStatus(requestAttributes, httpStatusCode);

		errorAttributes.put(KEY_STATUS, statusCode);
		errorAttributes.put(KEY_MESSAGE, message);
		if (!errors.isEmpty()) {
			errorAttributes.put("errors", errors);
		}

		return errorAttributes;
	}

	protected void handlerException(Throwable error, Map<String, Object> errorAttributes, Map<String, String> errors) {
	}

	private void addStackTrace(Map<String, Object> errorAttributes, Throwable error) {
		StringWriter stackTrace = new StringWriter();
		error.printStackTrace(new PrintWriter(stackTrace));
		stackTrace.flush();
		errorAttributes.put("trace", stackTrace.toString());
	}

	@Override public Throwable getError(RequestAttributes requestAttributes) {
		Throwable exception = getAttribute(requestAttributes, ERROR_ATTRIBUTE);
		if (exception == null) {
			exception = getAttribute(requestAttributes, "javax.servlet.error.exception");
		}
		return exception;
	}

	private void setStatus(RequestAttributes requestAttributes, Object statusCode) {
		requestAttributes.setAttribute("javax.servlet.error.status_code", statusCode, RequestAttributes.SCOPE_REQUEST);
	}

	private HttpStatus getStatus(RequestAttributes requestAttributes) {
		Integer statusCode = getAttribute(requestAttributes, "javax.servlet.error.status_code");
		if (statusCode != null) {
			try {
				return HttpStatus.valueOf(statusCode);
			} catch (Exception ignored) {
			}
		}
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}

	@SuppressWarnings("unchecked")
	private <T> T getAttribute(RequestAttributes requestAttributes, String name) {
		return (T) requestAttributes.getAttribute(name, RequestAttributes.SCOPE_REQUEST);
	}

	@Override public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		request.setAttribute(ERROR_ATTRIBUTE, ex);
		return null;
	}

	@Override public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}
}
