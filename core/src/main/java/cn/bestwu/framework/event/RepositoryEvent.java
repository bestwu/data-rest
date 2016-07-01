package cn.bestwu.framework.event;

import org.springframework.context.ApplicationEvent;

/**
 * 事件：实体事件，涉及CRUD的事件
 *
 * @author Peter Wu
 */
public abstract class RepositoryEvent extends ApplicationEvent {

	private static final long serialVersionUID = 4280564526952763162L;

	/**
	 * 实体类型
	 */
	private final Class<?> modelType;

	/**
	 * @param source    实体
	 * @param modelType 实体类型
	 */
	protected RepositoryEvent(Object source, Class<?> modelType) {
		super(source);
		this.modelType = modelType;
	}

	/**
	 * @return 实体类型
	 */
	public Class<?> getModelType() {
		return modelType;
	}
}
