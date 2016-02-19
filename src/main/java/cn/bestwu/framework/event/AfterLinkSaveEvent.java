package cn.bestwu.framework.event;

public class AfterLinkSaveEvent extends LinkedEntityEvent {

	private static final long serialVersionUID = 261522353893713633L;

	public AfterLinkSaveEvent(Object source, Object child) {
		super(source, child);
	}
}
