package cn.bestwu.framework.data;

import cn.bestwu.framework.data.model.TestUser3;
import cn.bestwu.framework.data.annotation.SupportedHttpMethods;

/**
 * @author Peter Wu
 */
public interface TestUser3Repository extends QCrudRepository<TestUser3, Long> {

	@SupportedHttpMethods(SupportedHttpMethods.POST)
	@Override <S extends TestUser3> S save(S entity);
}
