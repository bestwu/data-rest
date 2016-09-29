package cn.bestwu.framework.rest.filter;

import cn.bestwu.framework.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServletServerHttpRequest;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.Map;

/**
 * 权限验证失败记录日志
 *
 * @author Peter Wu
 */
@Slf4j
public abstract class AuthenticationFailureListener<T extends ApplicationEvent> implements ApplicationListener<T> {

	@Autowired(required = false)
	protected HttpServletRequest request;
	@Autowired(required = false)
	protected HttpServletResponse response;

	public void onApplicationEvent(T event) {
		if (log.isInfoEnabled()) {
			// request信息
			String ipAddress = request.getRemoteAddr();
			String servletPath = (String) request.getAttribute(RequestDispatcher.FORWARD_SERVLET_PATH);
			if (servletPath == null) {
				servletPath = request.getServletPath();
			}

			String requestMethod = request.getMethod();
			Map<String, String[]> parameterMap = request.getParameterMap();

			ServletServerHttpRequest servletServerHttpRequest = new ServletServerHttpRequest(request);
			HttpHeaders headers = servletServerHttpRequest.getHeaders();
			String MSG_CODE = "{} [{}] [{}] {} {} {} HEADERS[{}]HEADERSEND PARAMETERS[{}]PARAMETERSEND {}";

			String principalName = getPrincipalName(event);

			principalName = principalName == null ? (request.getRemoteUser() == null ? "anonymousUser" : request.getRemoteUser()) : principalName;

			String requestSignature = request.getMethod().toLowerCase() + servletPath.replace("/", "_");
			String resultStr = "权限认证失败";
			if (log.isDebugEnabled())
				log.info("{} [{}] [{}] {} {} {}\nheaders\n{}\nparameters\n{}\n{}", ipAddress, StringUtil.subString(getUserAgent(), 220), principalName, requestMethod,
						requestSignature,
						servletPath, StringUtil.valueOf(headers, true), StringUtil.valueOf(parameterMap, true), resultStr);
			else {
				log.info(MSG_CODE, ipAddress, StringUtil.subString(getUserAgent(), 220), principalName, requestMethod, requestSignature,
						servletPath, StringUtil.valueOf(headers), StringUtil.valueOf(parameterMap), resultStr);
			}

		}
	}

	protected abstract String getPrincipalName(T t);

	private String getUserAgent() {
		Enumeration<String> headers = request.getHeaders("user-agent");
		if (headers.hasMoreElements()) {
			return headers.nextElement();
		} else {
			return null;
		}
	}
}