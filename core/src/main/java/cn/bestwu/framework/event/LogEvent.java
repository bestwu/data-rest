package cn.bestwu.framework.event;

import cn.bestwu.framework.rest.support.Log;
import org.springframework.context.ApplicationEvent;

/**
 * @author Peter Wu
 */
public class LogEvent extends ApplicationEvent {
	private static final long serialVersionUID = -3748271390036778283L;

	/**
	 * Create a new ApplicationEvent.
	 *
	 * @param source the object on which the event initially occurred (never {@code null})
	 */
	public LogEvent(Log source) {
		super(source);
	}
}
