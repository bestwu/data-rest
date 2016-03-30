package cn.bestwu.framework.data;

import cn.bestwu.framework.data.annotation.HighLight;
import cn.bestwu.framework.data.util.EntityManagerUtil;
import cn.bestwu.framework.event.BeforeSearchEvent;
import cn.bestwu.framework.event.SearchResultEvent;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.query.dsl.TermMatchingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;

/**
 * 全文搜索类
 *
 * @author Peter Wu
 */
@Transactional(readOnly = true)
public class JpaSearchRepository implements SearchRepository {

	private Logger logger = LoggerFactory.getLogger(JpaSearchRepository.class);
	private Formatter defaultFormatter = new SimpleHTMLFormatter("<font color=\"#ff0000\">", "</font>");
	private Map<Class, String[]> fieldsCache = new HashMap<>();
	private Map<Class, String[]> highLightFieldsCache = new HashMap<>();
	@PersistenceContext
	private EntityManager entityManager;
	@Autowired
	private ApplicationEventPublisher publisher;

	/**
	 * 设置默认高亮格式
	 *
	 * @param defaultFormatter 默认高亮格式
	 */
	public void setDefaultFormatter(Formatter defaultFormatter) {
		this.defaultFormatter = defaultFormatter;
	}

	/**
	 * 可选高亮的全文搜索方法
	 *
	 * @param modelType 要搜索的类
	 * @param keyword   关键字
	 * @param pageable  分页
	 * @param highLight 是否高亮
	 * @param <T>       t
	 * @return 搜索结果
	 */
	@Override
	public <T> Page search(Class<T> modelType, String keyword, Pageable pageable, boolean highLight) {
		String[] fieldArray = getSearchFields(modelType);

		return search(modelType, keyword, pageable, fieldArray, (searchFactory, query, result) -> {
			//highLight
			if (highLight) {
				String[] highLightFieldArray = getHighLightFields(modelType);
				if (highLightFieldArray == null) {
					highLightFieldArray = fieldArray;
				}
				jpaHighLight(query, searchFactory.getAnalyzer(modelType), result, modelType, highLightFieldArray);
			} else {
				result.forEach(t -> publisher.publishEvent(new SearchResultEvent(t)));
			}
		});
	}

	/**
	 * 不处理结果的全文搜索
	 *
	 * @param modelType  要搜索的类
	 * @param keyword    关键字
	 * @param pageable   分页
	 * @param fieldArray fieldArray
	 * @param <T>        t
	 * @return 搜索结果
	 */
	@Override
	public <T> Page<T> search(Class<T> modelType, String keyword, Pageable pageable, String[] fieldArray) {
		return search(modelType, keyword, pageable, fieldArray, null);
	}

	/**
	 * 可选是否处理结果的全文搜索
	 *
	 * @param modelType 要搜索的类
	 * @param keyword   关键字
	 * @param pageable  分页
	 * @param <T>       t
	 * @return 搜索结果
	 */
	private <T> Page<T> search(Class<T> modelType, String keyword, Pageable pageable, String[] fieldArray, ResultHandler<T> handleResult) {

		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
		SearchFactory searchFactory = fullTextEntityManager.getSearchFactory();
		QueryBuilder queryBuilder = searchFactory.buildQueryBuilder().forEntity(modelType).get();

		TermMatchingContext termMatchingContext = queryBuilder.keyword().onFields(fieldArray);

		Query luceneQuery = termMatchingContext.matching(keyword).createQuery();
		org.hibernate.search.jpa.FullTextQuery jpaQuery = fullTextEntityManager.createFullTextQuery(luceneQuery, modelType);

		Criteria criteria = EntityManagerUtil.getSession(entityManager).createCriteria(modelType);
		publisher.publishEvent(new BeforeSearchEvent(criteria, modelType));
		boolean noCriterionEntries = criteria.toString().contains("[][]");
		if (!noCriterionEntries) {
			jpaQuery.setCriteriaQuery(criteria);
		}
		jpaQuery.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
		jpaQuery.setMaxResults(pageable.getPageSize());

		org.springframework.data.domain.Sort sort = pageable.getSort();
		if (sort != null) {
			List<SortField> sortFields = new ArrayList<>();
			sort.forEach(order -> new SortField(order.getProperty(), SortField.Type.SCORE, org.springframework.data.domain.Sort.Direction.DESC.equals(order.getDirection())));
			jpaQuery.setSort(new Sort(sortFields.toArray(new SortField[sortFields.size()])));
		}

		@SuppressWarnings("unchecked")
		List<T> result = jpaQuery.getResultList();

		long totalSize;
		if (result.size() == 0) {
			totalSize = 0;
		} else if (noCriterionEntries) {
			totalSize = jpaQuery.getResultSize();
		} else {
			criteria.setProjection(Projections.count("id"));
			totalSize = (long) criteria.list().get(0);
		}

		//处理搜索结果
		if (handleResult != null) {
			handleResult.accept(searchFactory, luceneQuery, result);
		}

		return new PageImpl<>(result, pageable, totalSize);
	}

	/**
	 * 处理搜索结果的函数接口
	 */
	@FunctionalInterface
	public interface ResultHandler<T> {

		void accept(SearchFactory searchFactory, Query query, List<T> result);
	}

	/*
	 * 高亮字段
	 */
	private <T> String[] getHighLightFields(Class<T> modelType) {
		String[] highLightFieldArray = null;
		if (highLightFieldsCache.containsKey(modelType)) {
			highLightFieldArray = highLightFieldsCache.get(modelType);
		} else {
			Set<String> fields = new HashSet<>();
			Arrays.stream(modelType.getDeclaredFields()).forEach(field -> addHighLightFields(fields, field, null));
			if (!fields.isEmpty()) {
				highLightFieldArray = fields.toArray(new String[fields.size()]);
				highLightFieldsCache.put(modelType, highLightFieldArray);
			}
		}
		return highLightFieldArray;
	}

