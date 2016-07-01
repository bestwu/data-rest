package cn.bestwu.framework.event;

import cn.bestwu.framework.rest.support.PersistentEntityResource;

/**
 * 事件：查询实体结果中单个实体
 *
 * @author Peter Wu
 */
public class ItemResourceEvent extends LinkedEntityEvent {

	private static final long serialVersionUID = 21083954687279686L;

	/**
	 * @param source   实体
	 * @param resource 实体资源包裹类
	 */
	public ItemResourceEvent(Object source, PersistentEntityResource<?> resource) {
		super(source, resource);
	}
}
