package cn.bestwu.framework.jpa;

import cn.bestwu.framework.data.QCrudRepository;
import cn.bestwu.framework.data.annotation.RepositoryRestResource;
import cn.bestwu.framework.jpa.domain.TestUser;

/**
 * @author Peter Wu
 */
@RepositoryRestResource
public interface TestUserRepository extends QCrudRepository<TestUser, Long> {
}
