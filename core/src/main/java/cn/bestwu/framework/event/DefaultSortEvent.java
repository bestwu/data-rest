package cn.bestwu.framework.event;

import org.springframework.data.domain.Sort;

import java.util.List;

/**
 * 事件：默认排序
 *
 * @author Peter Wu
 */
public class DefaultSortEvent extends RepositoryEvent {

	private static final long serialVersionUID = 4836932640633578985L;

	/**
	 * @param orders    排序
	 * @param domainType 实体类型
	 */
	public DefaultSortEvent(List<Sort.Order> orders, Class<?> domainType) {
		super(orders, domainType);
	}
}
