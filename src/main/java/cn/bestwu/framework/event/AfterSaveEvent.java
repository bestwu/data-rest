package cn.bestwu.framework.event;

public class AfterSaveEvent extends EntityEvent {

	private static final long serialVersionUID = 8568843338617401903L;

	public AfterSaveEvent(Object source) {
		super(source);
	}
}
