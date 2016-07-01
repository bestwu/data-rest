package cn.bestwu.framework.event;

/**
 * 事件：创建新实体前
 *
 * @author Peter Wu
 */
public class BeforeCreateEvent extends EntityEvent {

	private static final long serialVersionUID = -1642841708537223975L;

	/**
	 * @param source 新实体
	 */
	public BeforeCreateEvent(Object source) {
		super(source);
	}
}
