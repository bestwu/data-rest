package cn.bestwu.framework.event;

public class BeforeLinkSaveEvent extends LinkedEntityEvent {

	private static final long serialVersionUID = 4836932640633578985L;

	public BeforeLinkSaveEvent(Object source, Object linked) {
		super(source, linked);
	}
}
