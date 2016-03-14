package cn.bestwu.framework.event;

public class AfterDeleteEvent extends EntityEvent {

	private static final long serialVersionUID = -6090615345948638970L;

	public AfterDeleteEvent(Object source) {
		super(source);
	}
}
