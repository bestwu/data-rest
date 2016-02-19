package cn.bestwu.framework.event;

import org.springframework.context.ApplicationEvent;

/**
 * @author Peter Wu
 */
public abstract class RepositoryEvent extends ApplicationEvent {

	private static final long serialVersionUID = 4280564526952763162L;

	private final Class<?> domainType;

	protected RepositoryEvent(Object source, Class<?> domainType) {
		super(source);
		this.domainType = domainType;
	}

	public Class<?> getDomainType() {
		return domainType;
	}
}
