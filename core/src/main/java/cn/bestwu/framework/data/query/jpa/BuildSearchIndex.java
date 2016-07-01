package cn.bestwu.framework.data.query.jpa;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * 建立Hibernate Search 索引
 *
 * @author Peter Wu
 */
@Slf4j
public class BuildSearchIndex implements ApplicationListener<ContextRefreshedEvent> {

	@PersistenceContext
	private EntityManager entityManager;

	@Override public void onApplicationEvent(ContextRefreshedEvent event) {
		try {
			FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
			if (!fullTextEntityManager.getSearchFactory().getIndexedTypes().isEmpty()) {
				fullTextEntityManager.createIndexer().startAndWait();
			}
		} catch (Exception e) {
			log.error("An error occurred trying to build the serach index: ", e);
		}
	}
}