package cn.bestwu.framework.event;

/**
 * @author Peter Wu
 */
public abstract class EntityEvent extends RepositoryEvent {
	private static final long serialVersionUID = 5976098506650423457L;

	protected EntityEvent(Object source) {
		super(source, source.getClass());
	}
}
