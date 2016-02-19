package cn.bestwu.framework.event;

import org.springframework.data.domain.Sort;

import java.util.List;

public class DefaultSortEvent extends RepositoryEvent {

	private static final long serialVersionUID = 4836932640633578985L;

	public DefaultSortEvent(List<Sort.Order> orders, Class<?> domainType) {
		super(orders, domainType);
	}
}
