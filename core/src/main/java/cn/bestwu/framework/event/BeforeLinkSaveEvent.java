package cn.bestwu.framework.event;

/**
 * 事件：更新实体前，有关联实体
 *
 * @author Peter Wu
 */
public class BeforeLinkSaveEvent extends LinkedEntityEvent {

	private static final long serialVersionUID = 4836932640633578985L;

	/**
	 * @param source 要更新的实体
	 * @param linked 关联实体
	 */
	public BeforeLinkSaveEvent(Object source, Object linked) {
		super(source, linked);
	}
}
