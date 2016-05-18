package cn.bestwu.framework.rest.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.AbstractErrorController;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static cn.bestwu.framework.rest.support.Response.noCache;

@Controller
@ConditionalOnWebApplication
@RequestMapping("${server.error.path:${error.path:/error}}")
public class CustomErrorController extends AbstractErrorController {

	@Autowired
	private ServerProperties serverProperties;

	@Autowired
	public CustomErrorController(ErrorAttributes errorAttributes) {
		super(errorAttributes);
	}

	@Override
	public String getErrorPath() {
		return this.serverProperties.getError().getPath();
	}

	@Autowired
	private MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter;

	private String getString(Map<String, Object> body) throws JsonProcessingException {
		return mappingJackson2HttpMessageConverter.getObjectMapper().writeValueAsString(body);
	}

	protected ErrorProperties getErrorProperties() {
		return this.serverProperties.getError();
	}

	protected boolean isIncludeStackTrace(HttpServletRequest request, MediaType... produces) {
		ErrorProperties.IncludeStacktrace include = getErrorProperties().getIncludeStacktrace();
		if (include == ErrorProperties.IncludeStacktrace.ALWAYS) {
			return true;
		}
		if (include == ErrorProperties.IncludeStacktrace.ON_TRACE_PARAM) {
			return getTraceParameter(request);
		}
		return false;
	}

	@RequestMapping(produces = { "text/plain", "text/html" })
	@ResponseBody
	public ResponseEntity<Object> errorPlain(HttpServletRequest request) throws JsonProcessingException {
		Map<String, Object> body = getErrorAttributes(request, isIncludeStackTrace(request, MediaType.TEXT_HTML, MediaType.TEXT_PLAIN));
		HttpStatus status = getStatus(request);
		return ResponseEntity.status(status).headers(noCache()).body(getString(body));
	}

	@RequestMapping
	@ResponseBody
	public ResponseEntity<Object> error(HttpServletRequest request) throws JsonProcessingException {
		Map<String, Object> body = getErrorAttributes(request, isIncludeStackTrace(request, MediaType.ALL));
		HttpStatus status = getStatus(request);
		if (status.equals(HttpStatus.NOT_ACCEPTABLE)) {
			return ResponseEntity.status(status).headers(noCache()).body(getString(body));
		}
		return ResponseEntity.status(status).headers(noCache()).body(body);
	}

}