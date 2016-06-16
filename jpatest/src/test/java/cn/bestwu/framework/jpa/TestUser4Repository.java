package cn.bestwu.framework.jpa;

import cn.bestwu.framework.data.annotation.RepositoryRestResource;
import cn.bestwu.framework.jpa.model.TestUser4;
import org.springframework.http.HttpMethod;

/**
 * @author Peter Wu
 */
public interface TestUser4Repository extends QCrudRepository<TestUser4, Long> {

	@RepositoryRestResource(HttpMethod.PUT)
	@Override <S extends TestUser4> S save(S entity);

}
