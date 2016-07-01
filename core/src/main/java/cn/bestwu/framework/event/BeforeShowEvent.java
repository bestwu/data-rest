package cn.bestwu.framework.event;

/**
 * 事件：查看单个实体前
 *
 * @author Peter Wu
 */
public class BeforeShowEvent extends EntityEvent {

	private static final long serialVersionUID = -1642841708537223975L;

	/**
	 * @param source 查看的实体
	 */
	public BeforeShowEvent(Object source) {
		super(source);
	}
}
