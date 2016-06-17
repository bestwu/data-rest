package cn.bestwu.framework.event;

import cn.bestwu.framework.rest.support.PersistentEntityResource;

public class ItemResourceEvent extends LinkedEntityEvent {

	private static final long serialVersionUID = 21083954687279686L;

	public ItemResourceEvent(Object source, PersistentEntityResource<?> resource) {
		super(source, resource);
	}
}
