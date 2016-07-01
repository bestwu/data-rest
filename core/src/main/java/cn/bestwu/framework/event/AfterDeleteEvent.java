package cn.bestwu.framework.event;

/**
 * 事件：删除实体后
 *
 * @author Peter Wu
 */
public class AfterDeleteEvent extends EntityEvent {

	private static final long serialVersionUID = -6090615345948638970L;

	/**
	 * @param source 被删实体
	 */
	public AfterDeleteEvent(Object source) {
		super(source);
	}
}
