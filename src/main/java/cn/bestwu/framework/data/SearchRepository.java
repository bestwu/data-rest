package cn.bestwu.framework.data;

import cn.bestwu.framework.data.annotation.HighLight;
import cn.bestwu.framework.data.util.EntityManagerUtil;
import cn.bestwu.framework.event.BeforeSearchEvent;
import cn.bestwu.framework.event.SearchResultEvent;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.annotations.Field;
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
public class SearchRepository {

	private Logger logger = LoggerFactory.getLogger(SearchRepository.class);
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
	 * @param domainType 要搜索的类
	 * @param keyword    关键字
	 * @param pageable   分页
	 * @param sort       排序
	 * @param highLight  是否高亮
	 * @param formatter  高亮格式
	 * @param <T>        t
	 * @return 搜索结果
	 */
	public <T> Page search(Class<T> domainType, String keyword, Pageable pageable, Sort sort, boolean highLight, Formatter formatter) {
		String[] fieldArray = getSearchFields(domainType);

		return search(domainType, keyword, pageable, sort, fieldArray, (searchFactory, query, result) -> {
			//highLight
			if (highLight) {
				String[] highLightFieldArray = getHighLightFields(domainType);
				if (highLightFieldArray == null) {
					highLightFieldArray = fieldArray;
				}
				Formatter finalFormatter = formatter;
				if (finalFormatter == null) {
					finalFormatter = defaultFormatter;
				}
				highLight(query, searchFactory.getAnalyzer(domainType), result, domainType, finalFormatter, highLightFieldArray);
			} else {
				result.forEach(t -> publisher.publishEvent(new SearchResultEvent(t)));
			}
		});
	}

	/**
	 * 不处理结果的全文搜索
	 *
	 * @param domainType 要搜索的类
	 * @param keyword    关键字
	 * @param pageable   分页
	 * @param sort       排序
	 * @param fieldArray fieldArray
	 * @param <T>        t
	 * @return 搜索结果
	 */
	public <T> Page<T> search(Class<T> domainType, String keyword, Pageable pageable, Sort sort, String[] fieldArray) {
		return search(domainType, keyword, pageable, sort, fieldArray, null);
	}

	/**
	 * 可选是否处理结果的全文搜索
	 *
	 * @param domainType 要搜索的类
	 * @param keyword    关键字
	 * @param pageable   分页
	 * @param sort       排序
	 * @param <T>        t
	 * @return 搜索结果
	 */
	private <T> Page<T> search(Class<T> domainType, String keyword, Pageable pageable, Sort sort, String[] fieldArray, ResultHandler<T> handleResult) {

		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
		SearchFactory searchFactory = fullTextEntityManager.getSearchFactory();
		QueryBuilder queryBuilder = searchFactory.buildQueryBuilder().forEntity(domainType).get();

		TermMatchingContext termMatchingContext = queryBuilder.keyword().onFields(fieldArray);

		Query luceneQuery = termMatchingContext.matching(keyword).createQuery();
		org.hibernate.search.jpa.FullTextQuery jpaQuery = fullTextEntityManager.createFullTextQuery(luceneQuery, domainType);

		Criteria criteria = EntityManagerUtil.getSession(entityManager).createCriteria(domainType);
		publisher.publishEvent(new BeforeSearchEvent(criteria, domainType));
		boolean noCriterionEntries = criteria.toString().contains("[][]");
		if (!noCriterionEntries) {
			jpaQuery.setCriteriaQuery(criteria);
		}
		jpaQuery.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
		jpaQuery.setMaxResults(pageable.getPageSize());
		if (sort != null) {
			jpaQuery.setSort(sort);
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
	private <T> String[] getHighLightFields(Class<T> domainType) {
		String[] highLightFieldArray = null;
		if (highLightFieldsCache.containsKey(domainType)) {
			highLightFieldArray = highLightFieldsCache.get(domainType);
		} else {
			Set<String> fields = new HashSet<>();
			Arrays.stream(domainType.getDeclaredFields()).forEach(field -> {
				if (field.isAnnotationPresent(HighLight.class)) {
					fields.add(field.getName());
				}
			});
			if (!fields.isEmpty()) {
				highLightFieldArray = fields.toArray(new String[fields.size()]);
				highLightFieldsCache.put(domainType, highLightFieldArray);
			}
		}
		return highLightFieldArray;
	}

	/*
	 * 默认搜索字段
	 */
	private <T> String[] getSearchFields(Class<T> domainType) {
		String[] fieldArray;
		if (fieldsCache.containsKey(domainType)) {
			fieldArray = fieldsCache.get(domainType);
		} else {
			Set<String> fields = new HashSet<>();
			Arrays.stream(domainType.getDeclaredFields()).forEach(field -> {
				if (field.isAnnotationPresent(Field.class)) {
					fields.add(field.getName());
				}
			});
			fieldArray = fields.toArray(new String[fields.size()]);
			fieldsCache.put(domainType, fieldArray);
		}
		return fieldArray;
	}

	/**
	 * 高亮显示文章
	 *
	 * @param query      {@link Query}
	 * @param analyzer   analyzer
	 * @param data       要高亮的数据
	 * @param domainType domainType
	 * @param fields     需要高亮的字段   @return 高亮数据
	 */
	private <T> void highLight(Query query, Analyzer analyzer, List<T> data, Class<T> domainType, Formatter formatter, String... fields) {
		QueryScorer queryScorer = new QueryScorer(query);
		Highlighter highlighter = new Highlighter(formatter, queryScorer);
		for (T t : data) {
			publisher.publishEvent(new SearchResultEvent(t));
			for (String fieldName : fields) {
				try {
					Object fieldValue = ReflectionUtils.invokeMethod(BeanUtils.getPropertyDescriptor(domainType, fieldName).getReadMethod(), t);
					String highLightFieldValue = highlighter.getBestFragment(analyzer, fieldName, String.valueOf(fieldValue));
					if (highLightFieldValue != null) {
						ReflectionUtils.invokeMethod(BeanUtils.getPropertyDescriptor(domainType, fieldName).getWriteMethod(), t, highLightFieldValue);
					}
				} catch (Exception e) {
					//不处理，只记录日志
					logger.error("高亮显示关键字失败", e);
				}
			}
		}
	}

}