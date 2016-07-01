package cn.bestwu.framework.event;

/**
 * 事件：更新实体后
 *
 * @author Peter Wu
 */
public class AfterSaveEvent extends EntityEvent {

	private static final long serialVersionUID = 8568843338617401903L;

	/**
	 * @param source 更新后的实体
	 */
	public AfterSaveEvent(Object source) {
		super(source);
	}
}
