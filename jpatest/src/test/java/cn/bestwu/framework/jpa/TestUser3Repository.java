package cn.bestwu.framework.jpa;

import cn.bestwu.framework.data.QCrudRepository;
import cn.bestwu.framework.data.annotation.RepositoryRestResource;
import cn.bestwu.framework.jpa.domain.TestUser3;
import org.springframework.http.HttpMethod;

/**
 * @author Peter Wu
 */
@RepositoryRestResource
public interface TestUser3Repository extends QCrudRepository<TestUser3, Long> {

	@RepositoryRestResource(HttpMethod.POST)
	@Override <S extends TestUser3> S save(S entity);
}
