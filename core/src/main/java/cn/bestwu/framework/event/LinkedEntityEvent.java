package cn.bestwu.framework.event;

/**
 * 事件：关联实体
 *
 * @author Peter Wu
 */
public abstract class LinkedEntityEvent extends LinkedEvent {

	private static final long serialVersionUID = -8572586256625182910L;

	/**
	 * @param source 实体
	 * @param linked 关联实体
	 */
	public LinkedEntityEvent(Object source, Object linked) {
		super(source, linked, source.getClass());
	}
}
