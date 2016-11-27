package cn.bestwu.framework.event;

import cn.bestwu.framework.data.query.QueryCarrier;

/**
 * 全文搜索前设置其他条件
 *
 * @author Peter Wu
 */
public class QueryBuilderEvent extends RepositoryEvent {

	private static final long serialVersionUID = -4178548774162112340L;

	/**
	 * @param queryCarrier 搜索条件
	 * @param domainType    实体类型
	 */
	public QueryBuilderEvent(QueryCarrier queryCarrier, Class<?> domainType) {
		super(queryCarrier, domainType);
	}
}