	private void addHighLightFields(Set<String> fields, java.lang.reflect.Field field, String parentFieldName) {
		String fieldName = field.getName();
		if (parentFieldName != null) {
			fieldName = parentFieldName + "." + fieldName;
		}
		if (field.isAnnotationPresent(HighLight.class)) {
			fields.add(fieldName);
		} else if (field.isAnnotationPresent(IndexedEmbedded.class)) {
			for (java.lang.reflect.Field fieldField : field.getType().getDeclaredFields()) {
				addHighLightFields(fields, fieldField, fieldName);
			}
		}
	}

	/*
	 * 默认搜索字段
	 */
	private <T> String[] getSearchFields(Class<T> modelType) {
		String[] fieldArray;
		if (fieldsCache.containsKey(modelType)) {
			fieldArray = fieldsCache.get(modelType);
		} else {
			Set<String> fields = new HashSet<>();
			Arrays.stream(modelType.getDeclaredFields()).forEach(field -> addSearchFields(fields, field, null));
			fieldArray = fields.toArray(new String[fields.size()]);
			fieldsCache.put(modelType, fieldArray);
		}
		return fieldArray;
	}

	private void addSearchFields(Set<String> fields, java.lang.reflect.Field field, String parentFieldName) {
		String fieldName = field.getName();
		if (parentFieldName != null) {
			fieldName = parentFieldName + "." + fieldName;
		}
		if (field.isAnnotationPresent(Field.class)) {
			fields.add(fieldName);
		} else if (field.isAnnotationPresent(IndexedEmbedded.class)) {
			for (java.lang.reflect.Field fieldField : field.getType().getDeclaredFields()) {
				addSearchFields(fields, fieldField, fieldName);
			}
		}
	}

	/**
	 * 高亮显示文章
	 *
	 * @param query     {@link Query}
	 * @param analyzer  analyzer
	 * @param data      要高亮的数据
	 * @param modelType modelType
	 * @param fields    需要高亮的字段   @return 高亮数据
	 * @param <T>       t
	 */
	@Override public <T> void jpaHighLight(Query query, Analyzer analyzer, List<T> data, Class<T> modelType, String... fields) {
		QueryScorer queryScorer = new QueryScorer(query);
		Highlighter highlighter = new Highlighter(defaultFormatter, queryScorer);
		for (T t : data) {
			publisher.publishEvent(new SearchResultEvent(t));
			for (String fieldName : fields) {
				hightLightField(analyzer, modelType, highlighter, t, fieldName);
			}
		}
	}

	private <T> void hightLightField(Analyzer analyzer, Class<?> modelType, Highlighter highlighter, T t, String fieldName) {
		try {
			if (fieldName.contains(".")) {
				String[] split = fieldName.split("\\.");
				String pfieldName = split[0];
				Object fieldValue = ReflectionUtils.invokeMethod(BeanUtils.getPropertyDescriptor(modelType, pfieldName).getReadMethod(), t);
				Class<?> fieldType = modelType.getDeclaredField(pfieldName).getType();
				String propertyName = split[1];
				hightLightField(analyzer, fieldType, highlighter, fieldValue, propertyName);
			} else {
				Object fieldValue = ReflectionUtils.invokeMethod(BeanUtils.getPropertyDescriptor(modelType, fieldName).getReadMethod(), t);
				String text = String.valueOf(fieldValue);
				if (!text.contains("<font color=\"#ff0000\">")) {
					String highLightFieldValue = highlighter.getBestFragment(analyzer, fieldName, text);
					if (highLightFieldValue != null) {
						ReflectionUtils.invokeMethod(BeanUtils.getPropertyDescriptor(modelType, fieldName).getWriteMethod(), t, highLightFieldValue);
					}
				}
			}
		} catch (Exception e) {
			//不处理，只记录日志
			logger.error("高亮显示关键字失败", e);
		}
	}

	//	/**
	//	 * 高亮显示文章
	//	 *
	//	 * @param query     {@link Query}
	//	 * @param analyzer  analyzer
	//	 * @param data      要高亮的数据
	//	 * @param modelType modelType
	//	 * @param fields    需要高亮的字段   @return 高亮数据
	//	 */
	//	private <T> void highLight(Query query, Analyzer analyzer, List<T> data, Class<T> modelType, String... fields) {
	//		QueryScorer queryScorer = new QueryScorer(query);
	//		Highlighter highlighter = new Highlighter(defaultFormatter, queryScorer);
	//		for (T t : data) {
	//			publisher.publishEvent(new SearchResultEvent(t));
	//			for (String fieldName : fields) {
	//				try {
	//					Object fieldValue = ReflectionUtils.invokeMethod(BeanUtils.getPropertyDescriptor(modelType, fieldName).getReadMethod(), t);
	//					String highLightFieldValue = highlighter.getBestFragment(analyzer, fieldName, String.valueOf(fieldValue));
	//					if (highLightFieldValue != null) {
	//						ReflectionUtils.invokeMethod(BeanUtils.getPropertyDescriptor(modelType, fieldName).getWriteMethod(), t, highLightFieldValue);
	//					}
	//				} catch (Exception e) {
	//					//不处理，只记录日志
	//					logger.error("高亮显示关键字失败", e);
	//				}
	//			}
	//		}
	//	}

}