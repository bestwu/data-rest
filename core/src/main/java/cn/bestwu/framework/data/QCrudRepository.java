package cn.bestwu.framework.data;

import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

/**
 * QueryDsl CrudRepository
 *
 * @param <T> T
 * @param <ID> ID
 * @author Peter Wu
 */
@NoRepositoryBean
public interface QCrudRepository<T, ID extends Serializable> extends CrudRepository<T, ID>, QueryDslPredicateExecutor<T> {
}
