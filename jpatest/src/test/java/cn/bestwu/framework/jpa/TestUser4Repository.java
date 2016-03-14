package cn.bestwu.framework.jpa;

import cn.bestwu.framework.data.annotation.RepositoryRestResource;
import cn.bestwu.framework.jpa.model.TestUser4;

/**
 * @author Peter Wu
 */
@RepositoryRestResource(contained = true)
public interface TestUser4Repository extends QCrudRepository<TestUser4, Long> {

	@RepositoryRestResource(RepositoryRestResource.PUT)
	@Override <S extends TestUser4> S save(S entity);

}
