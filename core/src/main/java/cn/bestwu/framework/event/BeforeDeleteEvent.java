package cn.bestwu.framework.event;

/**
 * 事件：删除实体前
 *
 * @author Peter Wu
 */
public class BeforeDeleteEvent extends EntityEvent {

	private static final long serialVersionUID = 9150212393209433211L;

	/**
	 * @param source 要删除的实体
	 */
	public BeforeDeleteEvent(Object source) {
		super(source);
	}
}
