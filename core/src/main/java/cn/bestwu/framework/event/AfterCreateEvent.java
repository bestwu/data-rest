package cn.bestwu.framework.event;

/**
 * 创建新实体后事件
 *
 * @author Peter Wu
 */
public class AfterCreateEvent extends EntityEvent {

	private static final long serialVersionUID = -7673953693485678403L;

	/**
	 * @param source 新实体
	 */
	public AfterCreateEvent(Object source) {
		super(source);
	}
}
