package cn.bestwu.framework.data;

import cn.bestwu.framework.data.model.TestUser4;
import cn.bestwu.framework.data.annotation.RepositoryRestResource;
import cn.bestwu.framework.data.annotation.SupportedHttpMethods;

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
