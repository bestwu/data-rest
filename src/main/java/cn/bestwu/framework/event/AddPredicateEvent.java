package cn.bestwu.framework.event;

import cn.bestwu.framework.rest.support.Resource;
import com.mysema.query.types.Predicate;

/**
 * 给Predicate 附加条件
 *
 * @author Peter Wu
 */
public class AddPredicateEvent extends RepositoryEvent {

	private static final long serialVersionUID = 1224057810877643594L;

	public AddPredicateEvent(Resource<Predicate> predicateResource, Class<?> domainType) {
		super(predicateResource, domainType);
	}
}