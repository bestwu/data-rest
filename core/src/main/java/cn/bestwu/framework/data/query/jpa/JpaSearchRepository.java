package cn.bestwu.framework.data.query.jpa;

import cn.bestwu.framework.data.annotation.HighLight;
import cn.bestwu.framework.data.query.LuceneSort;
import cn.bestwu.framework.data.query.QueryCarrier;
import cn.bestwu.framework.data.query.ResultHandler;
import cn.bestwu.framework.data.query.SearchRepository;
import cn.bestwu.framework.event.QueryBuilderEvent;
import org.apache.lucene.search.Query;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.exception.EmptyQueryException;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Jpa全文搜索的hibernate Search实现
 *
 * @author Peter Wu
 */
@Transactional(readOnly = true)
public class JpaSearchRepository implements SearchRepository {

	private Map<Class, String[]> fieldsCache = new HashMap<>();
	private Map<Class, String[]> highlightFieldsCache = new HashMap<>();

	private final EntityManager entityManager;
	private final ApplicationEventPublisher publisher;

	public JpaSearchRepository(EntityManager entityManager, ApplicationEventPublisher publisher) {
		this.entityManager = entityManager;
		this.publisher = publisher;
	}

	/**
	 * @param domainType domainType
	 * @param <T>       <T>
	 * @return 搜索字段
	 */
	private <T> String[] getSearchFields(Class<T> domainType) {
		String[] fields;
		if (fieldsCache.containsKey(domainType)) {
			fields = fieldsCache.get(domainType);
		} else {
			fields = JpaSearchFieldUtil.getAnnotationedFields(domainType, Field.class);
			if (fields.length == 0) {
				throw new RuntimeException("搜索的类型" + domainType + "没有标注索引字段,请使用org.hibernate.search.annotations.Field标注");
			}
			fieldsCache.put(domainType, fields);
		}
		return fields;
	}

	/**
	 * @param domainType domainType
	 * @param <T>       <T>
	 * @return 高亮字段
	 */
	private <T> String[] getHighLightFields(Class<T> domainType) {
		String[] highLightFields;
		if (highlightFieldsCache.containsKey(domainType)) {
			highLightFields = highlightFieldsCache.get(domainType);
		} else {
			highLightFields = JpaSearchFieldUtil.getAnnotationedFields(domainType, HighLight.class);
			if (highLightFields.length == 0) {
				highLightFields = getSearchFields(domainType);
			}
			highlightFieldsCache.put(domainType, highLightFields);
		}
		return highLightFields;
	}

	/**
	 * @param domainType     要搜索的类
	 * @param keyword       关键字
	 * @param pageable      分页
	 * @param resultHandler 结果处理
	 * @param <T>           T
	 * @return 结果
	 */
	@SuppressWarnings("unchecked")
	@Override public <T> Page search(Class<T> domainType, String keyword, Pageable pageable, ResultHandler resultHandler) {
		try {
			FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
			SearchFactory searchFactory = fullTextEntityManager.getSearchFactory();
			QueryBuilder queryBuilder = searchFactory.buildQueryBuilder().forEntity(domainType).get();

			Query luceneQuery;
			if (keyword.length() == 1) {
				luceneQuery = queryBuilder.keyword().wildcard().onFields(getSearchFields(domainType)).matching(keyword + "*").createQuery();
			} else if (keyword.contains("*") || keyword.contains("?")) {
				luceneQuery = queryBuilder.keyword().wildcard().onFields(getSearchFields(domainType)).matching(keyword).createQuery();
			} else {
				luceneQuery = queryBuilder.keyword().onFields(getSearchFields(domainType)).matching(keyword).createQuery();
			}
			{//生成query
				QueryCarrier queryCarrier = new QueryCarrier(queryBuilder, luceneQuery, keyword);
				publisher.publishEvent(new QueryBuilderEvent(queryCarrier, domainType));
				Query query = queryCarrier.getQuery();
				if (query != null) {
					luceneQuery = query;
				}
			}

			List<T> result;
			long totalSize;
			org.hibernate.search.jpa.FullTextQuery fullTextQuery = fullTextEntityManager.createFullTextQuery(luceneQuery, domainType);
			totalSize = fullTextQuery.getResultSize();

			if (totalSize > 0) {
				org.springframework.data.domain.Sort sort = pageable.getSort();
				if (sort != null && sort instanceof LuceneSort) {
					fullTextQuery.setSort(((LuceneSort) sort).getSort());
				}

				fullTextQuery.setFirstResult(pageable.getOffset());
				fullTextQuery.setMaxResults(pageable.getPageSize());
				result = fullTextQuery.getResultList();
			} else {
				result = Collections.emptyList();
			}

			//处理搜索结果
			if (resultHandler != null) {
				if (resultHandler instanceof HighlightResultHandler) {
					HighlightResultHandler highlightResultHandler = (HighlightResultHandler) resultHandler;
					highlightResultHandler.setQuery(luceneQuery);
					highlightResultHandler.setAnalyzer(searchFactory.getAnalyzer(domainType));
					highlightResultHandler.setHighLightFields(getHighLightFields(domainType));
					highlightResultHandler.setDomainType(domainType);
				}
				resultHandler.accept(result);
			}

			return new PageImpl<>(result, pageable, totalSize);

		} catch (EmptyQueryException ignored) {
			return new PageImpl<>(Collections.emptyList(), pageable, 0);
		}
	}

}