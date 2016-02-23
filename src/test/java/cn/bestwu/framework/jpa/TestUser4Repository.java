package cn.bestwu.framework.jpa;

import cn.bestwu.framework.data.annotation.RepositoryRestResource;
import cn.bestwu.framework.data.annotation.SupportedHttpMethods;
import cn.bestwu.framework.jpa.model.TestUser4;

/**
 * @author Peter Wu
 */
public interface TestUser4Repository extends QCrudRepository<TestUser4, Long> {

	@SupportedHttpMethods(SupportedHttpMethods.PUT)
	@Override <S extends TestUser4> S save(S entity);

	@RepositoryRestResource(false)
	@Override Iterable<TestUser4> findAll();

	@RepositoryRestResource(false)
	@Override void delete(Long aLong);
}
