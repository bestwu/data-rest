package cn.bestwu.framework.event;

import cn.bestwu.framework.rest.support.Log;
import org.springframework.context.ApplicationEvent;

/**
 * 事件：日志记录
 *
 * @author Peter Wu
 */
public class LogEvent extends ApplicationEvent {
	private static final long serialVersionUID = -3748271390036778283L;

	/**
	 * @param source 日志
	 */
	public LogEvent(Log source) {
		super(source);
	}
}
