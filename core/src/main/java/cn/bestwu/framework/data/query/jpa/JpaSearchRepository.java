package cn.bestwu.framework.data.query.jpa;

import cn.bestwu.framework.data.annotation.HighLight;
import cn.bestwu.framework.data.query.ResultHandler;
import cn.bestwu.framework.data.query.SearchRepository;
import cn.bestwu.framework.data.util.EntityManagerUtil;
import cn.bestwu.framework.event.BeforeSearchEvent;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.hibernate.Criteria;
import org.hibernate.QueryTimeoutException;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.exception.EmptyQueryException;
import org.hibernate.search.hcore.util.impl.ContextHelper;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.query.dsl.TermMatchingContext;
import org.hibernate.search.query.engine.spi.EntityInfo;
import org.hibernate.search.query.engine.spi.HSQuery;
import org.hibernate.search.query.engine.spi.TimeoutExceptionFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.*;

/**
 * 全文搜索类
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

	@SuppressWarnings("unchecked")
	@Override public <T> Page search(Class<T> modelType, String keyword, Pageable pageable, ResultHandler resultHandler) {
		try {
			FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
			SearchFactory searchFactory = fullTextEntityManager.getSearchFactory();
			QueryBuilder queryBuilder = searchFactory.buildQueryBuilder().forEntity(modelType).get();

			TermMatchingContext termMatchingContext = queryBuilder.keyword().onFields(getSearchFields(modelType));

			Query luceneQuery = termMatchingContext.matching(keyword).createQuery();

			Criteria criteria = EntityManagerUtil.getSession(entityManager).createCriteria(modelType);
			publisher.publishEvent(new BeforeSearchEvent(criteria, modelType));
			boolean noCriterionEntries = criteria.toString().contains("[][]");

			List<T> result;
			long totalSize;

			org.springframework.data.domain.Sort sort = pageable.getSort();

			if (noCriterionEntries) {
				org.hibernate.search.jpa.FullTextQuery jpaQuery = fullTextEntityManager.createFullTextQuery(luceneQuery, modelType);

				if (sort != null) {
					List<SortField> sortFields = new ArrayList<>();
					sort.forEach(order -> sortFields.add(new SortField(order.getProperty(), SortField.Type.SCORE, org.springframework.data.domain.Sort.Direction.DESC.equals(order.getDirection()))));
					jpaQuery.setSort(new Sort(sortFields.toArray(new SortField[sortFields.size()])));
				}

				jpaQuery.setFirstResult(pageable.getOffset());
				jpaQuery.setMaxResults(pageable.getPageSize());
				result = jpaQuery.getResultList();
				if (result.size() == 0) {
					totalSize = 0;
				} else {
					totalSize = jpaQuery.getResultSize();
				}
			} else {
				SessionImplementor sessionImplementor = (SessionImplementor) EntityManagerUtil.getSession(entityManager);

				HSQuery hSearchQuery = ContextHelper.getSearchintegratorBySessionImplementor(sessionImplementor).createHSQuery();
				hSearchQuery
						.luceneQuery(luceneQuery)
						.timeoutExceptionFactory(exceptionFactory)
						.tenantIdentifier(sessionImplementor.getTenantIdentifier())
						.targetedEntities(Arrays.asList(new Class[] { modelType }));

				List<EntityInfo> entityInfos = hSearchQuery.queryEntityInfos();
				List<Serializable> ids = new ArrayList<>(entityInfos.size());
				String idName = null;
				for (EntityInfo entityInfo : entityInfos) {
					if (idName == null) {
						idName = entityInfo.getIdName();
					}
					ids.add(entityInfo.getId());
				}

				if (sort != null) {
					sort.forEach(order -> {
						if (org.springframework.data.domain.Sort.Direction.DESC.equals(order.getDirection())) {
							criteria.addOrder(org.hibernate.criterion.Order.desc(order.getProperty()));
						} else {
							criteria.addOrder(org.hibernate.criterion.Order.asc(order.getProperty()));
						}
					});
				}

				criteria.add(Restrictions.in(idName, ids));
				criteria.setFirstResult(pageable.getOffset());
				criteria.setMaxResults(pageable.getPageSize());
				result = criteria.list();
				if (result.size() == 0) {
					totalSize = 0;
				} else {
					criteria.setProjection(Projections.count("id"));
					totalSize = (long) criteria.list().get(0);
				}
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

	private static final TimeoutExceptionFactory exceptionFactory = (message, queryDescription) -> new QueryTimeoutException(message, null, queryDescription);
}