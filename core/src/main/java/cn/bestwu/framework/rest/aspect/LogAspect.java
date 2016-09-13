package cn.bestwu.framework.rest.aspect;

import cn.bestwu.framework.rest.support.PrincipalNamePutEvent;
import cn.bestwu.framework.rest.support.Resource;
import cn.bestwu.framework.util.ResourceUtil;
import cn.bestwu.framework.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.ServletServerHttpRequest;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Map;

/**
 * 日志拦截器
 *
 * @author Peter Wu
 */
@Aspect
@Slf4j
public class LogAspect {
	/**
	 * put方法请求参数request暂存参数名
	 */
	private final String PUT_PARAMETER_MAP = "PUT_PARAMETER_MAP";

	/**
	 * 请求方名称request暂存参数名
	 */
	public static final String PRINCIPAL_NAME = "PRINCIPAL_NAME";

	protected ApplicationEventPublisher publisher;

	@Autowired(required = false)
	protected HttpServletRequest request;

	public LogAspect(ApplicationEventPublisher publisher) {
		this.publisher = publisher;
	}

	/**
	 * 处理有异常时，跳转异常处理controller时，出现的用户信息丢失，及可能的参数丢失。
	 */
	@AfterThrowing(value = "@annotation(org.springframework.web.bind.annotation.RequestMapping)")
	public void afterThrowing() {
		Resource<String> source = new Resource<>();
		publisher.publishEvent(new PrincipalNamePutEvent(source));
		request.setAttribute(PRINCIPAL_NAME, source.getContent());

		if ("PUT".equals(request.getMethod()))
			request.setAttribute(PUT_PARAMETER_MAP, request.getParameterMap());
	}

	/**
	 * 记录日志
	 *
	 * @param result 操作结果
	 */
	@SuppressWarnings("unchecked")
	@AfterReturning(value = "@annotation(org.springframework.web.bind.annotation.RequestMapping)", returning = "result")
	public void log(Object result) {
		if (log.isInfoEnabled()) {
			// request信息
			String ipAddress = request.getRemoteAddr();
			String servletPath = (String) request.getAttribute(RequestDispatcher.FORWARD_SERVLET_PATH);
			if (servletPath == null) {
				servletPath = request.getServletPath();
			}
			String principalName = (String) request.getAttribute(PRINCIPAL_NAME);
			if (principalName == null) {
				Resource<String> source = new Resource<>();
				publisher.publishEvent(new PrincipalNamePutEvent(source));
				principalName = source.getContent();
			}

			String requestMethod = request.getMethod();
			Map<String, String[]> parameterMap = null;
			try {
				if ("PUT".equals(requestMethod)) {
					parameterMap = (Map<String, String[]>) request.getAttribute(PUT_PARAMETER_MAP);
				}
				if (parameterMap == null) {
					parameterMap = request.getParameterMap();
				}
			} catch (Exception e) {
				if (log.isDebugEnabled()) {
					log.debug("获取请求参数出错", e);
				}
				parameterMap = null;
			}

			ServletServerHttpRequest servletServerHttpRequest = new ServletServerHttpRequest(request);
			HttpHeaders headers = servletServerHttpRequest.getHeaders();
			String MSG_CODE = "{} [{}] [{}] {} {} {} HEADERS[{}]HEADERSEND PARAMETERS[{}]PARAMETERSEND {}";

			principalName = principalName == null ? (request.getRemoteUser() == null ? "anonymousUser" : request.getRemoteUser()) : principalName;
			String requestSignature = ResourceUtil.API_SIGNATURE.get();

			String resultStr;
			boolean error = false;
			if (result instanceof ResponseEntity) {
				ResponseEntity responseEntity = (ResponseEntity) result;
				HttpStatus statusCode = responseEntity.getStatusCode();
				if (statusCode.is2xxSuccessful() || statusCode.is3xxRedirection()) {
					resultStr = statusCode.toString() + " " + statusCode.getReasonPhrase();
				} else {
					if (log.isDebugEnabled())
						resultStr = "Error:\n" + StringUtil.valueOf(result, true);
					else
						resultStr = "Error:" + StringUtil.valueOf(result);
				}
				error = statusCode.is5xxServerError();
			} else {
				resultStr = StringUtil.subString(String.valueOf(result), 100);
			}
			if (log.isDebugEnabled())
				log.info("{} [{}] [{}] {} {} {}\nheaders\n{}\nparameters\n{}\n{}", ipAddress, StringUtil.subString(getUserAgent(), 220), principalName, requestMethod,
						requestSignature,
						servletPath, StringUtil.valueOf(headers, true), StringUtil.valueOf(parameterMap, true), resultStr);
			else {
				if (error)
					log.error(MSG_CODE, ipAddress, StringUtil.subString(getUserAgent(), 220), principalName, requestMethod, requestSignature,
							servletPath, StringUtil.valueOf(headers), StringUtil.valueOf(parameterMap), resultStr);
				else
					log.info(MSG_CODE, ipAddress, StringUtil.subString(getUserAgent(), 220), principalName, requestMethod, requestSignature,
							servletPath, StringUtil.valueOf(headers), StringUtil.valueOf(parameterMap), resultStr);
			}

		}
	}

	/**
	 * @return 客户端user-agent
	 */
	public String getUserAgent() {
		Enumeration<String> headers = request.getHeaders("user-agent");
		if (headers.hasMoreElements()) {
			return headers.nextElement();
		} else {
			return null;
		}
	}
}
