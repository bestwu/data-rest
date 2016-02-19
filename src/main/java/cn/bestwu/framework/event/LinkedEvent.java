package cn.bestwu.framework.event;

public abstract class LinkedEvent extends RepositoryEvent {

	private static final long serialVersionUID = -772062121414001949L;
	private final Object linked;

	public LinkedEvent(Object source, Object linked, Class<?> domainType) {
		super(source, domainType);
		this.linked = linked;
	}

	public Object getLinked() {
		return linked;
	}
}
