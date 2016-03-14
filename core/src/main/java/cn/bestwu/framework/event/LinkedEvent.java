package cn.bestwu.framework.event;

public abstract class LinkedEvent extends RepositoryEvent {

	private static final long serialVersionUID = -772062121414001949L;
	private final Object linked;

	public LinkedEvent(Object source, Object linked, Class<?> modelType) {
		super(source, modelType);
		this.linked = linked;
	}

	public Object getLinked() {
		return linked;
	}
}
