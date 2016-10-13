package cn.bestwu.framework.data.query;

import lombok.Getter;
import lombok.Setter;
import org.apache.lucene.search.Query;
import org.hibernate.search.query.dsl.QueryBuilder;

/**
 * @author Peter Wu
 */
@Getter @Setter
public class QueryCarrier {

	private QueryBuilder queryBuilder;

	private Query query;
	private Query defaultQuery;

	private String keyword;

	//--------------------------------------------

	public QueryCarrier(QueryBuilder queryBuilder, Query defaultQuery, String keyword) {
		this.queryBuilder = queryBuilder;
		this.defaultQuery = defaultQuery;
		this.keyword = keyword;
	}
}
