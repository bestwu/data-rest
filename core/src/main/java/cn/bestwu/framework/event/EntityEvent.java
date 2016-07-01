package cn.bestwu.framework.event;

/**
 * 实体事件
 *
 * @author Peter Wu
 */
public abstract class EntityEvent extends RepositoryEvent {
	private static final long serialVersionUID = 5976098506650423457L;

	/**
	 * @param source 实体
	 */
	protected EntityEvent(Object source) {
		super(source, source.getClass());
	}
}
