package cn.bestwu.framework.rest.aspect;

import cn.bestwu.framework.rest.controller.BaseController;
import cn.bestwu.framework.rest.support.PrincipalNamePutEvent;
import cn.bestwu.framework.rest.support.RequestJsonViewResponseBodyAdvice;
import cn.bestwu.framework.rest.support.Resource;
import cn.bestwu.framework.util.ResourceUtil;
import cn.bestwu.framework.util.StringUtil;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.http.server.ServletServerHttpRequest;

import javax.servlet.RequestDispatcher;
import java.util.Map;

@Aspect
public class LogAspect extends BaseController {
	private final Logger logger = LoggerFactory.getLogger(LogAspect.class);
	private final String PUT_PARAMETER_MAP = "PUT_PARAMETER_MAP";
	private final String PRINCIPAL_NAME = "PRINCIPAL_NAME";

	protected ApplicationEventPublisher publisher;

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

	@Autowired(required = false)
	private RequestJsonViewResponseBodyAdvice requestJsonViewResponseBodyAdvice;

	@AfterReturning(value = "@annotation(org.springframework.web.bind.annotation.RequestMapping)", returning = "result")
	public void log(Object result) {
		if (logger.isInfoEnabled()) {
			// request信息
			String ipAddress = request.getRemoteAddr();
			Object servletPath = request.getAttribute(RequestDispatcher.FORWARD_SERVLET_PATH);
			if (servletPath == null) {
				servletPath = request.getServletPath();
			}
			Object principalName = request.getAttribute(PRINCIPAL_NAME);
			if (principalName == null) {
				Resource<String> source = new Resource<>();
				publisher.publishEvent(new PrincipalNamePutEvent(source));
				principalName = source.getContent();
			}

			String requestMethod = request.getMethod();
			Map<String, String[]> parameterMap;
			try {
				parameterMap = request.getParameterMap();

				if ("PUT".equals(requestMethod)) {
					@SuppressWarnings("unchecked")
					Map<String, String[]> putParameterMap = (Map<String, String[]>) request.getAttribute(PUT_PARAMETER_MAP);
					if (putParameterMap != null) {
						parameterMap.putAll(putParameterMap);
					}
				}
			} catch (Exception e) {
				if (logger.isDebugEnabled()) {
					logger.debug("获取请求参数出错", e);
				}
				parameterMap = null;
			}

			ServletServerHttpRequest servletServerHttpRequest = new ServletServerHttpRequest(request);
			HttpHeaders headers = servletServerHttpRequest.getHeaders();
			String MSG_CODE = "{} 的 [{}] {} {} \nrequest headers:\n{} \nrequest parameters:\n{} \nresponse:\n{}";

			if (logger.isDebugEnabled()) {
				String requestSignature = ResourceUtil.getRequestSignature(request);
				String resultStr;
				if ("GET_LOGS_INDEX".equals(requestSignature)) {
					resultStr = StringUtil.subString(result.toString(), 100);
				} else {
					if (requestJsonViewResponseBodyAdvice != null) {
						MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(result);
						requestJsonViewResponseBodyAdvice.beforeBodyWrite(mappingJacksonValue, request);
						result = mappingJacksonValue;
					}
					resultStr = StringUtil.valueOf(result, true);
				}
				logger.info(MSG_CODE, ipAddress, principalName == null ? (request.getRemoteUser() == null ? "anonymousUser" : request.getRemoteUser()) : principalName, requestMethod,
						servletPath, StringUtil.valueOf(headers, true), StringUtil.valueOf(parameterMap, true),
						resultStr);
			} else
				logger.info(MSG_CODE, ipAddress, principalName == null ? (request.getRemoteUser() == null ? "anonymousUser" : request.getRemoteUser()) : principalName, requestMethod,
						servletPath, StringUtil.valueOf(headers, true), StringUtil.subString(StringUtil.valueOf(parameterMap), 100),
						StringUtil.subString(String.valueOf(result), 100));
		}
	}

}
