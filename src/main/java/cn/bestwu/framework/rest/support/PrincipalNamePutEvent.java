package cn.bestwu.framework.rest.support;

import org.springframework.context.ApplicationEvent;

/**
 * @author Peter Wu
 */
public class PrincipalNamePutEvent extends ApplicationEvent {
	private static final long serialVersionUID = 1122030040426673549L;

	/**
	 * Create a new ApplicationEvent.
	 *
	 * @param source the object on which the event initially occurred (never {@code null})
	 */
	public PrincipalNamePutEvent(Resource<String> source) {
		super(source);
	}

	@SuppressWarnings("unchecked")
	public Resource<String> getBaseResource() {
		return (Resource<String>) getSource();
	}
}
