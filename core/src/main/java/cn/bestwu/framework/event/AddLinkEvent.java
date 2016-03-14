package cn.bestwu.framework.event;

import cn.bestwu.framework.rest.support.PersistentEntityResource;

public class AddLinkEvent extends LinkedEntityEvent {

	private static final long serialVersionUID = 21083954687279686L;

	public AddLinkEvent(Object source, PersistentEntityResource<?> resource) {
		super(source, resource);
	}
}