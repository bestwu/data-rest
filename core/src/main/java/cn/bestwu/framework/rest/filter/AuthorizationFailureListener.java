package cn.bestwu.framework.rest.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.event.AuthorizationFailureEvent;

/**
 * 权限验证失败记录日志
 *
 * @author Peter Wu
 */
@Slf4j
public class AuthorizationFailureListener extends AbstractAuthenticationFailureListener<AuthorizationFailureEvent> {

	@Override protected String getPrincipalName(AuthorizationFailureEvent authorizationFailureEvent) {
		return authorizationFailureEvent.getAuthentication().getName();
	}
}