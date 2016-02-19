package cn.bestwu.framework.event;

public class AfterCreateEvent extends EntityEvent {

	private static final long serialVersionUID = -7673953693485678403L;

	public AfterCreateEvent(Object source) {
		super(source);
	}
}
