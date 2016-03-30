package cn.bestwu.framework;

import cn.bestwu.framework.data.JpaSearchRepository;
import cn.bestwu.framework.data.MongodbSearchRepository;
import cn.bestwu.framework.data.SearchRepository;
import cn.bestwu.framework.event.AnnotatedEventHandlerInvoker;
import cn.bestwu.framework.rest.config.RestMvcConfiguration;
import org.hibernate.search.jpa.Search;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.auditing.AuditableBeanWrapperFactory;
import org.springframework.data.auditing.MappingAuditableBeanWrapperFactory;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.data.mongodb.core.query.TextCriteria;

import java.util.ArrayList;
import java.util.List;

/**
 * DataRestConfiguration 配置
 *
 * @author Peter Wu
 */
@Configuration
@Import({ RestMvcConfiguration.class })
public class DataRestConfiguration {

	@Autowired
	private ApplicationContext applicationContext;

	@Bean
	public PersistentEntities persistentEntities() {

		List<MappingContext<?, ?>> arrayList = new ArrayList<>();

		BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, MappingContext.class).values().forEach(arrayList::add);

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

	@Configuration
	@ConditionalOnMissingBean(SearchRepository.class)
	@ConditionalOnClass(Search.class)
	protected static class JpaSearchRepositoryConfiguration {

		@Bean
		public SearchRepository searchRepository() {
			return new JpaSearchRepository();
		}
	}

	@Configuration
	@ConditionalOnMissingBean(SearchRepository.class)
	@ConditionalOnClass(TextCriteria.class)
	protected static class MongodbSearchRepositoryConfiguration {

		@Bean
		public SearchRepository searchRepository() {
			return new MongodbSearchRepository();
		}
	}
}
