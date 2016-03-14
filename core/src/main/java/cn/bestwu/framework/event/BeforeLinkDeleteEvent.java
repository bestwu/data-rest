package cn.bestwu.framework.event;

public class BeforeLinkDeleteEvent extends LinkedEntityEvent {

	private static final long serialVersionUID = -973540913790564962L;

	public BeforeLinkDeleteEvent(Object source, Object linked) {
		super(source, linked);
	}
}
