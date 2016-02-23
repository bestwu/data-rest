package cn.bestwu.framework.jpa;

import cn.bestwu.framework.data.annotation.RepositoryRestResource;
import cn.bestwu.framework.jpa.model.TestUser2;

/**
 * @author Peter Wu
 */
@RepositoryRestResource(false)
public interface TestUser2Repository extends QCrudRepository<TestUser2, Long> {

}
