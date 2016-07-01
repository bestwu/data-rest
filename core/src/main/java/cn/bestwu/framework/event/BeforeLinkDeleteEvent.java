package cn.bestwu.framework.event;

/**
 * 事件：删除实体前，有关联实体
 *
 * @author Peter Wu
 */
public class BeforeLinkDeleteEvent extends LinkedEntityEvent {

	private static final long serialVersionUID = -973540913790564962L;

	/**
	 * @param source 要删除的实体
	 * @param linked 关联实体
	 */
	public BeforeLinkDeleteEvent(Object source, Object linked) {
		super(source, linked);
	}
}
