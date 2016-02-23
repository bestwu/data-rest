package cn.bestwu.framework.jpa;

import cn.bestwu.framework.data.annotation.SupportedHttpMethods;
import cn.bestwu.framework.jpa.model.TestUser3;

/**
 * @author Peter Wu
 */
public interface TestUser3Repository extends QCrudRepository<TestUser3, Long> {

	@SupportedHttpMethods(SupportedHttpMethods.POST)
	@Override <S extends TestUser3> S save(S entity);
}
