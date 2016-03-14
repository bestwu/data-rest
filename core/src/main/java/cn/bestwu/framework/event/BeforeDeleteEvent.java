package cn.bestwu.framework.event;

public class BeforeDeleteEvent extends EntityEvent {

	private static final long serialVersionUID = 9150212393209433211L;

	public BeforeDeleteEvent(Object source) {
		super(source);
	}
}
