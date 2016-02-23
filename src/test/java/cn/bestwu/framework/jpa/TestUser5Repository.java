package cn.bestwu.framework.jpa;

import cn.bestwu.framework.jpa.model.TestUser5;
import com.mysema.query.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * @author Peter Wu
 */
public interface TestUser5Repository extends QCrudRepository<TestUser5, Long> {

	@Override <S extends TestUser5> S save(S entity);

	@Override Page<TestUser5> findAll(Predicate predicate, Pageable pageable);
}
