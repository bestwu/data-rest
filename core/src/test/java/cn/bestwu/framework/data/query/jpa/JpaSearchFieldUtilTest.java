package cn.bestwu.framework.data.query.jpa;

import cn.bestwu.framework.util.StringUtil;
import org.hibernate.search.annotations.Field;
import org.junit.Test;

/**
 * @author Peter Wu
 */
public class JpaSearchFieldUtilTest {

	@Test
	public void name() throws Exception {
		String[] annotationedFields = JpaSearchFieldUtil.getAnnotationedFields(Spot.class, Field.class);
		System.err.println(StringUtil.valueOf(annotationedFields));
	}
}
