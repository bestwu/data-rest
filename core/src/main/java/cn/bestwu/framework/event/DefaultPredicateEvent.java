package cn.bestwu.framework.event;

import org.springframework.util.MultiValueMap;

/**
 * 事件：默认QueryDsl Predicate
 *
 * @author Peter Wu
 */
public class DefaultPredicateEvent extends RepositoryEvent {

	private static final long serialVersionUID = 1224057810877643594L;

	/**
	 * @param multiValueMap 请求参数
	 * @param modelType     实体类型
	 */
	public DefaultPredicateEvent(MultiValueMap<String, String> multiValueMap, Class<?> modelType) {
		super(multiValueMap, modelType);
	}
}