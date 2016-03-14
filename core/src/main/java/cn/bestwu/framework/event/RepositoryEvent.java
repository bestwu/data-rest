package cn.bestwu.framework.event;

import org.springframework.context.ApplicationEvent;

/**
 * @author Peter Wu
 */
public abstract class RepositoryEvent extends ApplicationEvent {

	private static final long serialVersionUID = 4280564526952763162L;

	private final Class<?> modelType;

	protected RepositoryEvent(Object source, Class<?> modelType) {
		super(source);
		this.modelType = modelType;
	}

	public Class<?> getModelType() {
		return modelType;
	}
}
