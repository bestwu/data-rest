package cn.bestwu.framework.data;

import cn.bestwu.framework.rest.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.repository.support.Repositories;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;

/**
 * @author Peter Wu
 */
public class MongodbSearchRepository implements SearchRepository {
	@Autowired
	private Repositories repositories;
	private Logger logger = LoggerFactory.getLogger(MongodbSearchRepository.class);

	@Override public <T> Page search(Class<T> domainType, String keyword, Pageable pageable, boolean highLight) {
		Class<?> repositoryInterface = repositories.getRepositoryInformationFor(domainType).getRepositoryInterface();
		try {
			Method method = repositoryInterface.getMethod("findAllBy", TextCriteria.class, Pageable.class);
			TextCriteria criteria = TextCriteria.forDefaultLanguage().matchingPhrase(keyword);
			return (Page) method.invoke(repositories.getRepositoryFor(domainType), criteria, pageable);
		} catch (NoSuchMethodException e) {
			throw new ResourceNotFoundException();
		} catch (InvocationTargetException | IllegalAccessException e) {
			logger.error(e.getMessage(), e);
			return new PageImpl<>(Collections.emptyList());
		}
	}

	@Override public <T> Page<T> search(Class<T> domainType, String keyword, Pageable pageable, String[] fieldArray) {
		return null;
	}
}
