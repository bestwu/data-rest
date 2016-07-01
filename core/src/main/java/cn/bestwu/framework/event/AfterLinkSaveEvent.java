package cn.bestwu.framework.event;

/**
 * 事件：更新实体后有关联实体
 *
 * @author Peter Wu
 */
public class AfterLinkSaveEvent extends LinkedEntityEvent {

	private static final long serialVersionUID = 261522353893713633L;

	/**
	 * @param source 更新后实体
	 * @param old    关联实体,更新前实体
	 */
	public AfterLinkSaveEvent(Object source, Object old) {
		super(source, old);
	}
}
