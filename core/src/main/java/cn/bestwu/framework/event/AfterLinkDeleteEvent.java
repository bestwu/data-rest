package cn.bestwu.framework.event;

public class AfterLinkDeleteEvent extends LinkedEntityEvent {

	private static final long serialVersionUID = 3887575011761146290L;

	public AfterLinkDeleteEvent(Object source, Object linked) {
		super(source, linked);
	}
}
