package cn.bestwu.framework.event;

public class BeforeSaveEvent extends EntityEvent {

	private static final long serialVersionUID = -1404580942928384726L;

	public BeforeSaveEvent(Object source) {
		super(source);
	}
}