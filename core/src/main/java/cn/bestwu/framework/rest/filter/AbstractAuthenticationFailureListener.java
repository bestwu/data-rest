package cn.bestwu.framework.rest.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;

/**
 * 权限验证失败记录日志
 *
 * @author Peter Wu
 */
@Slf4j
public class AbstractAuthenticationFailureListener extends AuthenticationFailureListener<AbstractAuthenticationFailureEvent> {

	@Override protected String getPrincipalName(AbstractAuthenticationFailureEvent abstractAuthenticationFailureEvent) {
		return abstractAuthenticationFailureEvent.getAuthentication().getName();
	}
}