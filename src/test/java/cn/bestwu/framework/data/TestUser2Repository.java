package cn.bestwu.framework.data;

import cn.bestwu.framework.data.model.TestUser2;
import cn.bestwu.framework.data.annotation.RepositoryRestResource;

/**
 * @author Peter Wu
 */
@RepositoryRestResource(false)
public interface TestUser2Repository extends QCrudRepository<TestUser2, Long> {

}
