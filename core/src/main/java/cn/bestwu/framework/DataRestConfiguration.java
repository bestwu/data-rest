package cn.bestwu.framework;

import cn.bestwu.framework.data.query.MongodbSearchRepository;
import cn.bestwu.framework.data.query.SearchRepository;
import cn.bestwu.framework.data.query.jpa.JpaSearchRepository;
import cn.bestwu.framework.event.AnnotatedEventHandlerInvoker;
import cn.bestwu.framework.rest.config.RestMvcConfiguration;
import cn.bestwu.framework.rest.resolver.LucenePageableHandlerMethodArgumentResolver;
import cn.bestwu.framework.rest.resolver.LuceneSortHandlerMethodArgumentResolver;
import cn.bestwu.lang.util.AutowireHelper;
import org.hibernate.search.jpa.Search;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.data.auditing.AuditableBeanWrapperFactory;
import org.springframework.data.auditing.MappingAuditableBeanWrapperFactory;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

/**
 * DataRestConfiguration 配置
 *
 * @author Peter Wu
 */
@Configuration
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE + 9)
@Import({ RestMvcConfiguration.class })
public class DataRestConfiguration {

	@Autowired
	private ApplicationContext applicationContext;

	/**
	 * 初始化bean工具类
	 *
	 * @return bean工具类
	 */
	@Bean
	public AutowireHelper autowireHelper() {
		return new AutowireHelper();
	}

	@Bean
	@ConditionalOnMissingBean(Repositories.class)
	public Repositories repositories() {
		return new Repositories(applicationContext);
	}

	@Bean
	public PersistentEntities persistentEntities() {

		List<MappingContext<?, ?>> arrayList = new ArrayList<>();

		for (MappingContext mappingContext : BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, MappingContext.class).values()) {
			arrayList.add(mappingContext);
		}
		return new PersistentEntities(arrayList);
	}

	@Bean
	public AuditableBeanWrapperFactory auditableBeanWrapperFactory() {
		return new MappingAuditableBeanWrapperFactory(persistentEntities());
	}

	/**
	 * @return EventHandlerInvoker
	 */
	@Bean
	public AnnotatedEventHandlerInvoker annotatedEventHandlerInvoker() {
		return new AnnotatedEventHandlerInvoker();
	}

	/**
	 * jpa 全文搜索
	 */
	@Configuration
	@ConditionalOnMissingBean(SearchRepository.class)
	@ConditionalOnClass(Search.class)
	protected static class JpaSearchRepositoryConfiguration {

		@PersistenceContext
		private EntityManager entityManager;
		@Autowired
		private ApplicationEventPublisher publisher;

		@Bean
		public SearchRepository searchRepository() {
			return new JpaSearchRepository(entityManager, publisher);
		}

		@Configuration
		protected static class SpringDataWebConfiguration extends WebMvcConfigurerAdapter {

			@Bean
			public PageableHandlerMethodArgumentResolver pageableResolver() {
				return new LucenePageableHandlerMethodArgumentResolver(lucenceSortResolver());
			}

			@Bean
			public LuceneSortHandlerMethodArgumentResolver lucenceSortResolver() {
				return new LuceneSortHandlerMethodArgumentResolver();
			}

			@Bean
			public SortHandlerMethodArgumentResolver sortResolver() {
				return new SortHandlerMethodArgumentResolver();
			}

			@Override
			public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
				argumentResolvers.add(sortResolver());
				argumentResolvers.add(pageableResolver());
			}
		}

	}

	/**
	 * Mongodb全文搜索
	 */
	@Configuration
	@ConditionalOnMissingBean(SearchRepository.class)
	@ConditionalOnClass(TextCriteria.class)
	protected static class MongodbSearchRepositoryConfiguration {

		@Autowired
		private Repositories repositories;

		@Bean
		public SearchRepository searchRepository() {
			return new MongodbSearchRepository(repositories);
		}
	}
}
