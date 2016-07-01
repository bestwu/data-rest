package cn.bestwu.framework.jpa;

import cn.bestwu.framework.data.QCrudRepository;
import cn.bestwu.framework.data.annotation.RepositoryRestResource;
import cn.bestwu.framework.jpa.model.TestUser4;

/**
 * @author Peter Wu
 */
public interface TestUser4Repository extends QCrudRepository<TestUser4, Long> {

	@RepositoryRestResource
	@Override Iterable<TestUser4> findAll();
}
