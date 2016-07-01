package cn.bestwu.framework.event;

/**
 * 事件：更新实体前
 *
 * @author Peter Wu
 */
public class BeforeSaveEvent extends EntityEvent {

	private static final long serialVersionUID = -1404580942928384726L;

	/**
	 * @param source 要更新的实体
	 */
	public BeforeSaveEvent(Object source) {
		super(source);
	}
}
