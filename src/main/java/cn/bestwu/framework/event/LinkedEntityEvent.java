package cn.bestwu.framework.event;

public abstract class LinkedEntityEvent extends LinkedEvent {

	private static final long serialVersionUID = -8572586256625182910L;

	public LinkedEntityEvent(Object source, Object linked) {
		super(source, linked, source.getClass());
	}
}
