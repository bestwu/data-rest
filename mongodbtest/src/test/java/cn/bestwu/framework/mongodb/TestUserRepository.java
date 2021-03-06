package cn.bestwu.framework.mongodb;

import cn.bestwu.framework.data.annotation.RepositoryRestResource;
import cn.bestwu.framework.mongodb.domain.TestUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

/**
 * @author Peter Wu
 */
@RepositoryRestResource
public interface TestUserRepository extends MongoRepository<TestUser, String>, QueryDslPredicateExecutor<TestUser> {
	Page<TestUser> findAllBy(TextCriteria criteria, Pageable pageable);
}
