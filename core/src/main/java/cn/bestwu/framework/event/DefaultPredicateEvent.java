package cn.bestwu.framework.event;

import cn.bestwu.framework.rest.support.Resource;
import com.querydsl.core.types.Predicate;

/**
 * 给Predicate 附加条件
 *
 * @author Peter Wu
 */
public class DefaultPredicateEvent extends RepositoryEvent {

	private static final long serialVersionUID = 1224057810877643594L;

	/**
	 * @param predicateResource 持有Predicate 的资源
	 * @param domainType         实体类型
	 */
	public DefaultPredicateEvent(Resource<Predicate> predicateResource, Class<?> domainType) {
		super(predicateResource, domainType);
	}
}