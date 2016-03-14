package cn.bestwu.framework.event;

import org.springframework.util.MultiValueMap;

/**
 * @author Peter Wu
 */
public class DefaultPredicateEvent extends RepositoryEvent {

	private static final long serialVersionUID = 1224057810877643594L;

	public DefaultPredicateEvent(MultiValueMap<String, String> multiValueMap, Class<?> modelType) {
		super(multiValueMap, modelType);
	}
}