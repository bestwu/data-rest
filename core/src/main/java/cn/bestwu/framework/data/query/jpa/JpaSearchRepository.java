package cn.bestwu.framework.data.query.jpa;

import cn.bestwu.framework.data.annotation.HighLight;
import cn.bestwu.framework.data.query.QueryCarrier;
import cn.bestwu.framework.data.query.ResultHandler;
import cn.bestwu.framework.data.query.SearchRepository;
import cn.bestwu.framework.event.QueryBuilderEvent;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
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
import java.util.*;

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
	 * @param modelType modelType
	 * @param <T>       <T>
	 * @return 搜索字段
	 */
	private <T> String[] getSearchFields(Class<T> modelType) {
		String[] fields;
		if (fieldsCache.containsKey(modelType)) {
			fields = fieldsCache.get(modelType);
		} else {
			fields = JpaSearchFieldUtil.getAnnotationedFields(modelType, Field.class);
			if (fields.length == 0) {
				throw new RuntimeException("搜索的类型" + modelType + "没有标注索引字段,请使用org.hibernate.search.annotations.Field标注");
			}
			fieldsCache.put(modelType, fields);
		}
		return fields;
	}

	/**
	 * @param modelType modelType
	 * @param <T>       <T>
	 * @return 高亮字段
	 */
	private <T> String[] getHighLightFields(Class<T> modelType) {
		String[] highLightFields;
		if (highlightFieldsCache.containsKey(modelType)) {
			highLightFields = highlightFieldsCache.get(modelType);
		} else {
			highLightFields = JpaSearchFieldUtil.getAnnotationedFields(modelType, HighLight.class);
			if (highLightFields.length == 0) {
				highLightFields = getSearchFields(modelType);
			}
			highlightFieldsCache.put(modelType, highLightFields);
		}
		return highLightFields;
	}

	/**
	 * @param modelType     要搜索的类
	 * @param keyword       关键字
	 * @param pageable      分页
	 * @param resultHandler 结果处理
	 * @param <T>           T
	 * @return 结果
	 */
	@SuppressWarnings("unchecked")
	@Override public <T> Page search(Class<T> modelType, String keyword, Pageable pageable, ResultHandler resultHandler) {
		try {
			FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
			SearchFactory searchFactory = fullTextEntityManager.getSearchFactory();
			QueryBuilder queryBuilder = searchFactory.buildQueryBuilder().forEntity(modelType).get();
			if (!keyword.contains("?") && !keyword.contains("*")) {
				keyword = keyword + "*";
			}
			Query luceneQuery = queryBuilder.keyword().wildcard().onFields(getSearchFields(modelType)).matching(keyword).createQuery();
			{//生成query
				QueryCarrier queryCarrier = new QueryCarrier(queryBuilder, luceneQuery);
				publisher.publishEvent(new QueryBuilderEvent(queryCarrier, modelType));
				Query query = queryCarrier.getQuery();
				if (query != null) {
					luceneQuery = query;
				}
			}

			List<T> result;
			long totalSize;
			org.hibernate.search.jpa.FullTextQuery fullTextQuery = fullTextEntityManager.createFullTextQuery(luceneQuery, modelType);
			totalSize = fullTextQuery.getResultSize();

			if (totalSize > 0) {
				org.springframework.data.domain.Sort sort = pageable.getSort();
				if (sort != null) {
					List<SortField> sortFields = new ArrayList<>();
					sort.forEach(
							order -> sortFields.add(new SortField(order.getProperty(), SortField.Type.SCORE, org.springframework.data.domain.Sort.Direction.DESC.equals(order.getDirection()))));
					fullTextQuery.setSort(new Sort(sortFields.toArray(new SortField[sortFields.size()])));
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
					highlightResultHandler.setAnalyzer(searchFactory.getAnalyzer(modelType));
					highlightResultHandler.setHighLightFields(getHighLightFields(modelType));
					highlightResultHandler.setModelType(modelType);
				}
				resultHandler.accept(result);
			}

			return new PageImpl<>(result, pageable, totalSize);

		} catch (EmptyQueryException ignored) {
			return new PageImpl<>(Collections.emptyList(), pageable, 0);
		}
	}

}