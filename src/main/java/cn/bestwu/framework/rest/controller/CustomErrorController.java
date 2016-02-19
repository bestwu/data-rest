package cn.bestwu.framework.rest.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
@ConditionalOnWebApplication
@RequestMapping(value = "${error.path:/error}")
public class CustomErrorController extends BaseController implements ErrorController {

	@Value("${error.path:/error}")
	private String errorPath;

	@Autowired
	private ErrorAttributes errorAttributes;

	@Override
	public String getErrorPath() {
		return this.errorPath;
	}

	@Autowired
	private MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter;

	private String getString(Map<String, Object> body) throws JsonProcessingException {
		return mappingJackson2HttpMessageConverter.getObjectMapper().writeValueAsString(body);
	}

	@RequestMapping(produces = { "text/plain", "text/html" })
	@ResponseBody
	public ResponseEntity<Object> errorPlain(HttpServletRequest request) throws JsonProcessingException {
		Map<String, Object> body = getErrorAttributes(request, getTraceParameter(request));
		HttpStatus status = getStatus(request);
		return new ResponseEntity<>(getString(body), status);
	}

	@RequestMapping
	@ResponseBody
	public ResponseEntity<Object> error(HttpServletRequest request) throws JsonProcessingException {
		Map<String, Object> body = getErrorAttributes(request, getTraceParameter(request));
		HttpStatus status = getStatus(request);
		if (status.equals(HttpStatus.NOT_ACCEPTABLE)) {
			return new ResponseEntity<>(getString(body), status);
		}
		return new ResponseEntity<>(body, status);
	}

	private boolean getTraceParameter(HttpServletRequest request) {
		String parameter = request.getParameter("trace");
		return parameter != null && !"false".equals(parameter.toLowerCase());
	}

	private Map<String, Object> getErrorAttributes(HttpServletRequest request,
			boolean includeStackTrace) {
		RequestAttributes requestAttributes = new ServletRequestAttributes(request);
		return this.errorAttributes.getErrorAttributes(requestAttributes,
				includeStackTrace);
	}

	private HttpStatus getStatus(HttpServletRequest request) {
		Integer statusCode = (Integer) request
				.getAttribute("javax.servlet.error.status_code");
		if (statusCode != null) {
			try {
				return HttpStatus.valueOf(statusCode);
			} catch (Exception ignored) {
			}
		}
		return HttpStatus.INTERNAL_SERVER_ERROR;
	}

}