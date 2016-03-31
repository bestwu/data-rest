package cn.bestwu.framework.data.query.jpa;

import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class BuildSearchIndex implements ApplicationListener<ContextRefreshedEvent> {

	private Logger logger = LoggerFactory.getLogger(BuildSearchIndex.class);

	@PersistenceContext
	private EntityManager entityManager;

	@Override public void onApplicationEvent(ContextRefreshedEvent event) {
		try {
			FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
			if (!fullTextEntityManager.getSearchFactory().getIndexedTypes().isEmpty()) {
				fullTextEntityManager.createIndexer().startAndWait();
			}
		} catch (Exception e) {
			logger.error("An error occurred trying to build the serach index: ", e);
		}
	}
}