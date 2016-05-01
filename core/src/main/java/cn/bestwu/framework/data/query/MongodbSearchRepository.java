package cn.bestwu.framework.data.query;

import cn.bestwu.framework.rest.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class MongodbSearchRepository implements SearchRepository {

	private final Repositories repositories;

	public MongodbSearchRepository(Repositories repositories) {
		this.repositories = repositories;
	}

	@Override public <T> Page search(Class<T> modelType, String keyword, Pageable pageable, ResultHandler resultHandler) {
		Class<?> repositoryInterface = repositories.getRepositoryInformationFor(modelType).getRepositoryInterface();
		try {
			Method method = repositoryInterface.getMethod("findAllBy", TextCriteria.class, Pageable.class);
			TextCriteria criteria = TextCriteria.forDefaultLanguage().matchingPhrase(keyword);
			Page page = (Page) method.invoke(repositories.getRepositoryFor(modelType), criteria, pageable);
			if (resultHandler != null) {
				resultHandler.accept(page.getContent());
			}
			return page;
		} catch (NoSuchMethodException e) {
			throw new ResourceNotFoundException();
		} catch (InvocationTargetException | IllegalAccessException e) {
			log.error(e.getMessage(), e);
			return new PageImpl<>(Collections.emptyList());
		}
	}
}
