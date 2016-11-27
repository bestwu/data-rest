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
 * Mongodb 搜索实现
 *
 * @author Peter Wu
 */
@Slf4j
public class MongodbSearchRepository implements SearchRepository {

	private final Repositories repositories;

	public MongodbSearchRepository(Repositories repositories) {
		this.repositories = repositories;
	}

	/**
	 * @param domainType     要搜索的类
	 * @param keyword       关键字
	 * @param pageable      分页
	 * @param resultHandler 结果处理
	 * @param <T>           T
	 * @return 结果
	 */
	@Override public <T> Page search(Class<T> domainType, String keyword, Pageable pageable, ResultHandler resultHandler) {
		Class<?> repositoryInterface = repositories.getRepositoryInformationFor(domainType).getRepositoryInterface();
		try {
			Method method = repositoryInterface.getMethod("findAllBy", TextCriteria.class, Pageable.class);
			TextCriteria criteria = TextCriteria.forDefaultLanguage().matchingPhrase(keyword);
			Page page = (Page) method.invoke(repositories.getRepositoryFor(domainType), criteria, pageable);
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
