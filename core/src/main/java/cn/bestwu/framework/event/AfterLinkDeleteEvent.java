package cn.bestwu.framework.event;

/**
 * 事件：删除实体后有关联实体
 *
 * @author Peter Wu
 */
public class AfterLinkDeleteEvent extends LinkedEntityEvent {

	private static final long serialVersionUID = 3887575011761146290L;

	/**
	 * @param source 被删实体
	 * @param linked 关联实体
	 */
	public AfterLinkDeleteEvent(Object source, Object linked) {
		super(source, linked);
	}
}
