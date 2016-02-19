package cn.bestwu.framework.rest.aspect;

import cn.bestwu.framework.rest.controller.BaseController;
import cn.bestwu.framework.rest.support.Resource;
import cn.bestwu.framework.rest.support.PrincipalNamePutEvent;
import cn.bestwu.framework.util.StringUtil;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import javax.servlet.RequestDispatcher;
import java.util.Map;

@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
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

			String user_agent = getUserAgent();
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
			String parameters = StringUtil.subString(StringUtil.valueOf(parameterMap), 100);
			String MSG_CODE = "{}/{} 的 [{}] {} {} 参数：{} 结果： {}";
			logger.info(MSG_CODE, ipAddress, user_agent, principalName == null ? (request.getRemoteUser() == null ? "anonymousUser" : request.getRemoteUser()) : principalName, requestMethod,
					servletPath, parameters,
					StringUtil.subString(String.valueOf(result), 100));

			if (logger.isDebugEnabled()) {
				logger.debug("parameters:{}", String.valueOf(parameterMap));
				logger.debug("result:{}", String.valueOf(result));
			}
		}
	}

}
