package cn.bestwu.framework.event;

public class BeforeCreateEvent extends EntityEvent {

	private static final long serialVersionUID = -1642841708537223975L;

	public BeforeCreateEvent(Object source) {
		super(source);
	}
}
